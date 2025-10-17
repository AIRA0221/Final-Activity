import java.time.LocalDate;

public class Transaction {
    private String transactionId;
    private String userId;
    private String bookId;
    private LocalDate dateBorrowed;
    private LocalDate dateReturned; // null if not yet returned

    public Transaction(String transactionId, String userId, String bookId, LocalDate dateBorrowed, LocalDate dateReturned) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.dateBorrowed = dateBorrowed;
        this.dateReturned = dateReturned;
    }

    public void displayTransaction() {
        System.out.printf("%s | User: %s | Book: %s | Borrowed: %s | Returned: %s%n",
                transactionId,
                userId,
                bookId,
                dateBorrowed != null ? dateBorrowed.toString() : "null",
                dateReturned != null ? dateReturned.toString() : "null");
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public LocalDate getDateBorrowed() {
        return dateBorrowed;
    }
     public LocalDate getDateReturned() {
        return dateReturned;
    }

    public void setDateReturned(LocalDate date) {
        dateReturned = date;
    }
}
