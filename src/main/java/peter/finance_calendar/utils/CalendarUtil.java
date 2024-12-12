package peter.finance_calendar.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import peter.finance_calendar.models.Day;
import peter.finance_calendar.models.Event;

@Component
public class CalendarUtil {

    public List<List<Day>> getWeeks(int month, int year, List<Event> events) {
        List<List<Day>> weeks = new ArrayList<>();
        String[] DOW = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Set the calendar to the first day of the previous month
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        // Get today's date for comparison
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();

        // Generate days covering the previous month, current month, and next month
        List<Day> currentWeek = new ArrayList<>();
        while (weeks.size() < 6) {
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0, Monday = 1, etc.

            Day day = new Day();
            day.setDate(dayOfMonth);
            day.setDay(DOW[dayOfWeek]);
            day.setYear(cal.get(Calendar.YEAR));
            day.setMonth(cal.get(Calendar.MONTH));

            Date thisDate = cal.getTime();
            List<Event> dayEvents = events.stream()
                    .filter(e -> isSameDay(e.getDate(), thisDate))
                    .toList();
            day.setEvents(dayEvents);
            day.setToday(thisDate.equals(today));
            day.setTodayOrLater(thisDate.equals(today) || thisDate.after(today));

            currentWeek.add(day);

            // If the current week has 7 days, add it to the weeks list and start a new week
            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }

            // Move to the next day
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return weeks;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        return cal1.equals(cal2);
    }
}
