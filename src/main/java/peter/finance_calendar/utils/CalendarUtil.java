package peter.finance_calendar.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
        System.out.println("\t\t" + year + ": " + month);

        List<List<Day>> weeks = new ArrayList<>();
        String[] DOW = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Set the date to the first day of the current month
        LocalDate cal = LocalDate.of(year, month, 1);

        // Adjust date to the nearest previous Sunday
        cal = cal.minusDays(cal.getDayOfWeek().getValue() % 7);

        // Get today's date for comparison
        LocalDate today = LocalDate.now();

        List<Day> currentWeek = new ArrayList<>();

        while (weeks.size() < 6 || cal.getMonthValue() == month) {
            int dayOfMonth = cal.getDayOfMonth();
            int dayOfWeekIndex = cal.getDayOfWeek().getValue() % 7; // Sunday = 0, Monday = 1, etc.

            boolean isToday = cal.isEqual(today);
            boolean isAfterToday = isToday || cal.isAfter(today);

            Day day = new Day();
            day.setDate(dayOfMonth);
            day.setDay(DOW[dayOfWeekIndex]);
            day.setYear(cal.getYear());
            day.setMonth(cal.getMonthValue());

            // Check for events on this day
            List<Event> dayEvents = new ArrayList<>();
            for (Event event : events) {
                LocalDate eventDate = event.getDate();
                if (cal.isEqual(eventDate)) {
                    day.hasEvents = true;
                    dayEvents.add(event);
                    day.setTotal(event.getTotal());
                }
            }
            day.setEvents(dayEvents);
            day.setToday(isToday);
            day.setTodayOrLater(isAfterToday);

            if (isToday) {
                day.setTotal(user.getCheckingBalance());
            } else if (!day.hasEvents) {
                day.setTotal(0.0);
            }

            currentWeek.add(day);

            // If the week has 7 days, add it to the list of weeks
            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }

            // Move to the next day
            cal = cal.plusDays(1);
        }

        return weeks;
    }

    public List<Event> generateEvents(User user, List<Expense> expenses) {
        List<Event> events = new ArrayList<>();

        for (Expense expense : expenses) {
            String frequency = expense.getFrequency();

            LocalDate start = expense.getStartdate();
            LocalDate end = expense.getRecurrenceenddate();

            String userId = expense.getUserId();
            String recurrenceid = UUID.randomUUID().toString();

            while (!start.isAfter(end)) {
                Event event = new Event(
                    UUID.randomUUID().toString(),
                    recurrenceid,
                    expense.getName(),
                    start,
                    end,
                    expense.getAmount(),
                    0.0,
                    expense.getAmount(),
                    false,
                    frequency,
                    userId
                );

                events.add(event);

                // Increment `start` based on the frequency
                switch (frequency) {
                    case "daily" -> start = start.plusDays(1);
                    case "weekly" -> start = start.plusWeeks(1);
                    case "biweekly" -> start = start.plusWeeks(2);
                    case "monthly" -> start = start.plusMonths(1);
                    case "yearly" -> start = start.plusYears(1);
                }
            }
        }

        events = this.calculateTotals(user, events);

        return events;
    }

    public List<Event> calculateTotals(User user, List<Event> events) {
        double total = user.getCheckingBalance();
        LocalDate today = LocalDate.now().atStartOfDay().toLocalDate();

        events = events.stream()
                .sorted(Comparator.comparing(event -> event.getDate()))
                .collect(Collectors.toList());

        boolean started = false;
        for (Event event : events) {
            LocalDate eventDate = event.getDate().atStartOfDay().toLocalDate();
            if ((!started && eventDate.isEqual(today)) || (!started && eventDate.isAfter(today))) {
                started = true;
            }
            if (started) {
                if (!event.getExclude()) {
                    total += event.getAmount();
                }
                event.setTotal(total);
            } else {
                event.setTotal(0.0);
            }
        }
        return events;
    }
}
