package peter.finance_calendar.models;

import java.util.List;

public final class Day {
	private Integer date;
	private String day;
	private List<Event> events;
	public boolean hasEvents = false;
	private Integer year;
	private Integer month;
	private Boolean todayOrLater;
	private Boolean today;
	private Double total;

	public Day() {
		date = 0;
		day = "";
		events = null;
		year = null;
		month = null;
		todayOrLater = false;
		today = false;
		total = 0.0;
	}

	public Day(Integer date, String day, List<Event> events, Integer year, Integer month) {
		this.date = date;
		this.day = day;
		this.events = events;
		this.year = year;
		this.month = month;
	}

	public Boolean getTodayOrLater() {
		return todayOrLater;
	}

	public void setTodayOrLater(Boolean todayOrLater) {
		this.todayOrLater = todayOrLater;
	}

	public Boolean getToday() {
		return today;
	}

	public void setToday(Boolean today) {
		this.today = today;
	}

	public Integer getDate() {
		return date;
	}

	public void setDate(Integer date) {
		this.date = date;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}
}