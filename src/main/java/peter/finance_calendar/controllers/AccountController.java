package peter.finance_calendar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.ControllerResponse;
import peter.finance_calendar.models.Expense;
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

    @PostMapping("/add-expense")
    public ResponseEntity<ControllerResponse<?>> addExpense(HttpServletRequest req) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult addedExpense = accountService.addExpense(user);
        if (addedExpense.status.equals("success") == false) {
            return new ResponseEntity<>(new ControllerResponse<>(addedExpense.status, addedExpense.exception), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ControllerResponse<>(addedExpense.status, addedExpense.data), HttpStatus.OK);
    }

    @DeleteMapping("/delete-expense/{expenseId}")
    public ResponseEntity<ControllerResponse<?>> deleteExpense(HttpServletRequest req, @PathVariable String expenseId) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult expenseDeleted = accountService.deleteExpense(user, expenseId);
        if (expenseDeleted.status.equals("success") == false) {
            return new ResponseEntity<>(new ControllerResponse<>(expenseDeleted.status, expenseDeleted.data), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ControllerResponse<>(expenseDeleted.status, expenseDeleted.exception), HttpStatus.OK);
    }

    @PostMapping("/update-expense")
    public ResponseEntity<ControllerResponse<Boolean>> updateExpense(HttpServletRequest req, @RequestBody Expense expense) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult expenseUpdated = accountService.updateExpense(user, expense);
        if (expenseUpdated.status.equals("success") == false) {
            return new ResponseEntity<>(new ControllerResponse<>(expenseUpdated.status, false), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ControllerResponse<>(expenseUpdated.status, true), HttpStatus.OK);
    }

    
}
