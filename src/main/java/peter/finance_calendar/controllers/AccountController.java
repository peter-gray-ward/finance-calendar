package peter.finance_calendar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.ControllerResponse;
import peter.finance_calendar.models.ServiceResult;
import peter.finance_calendar.models.User;
import peter.finance_calendar.services.AccountService;
import peter.finance_calendar.services.CalendarService;

@RestController
public class AccountController {
    
    private AccountService accountService;
    private CalendarService calendarService;

    public AccountController(AccountService accountService, CalendarService calendarService) {
        this.accountService = accountService;
        this.calendarService = calendarService;
    }

    @PostMapping("/save-checking-balance/{checkingBalance}")
    public ResponseEntity<ControllerResponse<?>> saveCheckingBalance(HttpServletRequest req, HttpSession session, @PathVariable Double checkingBalance) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult updatedUser = accountService.updateCheckingBalance(user, checkingBalance);
        if (updatedUser.exception != null) { 
            return new ResponseEntity<>(new ControllerResponse<>(updatedUser.status, updatedUser.exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        int year = (int) session.getAttribute(user.getId() + ".year");
        int month = (int) session.getAttribute(user.getId() + ".month");
        String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
        return new ResponseEntity<>(new ControllerResponse<>(updatedUser.status, (User)updatedUser.data, calendarFragment), HttpStatus.OK);
    }
}
