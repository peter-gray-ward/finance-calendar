package peter.finance_calendar.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Event(
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
    }
}

