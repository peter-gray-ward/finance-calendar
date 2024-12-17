package peter.finance_calendar.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import peter.finance_calendar.models.ServiceResult;
import peter.finance_calendar.models.User;
import peter.finance_calendar.utils.PasswordUtil;

@Service
public class AuthService {

    private AccountService accountService;

    public AuthService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ServiceResult<User> handleRegistration(String name, String password) {
        String error = null;

        if (name == null || name.isEmpty()) {
            error = "Name is required.";
        } else if (password == null || password.isEmpty()) {
            error = "Password is required.";
        }

        if (error == null) {
            try {
                String sql = "SELECT COUNT(*) FROM public.user WHERE name = ?";
                
                Integer count = jdbcTemplate.queryForObject(
                    sql, 
                    Integer.class,
                    name
                );

                if (count != null && count > 0) {
                    return new ServiceResult("User " + name + " already exists.");
                }


                sql = "INSERT INTO public.user (id, name, password) VALUES (?, ?, ?)";
                jdbcTemplate.update(
                    sql, 
                    UUID.randomUUID(), 
                    name, 
                    PasswordUtil.hashPassword(password)
                );

                User user = accountService.getUser(name);

                return new ServiceResult("success", null, user);
            } catch (DuplicateKeyException ex) {
                error = "User " + name + " is already registered.";
            } catch (Exception ex) {
                ex.printStackTrace();
                error = "An error occurred during registration.";
            }
        }

        return new ServiceResult(error);
    }

    public ServiceResult<User> handleLogin(String name, String password) {
        String error = null;

        try {
            User user = accountService.getUser(name);

            if (user ==  null) {
                return new ServiceResult("User " + name + " does not exist.");
            }

            boolean passwordMatches = false;

            try {
                passwordMatches = PasswordUtil.checkPassword(password, user.getPassword());
            } catch (Exception e) {
                error = "Invalid password";
                e.printStackTrace();
            }

            if (user != null && passwordMatches) {
                String accessToken = UUID.randomUUID().toString();
                user.setAccessToken(accessToken);

                return new ServiceResult("success", null, user);
            } else {
                error = "Incorrect password or name.";
            }
        } catch (EmptyResultDataAccessException e) {
            error = "User " + name + " not found.";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ServiceResult(error);
    }

    public void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        //SessionUtil.handleLogout(request, response);
    }
}
