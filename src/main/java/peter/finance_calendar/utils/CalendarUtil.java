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

    public List<Event> calculateEventTotals(List<Event> events) {

        return events;
    }

    public int[][] getMonthCalendar(Calendar cal) {
        int[][] monthDays = new int[6][7];
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        int day = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < firstDayOfWeek) {
                    monthDays[i][j] = 0;
                } else if (day <= daysInMonth) {
                    monthDays[i][j] = day++;
                } else {
                    monthDays[i][j] = 0;
                }
            }
        }
        return monthDays;
    }

    public List<List<Day>> getWeeks(int month, int year, List<Event> events) {
        System.out.println("about to create a Calendar");
        Calendar cal = Calendar.getInstance();
        System.out.println("got calendar instance");
        cal.set(year, month, 1); // Note: Month is 0-based in Java, so 10 = November
        System.out.println("set calendar");
        List<List<Day>> weeks = new ArrayList<>();
        int[][] monthDays = this.getMonthCalendar(cal);

        String[] DOW = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        Date today = new Date(System.currentTimeMillis());

        for (int[] week : monthDays) {
            List<Day> weekDays = new ArrayList<>();
            for (int date : week) {
                if (date != 0) {
                    Date thisDate = new Date(year, month, date);
                    cal.set(Calendar.DAY_OF_MONTH, date);
                    Day day = new Day();
                    day.setDate(date);
                    day.setDay(DOW[cal.get(Calendar.DAY_OF_WEEK) - 1]);
                    List<Event> dayEvents = events.stream().filter(e -> e.getDate().getDate() == date).toList();
                    day.setEvents(dayEvents);
                    day.setYear(year);
                    day.setMonth(month);
                    day.setToday(thisDate.equals(today));
                    day.setTodayOrLater(thisDate.equals(today) || thisDate.after(today));
                    weekDays.add(day);
                }
            }
            if (!weekDays.isEmpty()) {
                weeks.add(weekDays);
            }
        }

        return weeks;
    }
}