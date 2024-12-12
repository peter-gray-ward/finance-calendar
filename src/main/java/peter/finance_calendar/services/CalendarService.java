package peter.finance_calendar.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import peter.finance_calendar.models.Event;
import peter.finance_calendar.models.User;
import peter.finance_calendar.utils.CalendarUtil;

@Service
public class CalendarService {

    private CalendarUtil calendarUtil;

    public CalendarService(CalendarUtil calendarUtil) {
        this.calendarUtil = calendarUtil;
    }
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Event> selectEvents(User user, Integer year, Integer month) {
        try {
            String sql = "SELECT *"
                + " FROM public.event"
                + " WHERE user_id = ?"
                + " AND ("
                + "     date >= DATE_TRUNC('month', DATE '" + year + "-" + month + "-01') - INTERVAL '1 month'"
                + "     AND date < DATE_TRUNC('month', DATE '" + year + "-" + month + "-01') + INTERVAL '2 months'"
                + " )";
            System.out.println(sql);
            List<Event> events = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Event(
                    rs.getString("id"),
                    rs.getString("recurrenceid"),
                    rs.getString("summary"),
                    rs.getDate("date"),
                    rs.getDate("recurrenceenddate"),
                    rs.getDouble("amount"),
                    rs.getDouble("total"),
                    rs.getDouble("balance"),
                    rs.getBoolean("exclude"),
                    rs.getString("frequency"),
                    rs.getString("user_id")
                ),
                UUID.fromString(user.getId())
            );

            // calculate totals
            System.out.println(events.size() +  " events found");

            return events;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}