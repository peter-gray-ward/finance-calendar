package peter.finance_calendar.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.ZoneId;
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

    public CalendarService(CalendarUtil calendarUtil, AccountService accountService) {
        this.calendarUtil = calendarUtil;
        this.accountService = accountService;
    }
    
    @PostConstruct
    public void loadTemplate() throws IOException {
        String templatePath = "src/main/resources/templates/calendar.html";
        String rawTemplate = Files.readString(Path.of(templatePath));
        cachedCalendarTemplate = rawTemplate;
    }

    public String getCalendarTemplate() {
        return cachedCalendarTemplate;
    }

    public ServiceResult generateEventsFromExpenses(User user) {
        try {
            List<Expense> expenses = jdbcTemplate.query(
                "SELECT * FROM public.expense"
                + " WHERE user_id = ?",
                (rs, rowNum) -> new Expense(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getDouble("amount"),
                    rs.getDate("recurrenceenddate"),
                    rs.getDate("startdate"),
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
                    ps.setDate(4, Date.valueOf(event.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                    ps.setDate(5, Date.valueOf(event.getRecurrenceenddate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                    ps.setDouble(6, event.getAmount());
                    ps.setDouble(7, event.getTotal());
                    ps.setDouble(8, event.getBalance());
                    ps.setString(9, event.getExclude() ? "1" : "0");
                    ps.setString(10, event.getFrequency());
                    ps.setObject(11, UUID.fromString(event.getUserId()));
                }
            );

            return new ServiceResult("success");
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult("error", e, e.getMessage());
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

}