package peter.finance_calendar.models;

import java.util.Date;

public class Expense {
    private String id;
    private String user_id;
    private String name;
    private Double amount;
    private Date recurrenceenddate;
    private Date startdate;
    private String frequency;

    public Expense(String id, String user_id, String name, Double amount, Date recurrenceenddate, Date startdate, String frequency) {
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

    public Date getRecurrenceenddate() {
        return recurrenceenddate;
    }

    public Date getStartdate() {
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

    public void setRecurrenceenddate(Date recurrenceenddate) {
        this.recurrenceenddate = recurrenceenddate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}