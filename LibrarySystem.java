import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LibrarySystem {
    private static final String USERS_FILE = "users.txt";
    private static final String BOOKS_FILE = "books.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private User loggedInUser = null;

    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        LibrarySystem app = new LibrarySystem();
        app.run();
    }

    public void run() {
        System.out.println("Welcome to the Library Management System");
        System.out.println("----------------------------------------");

        try {
            loadAllFiles();
        } catch (FileNotFoundException fnfe) {
            System.out.println("One or more data files are missing. Creating default files...");
            createDefaultFiles();
            try {
                loadAllFiles();
            } catch (Exception e) {
                System.err.println("Failed to load files after creating defaults: " + e.getMessage());
                return;
            }
        } catch (IOException ioe) {
            System.err.println("IO error while loading files: " + ioe.getMessage());
            return;
        }

        boolean loggedIn = login();
        if (!loggedIn) {
            System.out.println("Exceeded login attempts. Exiting.");
            return;
        }

        // Demonstrate polymorphism
        Person p = loggedInUser; // Person reference to a User object
        System.out.print("Polymorphism demo - using Person reference: ");
        p.displayInfo();

        displayMenu();

        try {
            saveAllFiles();
            System.out.println("All changes saved. Goodbye!");
        } catch (IOException e) {
            System.err.println("Error saving files: " + e.getMessage());
        }
    }

    private void loadAllFiles() throws IOException {
        loadUsers();
        loadBooks();
        loadTransactions();
    }

    private void loadUsers() throws IOException {
        users.clear();
        Path path = Paths.get(USERS_FILE);
        if (!Files.exists(path)) throw new FileNotFoundException(USERS_FILE + " not found");
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Format: U001,John Doe,pass123,user
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                User u = new User(parts[0], parts[1], parts[2], parts[3]);
                users.add(u);
            }
        }
    }

    private void loadBooks() throws IOException {
        books.clear();
        Path path = Paths.get(BOOKS_FILE);
        if (!Files.exists(path)) throw new FileNotFoundException(BOOKS_FILE + " not found");
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Format: B001,The Great Gatsby,F. Scott Fitzgerald,true
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                boolean avail = Boolean.parseBoolean(parts[3]);
                Book b = new Book(parts[0], parts[1], parts[2], avail);
                books.add(b);
            }
        }
    }

    private void loadTransactions() throws IOException {
        transactions.clear();
        Path path = Paths.get(TRANSACTIONS_FILE);
        if (!Files.exists(path)) throw new FileNotFoundException(TRANSACTIONS_FILE + " not found");
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Format: T001,U001,B002,2025-10-14,null
                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;
                String tid = parts[0];
                String uid = parts[1];
                String bid = parts[2];
                LocalDate db = "null".equals(parts[3]) ? null : LocalDate.parse(parts[3]);
                LocalDate dr = "null".equals(parts[4]) ? null : LocalDate.parse(parts[4]);
                Transaction t = new Transaction(tid, uid, bid, db, dr);
                transactions.add(t);

                // Build user's borrowedBooks list for consistency
                if (dr == null) {
                    User u = findUserById(uid);
                    if (u != null) {
                        if (!u.getBorrowedBooks().contains(bid)) {
                            u.getBorrowedBooks().add(bid);
                        }
                    }
                }
            }
        }
    }

    private void saveAllFiles() throws IOException {
        saveUsers();
        saveBooks();
        saveTransactions();
    }

    private void saveUsers() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(USERS_FILE))) {
            for (User u : users) {
                bw.write(String.join(",", u.getId(), u.getName(), u.getPassword(), u.getRole()));
                bw.newLine();
            }
        }
    }

    private void saveBooks() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(BOOKS_FILE))) {
            for (Book b : books) {
                bw.write(String.join(",", b.getBookId(), b.getTitle(), b.getAuthor(), String.valueOf(b.isAvailable())));
                bw.newLine();
            }
        }
    }

    private void saveTransactions() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(TRANSACTIONS_FILE))) {
            for (Transaction t : transactions) {
                String db = t.getDateBorrowed() != null ? t.getDateBorrowed().toString() : "null";
                String dr = t.getDateReturned() != null ? t.getDateReturned().toString() : "null";
                bw.write(String.join(",", t.getTransactionId(), t.getUserId(), t.getBookId(), db, dr));
                bw.newLine();
            }
        }
    }

    private void createDefaultFiles() {
        try {
            List<String> defaultUsers = Arrays.asList(
                    "U001,John Doe,pass123,user",
                    "U002,Jane Smith,abc123,user",
                    "A001,Admin,admin123,admin"
            );
            Files.write(Paths.get(USERS_FILE), defaultUsers);

            List<String> defaultBooks = Arrays.asList(
                    "B001,The Great Gatsby,F. Scott Fitzgerald,true",
                    "B002,To Kill a Mockingbird,Harper Lee,true",
                    "B003,1984,George Orwell,false"
            );
            Files.write(Paths.get(BOOKS_FILE), defaultBooks);

            List<String> defaultTrans = Arrays.asList(
                    "T001,U001,B002,2025-10-14,null",
                    "T002,U002,B003,2025-10-10,2025-10-13"
            );
            Files.write(Paths.get(TRANSACTIONS_FILE), defaultTrans);
        } catch (IOException e) {
            System.err.println("Error creating default files: " + e.getMessage());
        }
    }

    private boolean login() {
        System.out.println("Please log in to continue.");
        int attempts = 3;
        while (attempts > 0) {
            System.out.print("Username: ");
            String usernameInput = scanner.nextLine().trim();
            System.out.print("Password: ");
            String passwordInput = scanner.nextLine().trim();

            User found = users.stream()
                    .filter(u -> u.getName().equalsIgnoreCase(usernameInput) && u.getPassword().equals(passwordInput))
                    .findFirst().orElse(null);

            if (found != null) {
                loggedInUser = found;
                System.out.println("Login successful! Welcome, " + found.getName() + ".");
                return true;
            } else {
                attempts--;
                System.out.println("Invalid username or password. Try again.");
                System.out.println("(Attempts left: " + attempts + ")");
            }
        }
        return false;
    }

    private void displayMenu() {
        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1. View All Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. Search Books (by title/author)");
            if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
                System.out.println("5. Users Management (Add/Update/Delete/Display)");
                System.out.println("6. Catalogue Management (Add/Update/Delete/Display)");
                System.out.println("7. Transactions (View All / By User / By Book)");
                System.out.println("8. Exit");
            } else {
                System.out.println("5. Exit");
            }
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();
            try {
                if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
                    switch (choice) {
                        case "1":
                            viewAllBooks();
                            break;
                        case "2":
                            borrowBook();
                            break;
                        case "3":
                            returnBook();
                            break;
                        case "4":
                            searchBooks();
                            break;
                        case "5":
                            usersManagement();
                            break;
                        case "6":
                            catalogueManagement();
                            break;
                        case "7":
                            transactionsMenu();
                            break;
                        case "8":
                            return;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } else {
                    switch (choice) {
                        case "1":
                            viewAllBooks();
                            break;
                        case "2":
                            borrowBook();
                            break;
                        case "3":
                            returnBook();
                            break;
                        case "4":
                            searchBooks();
                            break;
                        case "5":
                            return;
                        default:
                            System.out.println("Invalid choice.");
                    }
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
            System.out.println("\n----------------------------------------\nReturning to main menu...");
        }
    }

    private void viewAllBooks() {
        System.out.println("\nAll Books:");
        books.forEach(Book::displayBookDetails);
    }

    private void borrowBook() {
        System.out.print("Enter Book ID to borrow: ");
        String bookId = scanner.nextLine().trim();
        Book book = findBookById(bookId);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }
        if (!book.isAvailable()) {
            System.out.println("Book is currently unavailable.");
            return;
        }
        // limit to 3 books
        if (loggedInUser.getBorrowedBooks().size() >= 3) {
            System.out.println("You cannot borrow more than 3 books at once.");
            return;
        }
        // Update
        book.setAvailable(false);
        loggedInUser.borrowBook(bookId);
        String newTId = generateNextTransactionId();
        Transaction t = new Transaction(newTId, loggedInUser.getId(), bookId, LocalDate.now(), null);
        transactions.add(t);
        System.out.println("Book borrowed successfully! Transaction ID: " + newTId);
    }

    private void returnBook() {
        System.out.print("Enter Book ID to return: ");
        String bookId = scanner.nextLine().trim();
        if (!loggedInUser.getBorrowedBooks().contains(bookId)) {
            System.out.println("You have not borrowed this book.");
            return;
        }
        // find the active transaction for this user/book
        Optional<Transaction> opt = transactions.stream()
                .filter(tr -> tr.getUserId().equals(loggedInUser.getId()) &&
                        tr.getBookId().equals(bookId) &&
                        tr.getDateReturned() == null)
                .findFirst();
        if (!opt.isPresent()) {
            System.out.println("Transaction not found for this book/user.");
            return;
        }
        Transaction t = opt.get();
        t.setDateReturned(LocalDate.now());
        Book b = findBookById(bookId);
        if (b != null) b.setAvailable(true);
        loggedInUser.returnBook(bookId);
        System.out.println("Book returned successfully. Transaction updated: " + t.getTransactionId());
    }

    private void searchBooks() {
        System.out.print("Enter search keyword (title or author): ");
        String key = scanner.nextLine().trim().toLowerCase();
        List<Book> res = books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(key) || b.getAuthor().toLowerCase().contains(key))
                .collect(Collectors.toList());
        if (res.isEmpty()) {
            System.out.println("No books found for the keyword.");
        } else {
            res.forEach(Book::displayBookDetails);
        }
    }

    private void usersManagement() {
        while (true) {
            System.out.println("\nUsers Management");
            System.out.println("1. Add User");
            System.out.println("2. Update User");
            System.out.println("3. Delete User");
            System.out.println("4. Display Users");
            System.out.println("5. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            switch (c) {
                case "1":
                    addUser();
                    break;
                case "2":
                    updateUser();
                    break;
                case "3":
                    deleteUser();
                    break;
                case "4":
                    displayUsers();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void addUser() {
        System.out.print("Enter new User ID: ");
        String id = scanner.nextLine().trim();
        if (findUserById(id) != null) {
            System.out.println("User ID already exists.");
            return;
        }
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine().trim();
        System.out.print("Enter Role (user/admin): ");
        String role = scanner.nextLine().trim();
        users.add(new User(id, name, pass, role));
        System.out.println("User added.");
    }

    private void updateUser() {
        System.out.print("Enter User ID to update: ");
        String id = scanner.nextLine().trim();
        User u = findUserById(id);
        if (u == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.print("Enter new name (leave blank to keep): ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) u.name = name;
        System.out.print("Enter new password (leave blank to keep): ");
        String pass = scanner.nextLine().trim();
        if (!pass.isEmpty()) u.setPassword(pass);
        System.out.print("Enter new role (user/admin) (leave blank to keep): ");
        String role = scanner.nextLine().trim();
        if (!role.isEmpty()) u.setRole(role);
        System.out.println("User updated.");
    }

    private void deleteUser() {
        System.out.print("Enter User ID to delete: ");
        String id = scanner.nextLine().trim();
        User u = findUserById(id);
        if (u == null) {
            System.out.println("User not found.");
            return;
        }
        users.remove(u);
        System.out.println("User deleted.");
    }

    private void displayUsers() {
        System.out.println("\nUsers:");
        users.forEach(User::displayInfo);
    }

    private void catalogueManagement() {
        while (true) {
            System.out.println("\nCatalogue Management");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Display Books");
            System.out.println("5. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            switch (c) {
                case "1":
                    addBook();
                    break;
                case "2":
                    updateBook();
                    break;
                case "3":
                    deleteBook();
                    break;
                case "4":
                    viewAllBooks();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void addBook() {
        System.out.print("Enter new Book ID: ");
        String id = scanner.nextLine().trim();
        if (findBookById(id) != null) {
            System.out.println("Book ID already exists.");
            return;
        }
        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine().trim();
        books.add(new Book(id, title, author, true));
        System.out.println("Book added.");
    }

    private void updateBook() {
        System.out.print("Enter Book ID to update: ");
        String id = scanner.nextLine().trim();
        Book b = findBookById(id);
        if (b == null) {
            System.out.println("Book not found.");
            return;
        }
        System.out.print("Enter new title (leave blank to keep): ");
        String title = scanner.nextLine().trim();
        if (!title.isEmpty()) b.setTitle(title);
        System.out.print("Enter new author (leave blank to keep): ");
        String author = scanner.nextLine().trim();
        if (!author.isEmpty()) b.setAuthor(author);
        System.out.print("Set availability (true/false) (leave blank to keep): ");
        String av = scanner.nextLine().trim();
        if (!av.isEmpty()) b.setAvailable(Boolean.parseBoolean(av));
        System.out.println("Book updated.");
    }

    private void deleteBook() {
        System.out.print("Enter Book ID to delete: ");
        String id = scanner.nextLine().trim();
        Book b = findBookById(id);
        if (b == null) {
            System.out.println("Book not found.");
            return;
        }
        books.remove(b);
        System.out.println("Book deleted.");
    }

    private void transactionsMenu() {
        while (true) {
            System.out.println("\nTransactions Menu");
            System.out.println("1. View All Transactions");
            System.out.println("2. View Transactions By User");
            System.out.println("3. View Transactions By Book");
            System.out.println("4. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            switch (c) {
                case "1":
                    viewAllTransactions();
                    break;
                case "2":
                    viewTransactionsByUser();
                    break;
                case "3":
                    viewTransactionsByBook();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllTransactions() {
        System.out.println("\nAll Transactions:");
        transactions.forEach(Transaction::displayTransaction);
    }

    private void viewTransactionsByUser() {
        System.out.print("Enter User ID: ");
        String uid = scanner.nextLine().trim();
        transactions.stream()
                .filter(t -> t.getUserId().equals(uid))
                .forEach(Transaction::displayTransaction);
    }

    private void viewTransactionsByBook() {
        System.out.print("Enter Book ID: ");
        String bid = scanner.nextLine().trim();
        transactions.stream()
                .filter(t -> t.getBookId().equals(bid))
                .forEach(Transaction::displayTransaction);
    }

    private User findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    private Book findBookById(String id) {
        return books.stream().filter(b -> b.getBookId().equals(id)).findFirst().orElse(null);
    }

    private String generateNextTransactionId() {
        int max = 0;
        for (Transaction t : transactions) {
            String tid = t.getTransactionId();
            try {
                String num = tid.replaceAll("[^0-9]", "");
                int n = Integer.parseInt(num);
                if (n > max) max = n;
            } catch (Exception e) {
                System.out.println("No Transaction Found!");
            }
        }
        int next = max + 1;
        return String.format("T%03d", next);
    }
}