package peter.finance_calendar.models;

import java.time.LocalDate;
import java.util.List;

public class AccountInfo {
    private User user;
    private List<Expense> expenses;
    private List<Debt> debts;
    private int month = 12;
    private int year = 2024;

    public AccountInfo(User user) {
        LocalDate now = LocalDate.now();
        this.user = user;
        this.month = now.getMonthValue();
        this.year = now.getYear();
        
    }

    // Getters
    public User getUser() { return user; }
    public List<Expense> getExpenses() { return expenses; }
    public List<Debt> getDebts() { return debts; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    // Setters 
    public void setUser(User user) { this.user = user; }
    public void setExpenses(List<Expense> expenses) { this.expenses = expenses; }
    public void setDebts(List<Debt> debts) { this.debts = debts; }
    public void setMonth(int month) { this.month = month; }
    public void setYear(int year) { this.year = year; }
}
