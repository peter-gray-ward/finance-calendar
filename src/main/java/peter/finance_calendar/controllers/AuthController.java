package peter.finance_calendar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.Auth;
import peter.finance_calendar.models.ServiceResult;
import peter.finance_calendar.models.User;
import peter.finance_calendar.services.AuthService;
import peter.finance_calendar.utils.SessionUtil;

@RestController
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Auth> register(@RequestBody User user, HttpSession session) {
        ServiceResult result = authService.handleRegistration(user.getName(), user.getPassword());
        if (result.status.equals("success")) {
            SessionUtil.login(session, (User) result.data);
            return new ResponseEntity<>((User) result.data, HttpStatus.OK);
        }
        Auth auth = new Auth();
        auth.setAuthenticated(false);
        return new ResponseEntity<>(auth, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Auth> login(@RequestBody User user, HttpSession session) {

        ServiceResult result = authService.handleLogin(user.getName(), user.getPassword());

        if (result.status.equals("success")) {
            SessionUtil.login(session, (User) result.data);
            return new ResponseEntity<>((User) result.data, HttpStatus.OK);
        }
        
        Auth auth = new Auth();
        auth.setAuthenticated(false);
        return new ResponseEntity<>(auth, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest req, HttpServletResponse res) {
        SessionUtil.logout(req, res);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}