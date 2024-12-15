package peter.finance_calendar.services;

import java.util.Arrays;
import java.util.Calendar;
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
import peter.finance_calendar.models.ServiceResult;
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

    public ServiceResult updateCheckingBalance(User user, Double checkingBalance) {
        try {
            jdbcTemplate.update(
                "UPDATE public.user"
                + " SET checking_balance = ?"
                + " WHERE id = ?",
                checkingBalance,
                UUID.fromString(user.getId())
            );

            user.setCheckingBalance(checkingBalance);

            return new ServiceResult("success", null, user);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult("error", e, e.getMessage());
        }
    }

    
    public ServiceResult addExpense(User user) {
        try {
            UUID expenseId = UUID.randomUUID();
            jdbcTemplate.update(
                "INSERT INTO public.expense"
                + " (id, name, amount, recurrenceenddate, startdate, frequency, user_id)"
                + " VALUES (?, '', 0.0, ?, ?, 'monthly', ?)",
                expenseId,
                Calendar.getInstance().getTime(),
                Calendar.getInstance().getTime(),
                UUID.fromString(user.getId())
            );
            Expense expense = jdbcTemplate.queryForObject(
                "SELECT *"
                + " FROM public.expense"
                + " WHERE id = ?",
                (rs, rowNum) -> new Expense(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getDouble("amount"),
                    rs.getDate("recurrenceenddate"),
                    rs.getDate("startdate"),
                    rs.getString("frequency")
                ),
                expenseId
            );
            return new ServiceResult("success", null, expense);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult("error", e, e.getMessage());
        }
    }
    public ServiceResult deleteExpense(User user, String expenseId) {
        try {
            jdbcTemplate.update(
                "DELETE FROM public.expense"
                + " WHERE user_id = ?"
                + " AND id = ?",
                UUID.fromString(user.getId()),
                UUID.fromString(expenseId)
            );
            return new ServiceResult("success", null);
        } catch (Exception e) {
            return new ServiceResult("error", e, e.getMessage());
        }
    }

    public ServiceResult updateExpense(User user, Expense expense) {
        try {
            jdbcTemplate.update(
                "UPDATE public.expense"
                + " SET name = ?, amount = ?, recurrenceenddate = ?, startdate = ?, frequency = ?"
                + " WHERE user_id = ?"
                + " AND id = ?",
                expense.getName(),
                expense.getAmount(),
                expense.getRecurrenceenddate(),
                expense.getStartdate(),
                expense.getFrequency(),
                UUID.fromString(user.getId()),
                UUID.fromString(expense.getId())
            );
            return new ServiceResult("success", null);
        } catch (Exception e) {
            return new ServiceResult("error", e, e.getMessage());
        }
    }
}
