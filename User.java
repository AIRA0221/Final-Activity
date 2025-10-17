import java.util.ArrayList;
import java.util.List;

public class User extends Person {
    private String password;
    private String role; // "admin" or "user"
    private List<String> borrowedBooks;

    public User(String id, String name, String password, String role) {
        super(id, name);
        this.password = password;
        this.role = role;
        this.borrowedBooks = new ArrayList<>();
    }

    @Override
    public void displayInfo() {
        System.out.println("UserID: " + id + " | Name: " + name + " | Role: " + role + " | Borrowed: " + borrowedBooks);
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public List<String> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrowBook(String bookId) {
        borrowedBooks.add(bookId);
    }

    public void returnBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}