package peter.finance_calendar.controllers;

import java.util.Calendar;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.ControllerResponse;
import peter.finance_calendar.models.ServiceResult;
import peter.finance_calendar.models.SyncData;
import peter.finance_calendar.models.User;
import peter.finance_calendar.services.AccountService;
import peter.finance_calendar.services.CalendarService;

@RestController
public class CalendarController {

    private AccountService accountService;
    private CalendarService calendarService;

    public CalendarController(AccountService accountService, CalendarService calendarService) {
        this.accountService = accountService;
        this.calendarService = calendarService;
    }
    
    @GetMapping("/sync-data")
    public ResponseEntity<ControllerResponse<SyncData>> syncData() {
        SyncData syncData = new SyncData();
        ControllerResponse<SyncData> res = new ControllerResponse<>("success");
        res.setData(syncData);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/refresh-calendar")
    public ResponseEntity<ControllerResponse<String>> refreshCalendar(HttpServletRequest req, HttpServletResponse re, HttpSession session) {
        try {
            User user = accountService.getUser(req.getCookies());
            String userId = user.getId();
            ServiceResult generated = calendarService.generateEventsFromExpenses(user);
            
            if (generated.status.equals("success")) {
                Integer year = (Integer) session.getAttribute(userId + ".year");
                Integer month = (Integer) session.getAttribute(userId + ".month");

                if (year == null || month == null) {
                    Calendar today = Calendar.getInstance();
                    year = today.get(Calendar.YEAR);
                    month = today.get(Calendar.MONTH);
                    session.setAttribute(userId + ".year", year);
                    session.setAttribute(userId + ".month", month);
                }
                
                String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
                return new ResponseEntity<>(new ControllerResponse<>("success", null, calendarFragment), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ControllerResponse<>("error", generated.exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ControllerResponse<>("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    @PostMapping("/change-month/{which}")
    public ResponseEntity<ControllerResponse<HashMap<String, Integer>>> changeMonth(@PathVariable String which, HttpServletRequest req, HttpSession session) {
        User user = accountService.getUser(req.getCookies());
        String userId = user.getId();
        Integer year = (Integer) session.getAttribute(userId + ".year");
        Integer month = (Integer) session.getAttribute(userId + ".month");

        Calendar sessionDate = Calendar.getInstance();
        sessionDate.set(Calendar.YEAR, year);
        sessionDate.set(Calendar.MONTH, month);

        switch (which) {
            case "prev":
                sessionDate.add(Calendar.MONTH, -1);
            break;
            case "this":
                Calendar today = Calendar.getInstance();
                sessionDate.set(Calendar.YEAR, today.get(Calendar.YEAR));
                sessionDate.set(Calendar.MONTH, today.get(Calendar.MONTH));
            break;
            case "next":
                sessionDate.add(Calendar.MONTH, 1);
            break;
        }

        year = (Integer) sessionDate.get(Calendar.YEAR);
        month = (Integer) sessionDate.get(Calendar.MONTH);

        session.setAttribute(userId + ".year", year);
        session.setAttribute(userId + ".month", month);

        String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
        HashMap<String, Integer> data = new HashMap<>();
        data.put("year", year);
        data.put("month", month);
        return new ResponseEntity<>(new ControllerResponse<>("success", data, calendarFragment), HttpStatus.OK);
    }
}
