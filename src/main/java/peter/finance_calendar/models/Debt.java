package peter.finance_calendar.models;

public class Debt {
    public String id;
    public String user_id;
    public String creditor;
    public Double balance;
    public Double interest;
    public String account_number;
    public String link;
    public String recurrenceid;

    public Debt(String id, String user_id, String creditor, Double balance, Double interest, String account_number, String link, String recurrenceid) {
        this.id = id;
        this.user_id = user_id;
        this.creditor = creditor;
        this.balance = balance;
        this.interest = interest;
        this.account_number = account_number;
        this.link = link;
        this.recurrenceid = recurrenceid;
    }
}
