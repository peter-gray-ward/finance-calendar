package peter.finance_calendar.controllers;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.ControllerResponse;
import peter.finance_calendar.models.Event;
import peter.finance_calendar.models.ServiceResult;
import peter.finance_calendar.models.SyncData;
import peter.finance_calendar.models.User;
import peter.finance_calendar.services.AccountService;
import peter.finance_calendar.services.CalendarService;
import peter.finance_calendar.utils.CalendarUtil;

@RestController
public class CalendarController {

    private AccountService accountService;
    private CalendarService calendarService;
    private CalendarUtil calendarUtil;

    public CalendarController(AccountService accountService, CalendarService calendarService, CalendarUtil calendarUtil) {
        this.accountService = accountService;
        this.calendarService = calendarService;
        this.calendarUtil = calendarUtil;
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

        // Retrieve year and month from session or use the current date as default
        Integer year = (Integer) session.getAttribute(userId + ".year");
        Integer month = (Integer) session.getAttribute(userId + ".month");

        LocalDate sessionDate;

        if (year != null && month != null) {
            sessionDate = LocalDate.of(year, month, 1);
        } else {
            sessionDate = LocalDate.now().withDayOfMonth(1);
        }

        // Update the date based on the 'which' parameter
        switch (which) {
            case "prev":
                sessionDate = sessionDate.minusMonths(1);
                break;
            case "this":
                sessionDate = LocalDate.now().withDayOfMonth(1);
                break;
            case "next":
                sessionDate = sessionDate.plusMonths(1);
                break;
        }

        // Extract updated year and month
        year = sessionDate.getYear();
        month = sessionDate.getMonthValue();

        System.out.println(year + " : " + month);

        // Store updated year and month in the session
        session.setAttribute(userId + ".year", year);
        session.setAttribute(userId + ".month", month);

        // Generate calendar fragment
        String calendarFragment = calendarService.generateCalendarFragment(user, year, month);

        // Prepare response data
        HashMap<String, Integer> data = new HashMap<>();
        data.put("year", year);
        data.put("month", month);

        return new ResponseEntity<>(new ControllerResponse<>("success", data, calendarFragment), HttpStatus.OK);
    }

    @GetMapping("/get-event/{eventId}")
    public ResponseEntity<ControllerResponse<Event>> getEvent(HttpServletRequest req, @PathVariable String eventId) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<Event> eventResult = calendarService.getEvent(user, eventId);
        if (eventResult.status.equals("success")) {
            Event event = (Event) eventResult.data;
            String eventFragment = calendarService.generateEventFragment(event);
            return new ResponseEntity<>(new ControllerResponse<>("success", event, eventFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(eventResult.status), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/add-event/{year}/{month}/{date}")
    public ResponseEntity<ControllerResponse<Event>> addEvent(HttpServletRequest req, @PathVariable Integer year, @PathVariable Integer month, @PathVariable Integer date) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<Event> eventResult = calendarService.addEvent(user, year, month, date);
        if (eventResult.status.equals("success")) {
            Event event = (Event) eventResult.data;
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", event, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(eventResult.status), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/clude-this-event/{id}")
    public ResponseEntity<ControllerResponse<Boolean>> cludeThisEvent(HttpServletRequest req, HttpSession session, @PathVariable String id) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<Boolean> eventResult = calendarService.cludeEvent(user, id);
        if (eventResult.status.equals("success")) {
            int year = (int) session.getAttribute(user.getId() + ".year");
            int month = (int) session.getAttribute(user.getId() + ".month");
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", true, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(eventResult.status, false), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/clude-all-these-events/{recurrenceid}")
    public ResponseEntity<ControllerResponse<Boolean>> cludeAllTheseEvents(HttpServletRequest req, HttpSession session, @PathVariable String recurrenceid) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<Boolean> eventResult = calendarService.cludeAllEventTheseEvents(user, recurrenceid);
        if (eventResult.status.equals("success")) {
            int year = (int) session.getAttribute(user.getId() + ".year");
            int month = (int) session.getAttribute(user.getId() + ".month");
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", true, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(eventResult.status, false), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/save-this-event")
    public ResponseEntity<ControllerResponse<Event>> updateEvent(HttpServletRequest req, HttpSession session, @RequestBody Event event) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<Event> updatedEvent = calendarService.updateEvent(user, event);
        if (updatedEvent.status.equals("success")) {
            int year = (int) session.getAttribute(user.getId() + ".year");
            int month = (int) session.getAttribute(user.getId() + ".month");
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", event, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(updatedEvent.status, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/save-all-these-events")
    public ResponseEntity<ControllerResponse<Event>> updateAllTheseEvents(HttpServletRequest req, HttpSession session, @RequestBody Event event) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<Event> updatedEvent = calendarService.updateAllTheseEvents(user, event);
        if (updatedEvent.status.equals("success")) {
            int year = (int) session.getAttribute(user.getId() + ".year");
            int month = (int) session.getAttribute(user.getId() + ".month");
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", event, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(updatedEvent.status, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/delete-this-event/{eventId}")
    public ResponseEntity<ControllerResponse<?>> deleteEvent(HttpServletRequest req, HttpSession session, @PathVariable String eventId) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<?> updatedEvent = calendarService.deleteEvent(user, eventId);
        if (updatedEvent.status.equals("success")) {
            int year = (int) session.getAttribute(user.getId() + ".year");
            int month = (int) session.getAttribute(user.getId() + ".month");
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", null, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(updatedEvent.status, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/delete-all-these-events/{eventRecurrenceid}")
    public ResponseEntity<ControllerResponse<?>> deleteAllTheseEvents(HttpServletRequest req, HttpSession session, @PathVariable String eventRecurrenceid) {
        User user = accountService.getUser(req.getCookies());
        ServiceResult<?> updatedEvent = calendarService.deleteAllTheseEvents(user, eventRecurrenceid);
        if (updatedEvent.status.equals("success")) {
            int year = (int) session.getAttribute(user.getId() + ".year");
            int month = (int) session.getAttribute(user.getId() + ".month");
            String calendarFragment = calendarService.generateCalendarFragment(user, year, month);
            return new ResponseEntity<>(new ControllerResponse<>("success", null, calendarFragment), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ControllerResponse<>(updatedEvent.status, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
