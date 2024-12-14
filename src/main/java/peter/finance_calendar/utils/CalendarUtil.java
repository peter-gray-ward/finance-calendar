package peter.finance_calendar.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import peter.finance_calendar.models.Day;
import peter.finance_calendar.models.Event;
import peter.finance_calendar.models.Expense;
import peter.finance_calendar.models.User;

@Component
public class CalendarUtil {

    public List<List<Day>> getWeeks(User user, int month, int year, List<Event> events) {
        System.out.println("\n getting weeks for " + year + ": " + month + "\n");


        List<List<Day>> weeks = new ArrayList<>();
        String[] DOW = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Set the calendar to the first day of the previous month
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        // Get today's date for comparison
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        // Generate days covering the previous month, current month, and next month
        List<Day> currentWeek = new ArrayList<>();
        double total = user.getCheckingBalance();
        while (weeks.size() < 12) {
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0, Monday = 1, etc.

            boolean isToday = cal.equals(todayCal);
            boolean isAfterToday = isToday || cal.after(todayCal);


            Day day = new Day();
            day.setDate(dayOfMonth);
            day.setDay(DOW[dayOfWeek]);
            day.setYear(cal.get(Calendar.YEAR));
            day.setMonth(cal.get(Calendar.MONTH));

            if (isToday) {
                day.setTotal(total);
            }

            List<Event> dayEvents = new ArrayList<>();
            for (Event event : events) {
                Date date = event.getDate();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                if (c.equals(cal)) {
                    day.hasEvents = true;
                    dayEvents.add(event);
                    if (isAfterToday) {
                        total += event.getAmount();
                    }
                }
            }
            day.setEvents(dayEvents);
            day.setToday(isToday);
            day.setTodayOrLater(isAfterToday);
            day.setTotal(isAfterToday ? total : 0);

            currentWeek.add(day);

            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }

            // Move to the next day
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return weeks;
    }

    public List<Event> generateEvents(User user, List<Expense> expenses) {
        List<Event> events = new ArrayList<>();

        for (Expense expense : expenses) {
            String frequency = expense.getFrequency();

            Calendar start = Calendar.getInstance();
            start.setTime(expense.getStartdate());

            Calendar end = Calendar.getInstance();
            end.setTime(expense.getRecurrenceenddate());
            
            String userId = expense.getUserId();
            String recurrenceid = UUID.randomUUID().toString();

            while (start.before(end) || start.equals(end)) {
                Event event = new Event(
                    UUID.randomUUID().toString(), 
                    recurrenceid, 
                    expense.getName(),
                    start.getTime(), 
                    end.getTime(), 
                    expense.getAmount(), 
                    0.0, 
                    expense.getAmount(), 
                    false,
                    frequency,
                    userId
                );
                

                events.add(event);

                switch (frequency) {
                    case "daily" -> start.add(Calendar.DAY_OF_YEAR, 1);
                    case "weekly" -> start.add(Calendar.WEEK_OF_YEAR, 1);
                    case "biweekly" -> start.add(Calendar.WEEK_OF_YEAR, 2);
                    case "monthly" -> start.add(Calendar.MONTH, 1);
                    case "yearly" -> start.add(Calendar.YEAR, 1);
                }
            }
        }

        events = this.calculateTotals(user, events);

        return events;
    }

    public List<Event> calculateTotals(User user, List<Event> events) {
        double total = user.getCheckingBalance();
        Calendar today = Calendar.getInstance();
        events = events.stream()
               .sorted(Comparator.comparing(Event::getDate))
               .collect(Collectors.toList());
        boolean started = false;
        for (Event event : events) {
            Date date = event.getDate();
            Calendar eventDate = Calendar.getInstance();
            eventDate.setTime(date);
            if (eventDate.before(today)) {
                continue;
            }
            if (started == false) {
                started = true;
                event.setTotal(total);
                total -= event.getAmount();
                continue;
            }
            event.setTotal(total);
            total -= event.getAmount();
        }
        return events;
    }
}
