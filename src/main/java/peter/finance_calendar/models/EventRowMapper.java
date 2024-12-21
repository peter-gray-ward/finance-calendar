package peter.finance_calendar.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;


// id, recurrenceid, summary, date, recurrenceenddate, amount, total, balance, exclude, frequency, user_id
public final class EventRowMapper implements RowMapper<Event> {
    private boolean print = false;
    public EventRowMapper() {
        super();
    }
    public EventRowMapper(boolean print) {
        super();
        this.print = print;
    }
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event(
            rs.getString("id"),
            rs.getString("recurrenceid"),
            rs.getString("summary"),
            rs.getObject("date", LocalDate.class),
            rs.getObject("recurrenceenddate", LocalDate.class),
            rs.getDouble("amount"),
            rs.getDouble("total"),
            rs.getDouble("balance"),
            rs.getBoolean("exclude"),
            rs.getString("frequency"),
            rs.getString("user_id")
        );
        return event;
    }
}

