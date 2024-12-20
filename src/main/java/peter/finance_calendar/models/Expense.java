package peter.finance_calendar.models;

import java.time.LocalDate;

public final class Expense {
    private String id;
    private String user_id;
    private String name;
    private Double amount;
    private LocalDate recurrenceenddate;
    private LocalDate startdate;
    private String frequency;

    public Expense() {}

    public Expense(String id, String user_id, String name, Double amount, LocalDate recurrenceenddate, LocalDate startdate, String frequency) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.amount = amount;
        this.recurrenceenddate = recurrenceenddate;
        this.startdate = startdate;
        this.frequency = frequency;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDate getRecurrenceenddate() {
        return recurrenceenddate;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public String getFrequency() {
        return frequency;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setRecurrenceenddate(LocalDate recurrenceenddate) {
        this.recurrenceenddate = recurrenceenddate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}