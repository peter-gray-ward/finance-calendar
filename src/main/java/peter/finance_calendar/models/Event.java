package peter.finance_calendar.models;

import java.lang.reflect.Field;
import java.time.LocalDate;

public final class Event {
	private String id;
	private String recurrenceid;
	private String summary;
	private LocalDate date;
	private LocalDate recurrenceenddate;
	private Double amount;
	private Double total;
	private Double balance;
	private Boolean exclude;
	private String frequency;
	private String user_id;

	public Event(
		String id,
		String recurrenceid,
		String summary,
		LocalDate date,
		LocalDate recurrenceenddate,
		Double amount,
		Double total,
		Double balance,
		Boolean exclude,
		String frequency,
		String user_id
	) {
		this.id = id;
		this.recurrenceid = recurrenceid;
		this.summary = summary;
		this.date = date;
		this.recurrenceenddate = recurrenceenddate;
		this.amount = amount;
		this.total = total;
		this.balance = balance;
		this.exclude = exclude;
		this.frequency = frequency;
		this.user_id = user_id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRecurrenceid() {
		return recurrenceid;
	}

	public void setRecurrenceid(String recurrenceid) {
		this.recurrenceid = recurrenceid;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDate getRecurrenceenddate() {
		return recurrenceenddate;
	}

	public void setRecurrenceenddate(LocalDate recurrenceenddate) {
		this.recurrenceenddate = recurrenceenddate;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Boolean getExclude() {
		return exclude;
	}

	public void setExclude(Boolean exclude) {
		this.exclude = exclude;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getUserId() {
		return user_id;
	}

	public void setUserId(String user_id) {
		this.user_id = user_id;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("Event(\n");
		Class<?> cls = this.getClass();
		try {
			for (Field f : cls.getDeclaredFields()) {
				f.setAccessible(true); // Ensure private fields can be accessed
				str.append(f.getName())
				.append(": ")
				.append(f.get(this)) // Get the value of the field
				.append("\n");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return "Error generating toString: " + e.getMessage();
		}
		str.append(")");
		return str.toString();
	}

}