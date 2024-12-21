package peter.finance_calendar.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.annotation.PostConstruct;
import peter.finance_calendar.models.AccountInfo;
import peter.finance_calendar.models.Day;
import peter.finance_calendar.models.Event;
import peter.finance_calendar.models.EventRowMapper;
import peter.finance_calendar.models.Expense;
import peter.finance_calendar.models.ServiceResult;
import peter.finance_calendar.models.SyncData;
import peter.finance_calendar.models.User;
import peter.finance_calendar.utils.CalendarUtil;

@Service
public class CalendarService {

    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CalendarUtil calendarUtil;
    private AccountService accountService;

    private String cachedCalendarTemplate;
    private String cachedEventTemplate;

    public CalendarService(CalendarUtil calendarUtil, AccountService accountService) {
        this.calendarUtil = calendarUtil;
        this.accountService = accountService;
    }
    
    @PostConstruct
    public void loadTemplate() throws IOException {
        String templatePath = "src/main/resources/templates/calendar.html";
        String rawTemplate = Files.readString(Path.of(templatePath));
        cachedCalendarTemplate = rawTemplate;

        templatePath = "src/main/resources/templates/event.html";
        rawTemplate = Files.readString(Path.of(templatePath));
        cachedEventTemplate = rawTemplate;
    }

    public String getCalendarTemplate() {
        return cachedCalendarTemplate;
    }

    public ServiceResult<Boolean> generateEventsFromExpenses(User user) {
        try {
            List<Expense> expenses = jdbcTemplate.query(
                "SELECT * FROM public.expense"
                + " WHERE user_id = ?",
                (rs, rowNum) -> new Expense(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getDouble("amount"),
                    rs.getObject("recurrenceenddate", LocalDate.class),
                    rs.getObject("startdate", LocalDate.class),
                    rs.getString("frequency")
                ),
                UUID.fromString(user.getId())
            );

            List<Event> events = calendarUtil.generateEvents(user, expenses);

            if (events.size() < 1) {
                return new ServiceResult("error", null, "Exception generating events");
            }

            jdbcTemplate.update("DELETE FROM public.event WHERE user_id = ?", UUID.fromString(user.getId()));

            String sql = "INSERT INTO public.event" +
                     " (id, recurrenceid, summary, date, recurrenceenddate, amount, total, balance, exclude, frequency, user_id)" +
                     " VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS bit), ?, ?)";

            int[][] batchUpdates = jdbcTemplate.batchUpdate(
                sql,
                events,
                events.size(),
                (ps, event) -> {
                    ps.setObject(1, UUID.fromString(event.getId()));
                    ps.setObject(2, UUID.fromString(event.getRecurrenceid()));
                    ps.setString(3, event.getSummary());
                    ps.setObject(4, event.getDate());
                    ps.setObject(5, event.getRecurrenceenddate());
                    ps.setDouble(6, event.getAmount());
                    ps.setDouble(7, event.getTotal());
                    ps.setDouble(8, event.getBalance());
                    ps.setString(9, event.getExclude() ? "1" : "0");
                    ps.setString(10, event.getFrequency());
                    ps.setObject(11, UUID.fromString(event.getUserId()));
                }
            );

            return new ServiceResult<>("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>("error", e, false);
        }
    }

    public List<Event> selectEvents(User user, Integer year, Integer month) {
        try {
            String sql = "SELECT *"
                + " FROM public.event"
                + " WHERE user_id = ?"
                + " AND ("
                + "     date >= DATE_TRUNC('month', DATE '" + year + "-" + month + "-01') - INTERVAL '1 month'"
                + "     AND date < DATE_TRUNC('month', DATE '" + year + "-" + month + "-01') + INTERVAL '2 months'"
                + " )";
            List<Event> events = jdbcTemplate.query(
                sql,
                new EventRowMapper(),
                UUID.fromString(user.getId())
            );


            return events;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateCalendarFragment(User user, Integer year, Integer month) {
        try {
            TemplateEngine templateEngine = new TemplateEngine();
            Context context = new Context();
            List<Event> events = this.selectEvents(user, year, month);
            AccountInfo info = accountService.getAccountInfo(user);

            if (info == null) {
                return "error";
            }
            SyncData data = new SyncData();
            
            List<List<Day>> weeks = calendarUtil.getWeeks(user, month, year, events);

            context.setVariable("info", info);
            context.setVariable("title", "Finance Calendar");
            context.setVariable("name", user.getName());
            context.setVariable("events", events);
            context.setVariable("data", data);
            context.setVariable("weeks", weeks);

            return templateEngine.process(cachedCalendarTemplate, context);
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception generating calendar: " + e.getMessage();
        }
    }

    public String generateEventFragment(Event event) {
        try {
            TemplateEngine templateEngine = new TemplateEngine();
            Context context = new Context();
            
            context.setVariable("event", event);

            return templateEngine.process(cachedEventTemplate, context);
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception generating event: " + e.getMessage();
        }
    }

    public ServiceResult<Event> getEvent(User user, String eventId) {
        try {
            Event event = jdbcTemplate.queryForObject(
                "SELECT *"
                + " FROM public.event"
                + " WHERE user_id = ?"
                + " AND id = ?", 
                new EventRowMapper(),
                UUID.fromString(user.getId()),
                UUID.fromString(eventId)
            );
            return new ServiceResult<>("success", event);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>("error", null);
        }
    }

    public ServiceResult<Event> addEvent(User user, int year, int month, int dayOfMonth) {
        try {
            UUID eventId = UUID.randomUUID();
            UUID recurrenceId = UUID.randomUUID();
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            jdbcTemplate.update(
                "INSERT INTO public.event" +
                " (id, recurrenceid, summary, date, recurrenceenddate, amount, total, balance, exclude, frequency, user_id)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS bit), ?, ?)",
                eventId, recurrenceId, "", date, date, 0.0, 0.0, 0.0, "0", "monthly", UUID.fromString(user.getId())
            );
            Event event = jdbcTemplate.queryForObject(
                "SELECT *"
                + " FROM public.event"
                + " WHERE id = ?",
                new EventRowMapper(),
                eventId
            );
            return new ServiceResult("success", event);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>("error", null);
        }
    }
   
    public void updateEventTotals(User user) {
        try {
            List<Event> events = jdbcTemplate.query(
                "SELECT *"
                + " FROM public.event"
                + " WHERE user_id = ?",
                new EventRowMapper(),
                UUID.fromString(user.getId())
            );
            events = calendarUtil.calculateTotals(user, events);
            jdbcTemplate.batchUpdate(
                "UPDATE public.event"
                + " SET total = ?"
                + " WHERE user_id = ? AND id = ?",
                events,
                events.size(),
                (ps, event) -> {
                    ps.setDouble(1, event.getTotal());
                    ps.setObject(2, UUID.fromString(event.getUserId()));
                    ps.setObject(3, UUID.fromString(event.getId()));
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ServiceResult<Boolean> cludeEvent(User user, String id) {
        try {
            jdbcTemplate.update(
                "UPDATE public.event"
                + " SET exclude = exclude # B'1'"
                + " WHERE user_id = ?"
                + " AND id = ?",
                UUID.fromString(user.getId()),
                UUID.fromString(id)
            );
            this.updateEventTotals(user);
            return new ServiceResult<>("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>("error", e, false);
        }
    }
    
    public ServiceResult<Boolean> cludeAllEventTheseEvents(User user, String recurrenceid) {
        try {
            jdbcTemplate.update(
                "UPDATE public.event"
                + " SET exclude = exclude # B'1'"
                + " WHERE user_id = ?"
                + " AND recurrenceid = ?",
                UUID.fromString(user.getId()),
                UUID.fromString(recurrenceid)
            );
            this.updateEventTotals(user);
            return new ServiceResult<>("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>("error", e, false);
        }
    }

    public ServiceResult<Event> updateEvent(User user, Event event) {
        try {
            
            jdbcTemplate.update(
                "UPDATE public.event"
                + " SET summary = ?, date = ?, recurrenceenddate = ?, amount = ?, total = ?, balance = ?, frequency = ?"
                + " WHERE user_id = ?"
                + " AND id = ?",
                event.getSummary(),
                event.getDate(),
                event.getRecurrenceenddate(),
                event.getAmount(),
                event.getTotal(),
                event.getBalance(),
                event.getFrequency(),
                UUID.fromString(user.getId()),
                UUID.fromString(event.getId())
            );
            this.updateEventTotals(user);
            return new ServiceResult<>("success", event);
        } catch (Exception e) {
            return new ServiceResult<>("error", null);
        }
    }


    public ServiceResult<Event> updateAllTheseEvents(User user, Event event) {
        try {
            // Fetch current event from the database
            Event currentEvent = jdbcTemplate.queryForObject(
                "SELECT id, recurrenceid, summary, date, recurrenceenddate, amount, total, balance, exclude, frequency, user_id"
                + " FROM public.event WHERE id = ?",
                new EventRowMapper(true),
                UUID.fromString(event.getId())
            );

            LocalDate currentDate = currentEvent.getDate().atStartOfDay().toLocalDate();
            LocalDate newDate = event.getDate().atStartOfDay().toLocalDate();
            LocalDate currentRecurrenceEndDate = currentEvent.getRecurrenceenddate().atStartOfDay().toLocalDate();
            LocalDate newRecurrenceEndDate = event.getRecurrenceenddate().atStartOfDay().toLocalDate();

            // Check if dates are different and update accordingly
            if (!currentDate.isEqual(newDate)) {
                jdbcTemplate.update(
                    "WITH diff AS ("
                    + "    SELECT (CAST(? AS TIMESTAMP) - CAST(? AS TIMESTAMP)) AS date_diff"
                    + ") "
                    + "UPDATE public.event "
                    + "SET summary = ?, "
                    + "    date = date + diff.date_diff, "
                    + "    recurrenceenddate = recurrenceenddate + diff.date_diff, "
                    + "    amount = ?, total = ?, balance = ?, frequency = ? "
                    + "FROM diff "
                    + "WHERE user_id = ? AND recurrenceid = ?",
                    newDate,                // New date
                    currentDate,            // Current date
                    event.getSummary(),
                    event.getAmount(),
                    event.getTotal(),
                    event.getBalance(),
                    event.getFrequency(),
                    UUID.fromString(user.getId()),
                    UUID.fromString(event.getRecurrenceid())
                );
            }

            // Handle changes to recurrence end date
            if (!currentRecurrenceEndDate.isEqual(newRecurrenceEndDate)) {
                if (newRecurrenceEndDate.isBefore(currentRecurrenceEndDate)) {
                    jdbcTemplate.update(
                        "DELETE FROM public.event "
                        + "WHERE user_id = ? "
                        + "AND recurrenceid = ? "
                        + "AND date > ?",
                        UUID.fromString(user.getId()),
                        UUID.fromString(event.getRecurrenceid()),
                        newRecurrenceEndDate
                    );
                } else if (newRecurrenceEndDate.isAfter(currentRecurrenceEndDate)) {
                    LocalDate latestDate = currentRecurrenceEndDate;
                    while (latestDate.isBefore(newRecurrenceEndDate)) {
                        String sql = "INSERT INTO public.event "
                            + "(id, recurrenceid, summary, date, recurrenceenddate, amount, total, balance, exclude, frequency, user_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS bit), ?, ?)";
                        jdbcTemplate.update(
                            sql,
                            UUID.randomUUID(),
                            UUID.fromString(event.getRecurrenceid()),
                            event.getSummary(),
                            latestDate,
                            newRecurrenceEndDate,
                            event.getAmount(),
                            event.getTotal(),
                            event.getBalance(),
                            event.getExclude(),
                            event.getFrequency(),
                            UUID.fromString(user.getId())
                        );

                        // Increment `latestDate` based on the frequency
                        switch (event.getFrequency()) {
                            case "daily" -> latestDate = latestDate.plusDays(1);
                            case "weekly" -> latestDate = latestDate.plusWeeks(1);
                            case "biweekly" -> latestDate = latestDate.plusWeeks(2);
                            case "monthly" -> latestDate = latestDate.plusMonths(1);
                            case "yearly" -> latestDate = latestDate.plusYears(1);
                        }
                    }
                }
            }

            return new ServiceResult<>("success", event);
        } catch (Exception e) {
            return new ServiceResult<>("error", null);
        }
    }

    public ServiceResult<?> deleteEvent(User user, String id) {
        try {
            jdbcTemplate.update(
                "DELETE from public.event"
                + " WHERE user_id = ?"
                + " AND id = ?",
                UUID.fromString(user.getId()),
                UUID.fromString(id)
            );
            return new ServiceResult<>("success");
        } catch (Exception e) {
            return new ServiceResult<>("error", null);
        }
    }

    public ServiceResult<Event> deleteAllTheseEvents(User user, String recurrenceid) {
        try {
            jdbcTemplate.update(
                "DELETE from public.event"
                + " WHERE user_id = ?"
                + " AND recurrenceid = ?",
                UUID.fromString(user.getId()),
                UUID.fromString(recurrenceid)
            );
            return new ServiceResult<>("success");
        } catch (Exception e) {
            return new ServiceResult<>("error", null);
        }
    }
}