package peter.finance_calendar.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import peter.finance_calendar.models.AccountInfo;
import peter.finance_calendar.models.Debt;
import peter.finance_calendar.models.Expense;
import peter.finance_calendar.models.User;

@Service
public class AccountService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User getUser(Cookie[] cookies) {
        Optional<Cookie> userIdCookie = Arrays.stream(cookies)
                              .filter(c -> c.getName().equals("fcUserId"))
                              .findFirst();
        if (userIdCookie == null) {
            return null;
        }

        System.out.println("    getUser : " + userIdCookie.get().getValue());
                              
        try {
            String sql = "SELECT name, id, checking_balance FROM public.user WHERE id = ?";
            
            User user = jdbcTemplate.queryForObject(
                sql, 
                (rs, rowNum) -> new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDouble("checking_balance")
                ),
                UUID.fromString(userIdCookie.get().getValue())
            );

            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(String name) {
        try {
            String sql = "SELECT id, checking_balance, password FROM public.user WHERE name = ?";
            
            User user = jdbcTemplate.queryForObject(
                sql, 
                (rs, rowNum) -> new User(
                    rs.getString("id"), 
                    name, 
                    rs.getString("password"),
                    rs.getDouble("checking_balance")
                ),
                name
            );

            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Expense> getExpenses(User user) {
        try {
            String sql = "SELECT * FROM public.expense WHERE user_id = ?";
            return jdbcTemplate.query(
                sql, 
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Debt> getDebts(User user) {
        try {
            String sql = "SELECT * FROM public.debt WHERE user_id = ?";
            return jdbcTemplate.query(
                sql, 
                (rs, rowNum) -> new Debt(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("creditor"),
                    rs.getDouble("balance"),
                    rs.getDouble("interest"),
                    rs.getString("account_number"),
                    rs.getString("link"),
                    rs.getString("recurrenceid")
                ),
                UUID.fromString(user.getId())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AccountInfo getAccountInfo(User user) {
        try {
            AccountInfo info = new AccountInfo(user);
            List<Expense> expenses = this.getExpenses(user);
            info.setExpenses(expenses);
            // List<Debt> debts = this.getDebts(user);
            // info.setDebts(debts);

            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
