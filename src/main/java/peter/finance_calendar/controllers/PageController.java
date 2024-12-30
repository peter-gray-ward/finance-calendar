package peter.finance_calendar.controllers;

import java.util.Enumeration;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.AccountInfo;
import peter.finance_calendar.models.Day;
import peter.finance_calendar.models.Event;
import peter.finance_calendar.models.SyncData;
import peter.finance_calendar.models.User;
import peter.finance_calendar.services.AccountService;
import peter.finance_calendar.services.CalendarService;
import peter.finance_calendar.utils.CalendarUtil;
import peter.finance_calendar.utils.SessionUtil;

@Controller
public class PageController {

    private AccountService accountService;
    private CalendarService calendarService;
    private CalendarUtil calendarUtil;

    public PageController(AccountService accountService, CalendarService calendarService, CalendarUtil calendarUtil) {
        this.accountService = accountService;
        this.calendarService = calendarService;
        this.calendarUtil = calendarUtil;
    }

    @GetMapping("/")
    public String home(HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) {
        System.out.println("HomeController.home");

        try {
            Cookie[] cookies = req.getCookies();
            User user = accountService.getUser(cookies);

            if (user == null) {
                return "auth";
            }

            String userId = user.getId();
            Integer year = null;
            Integer month = null;

            try {
                year = (Integer) session.getAttribute(userId + ".year");
                month = (Integer) session.getAttribute(userId + ".month");
            } catch (Exception ex) {
                SessionUtil.logout(req, res);
                return "auth";
            }

            if (year == null || month == null || month == 0) {
                SessionUtil.logout(req, res);
                return "auth";
            }

            AccountInfo info = accountService.getAccountInfo(user);

            if (info == null) {
                return "error";
            }

            List<Event> events = calendarService.selectEvents(user, year, month);

            SyncData data = new SyncData();
            
            List<List<Day>> weeks = calendarUtil.getWeeks(user, month, year, events);

            model.addAttribute("info", info);
            model.addAttribute("title", "Finance Calendar");
            model.addAttribute("name", user.getName());
            model.addAttribute("events", events);
            model.addAttribute("data", data);
            model.addAttribute("weeks", weeks);

            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // Return an error view in case of an exception
        }
    }

    @GetMapping("/auth")
    public String auth(HttpServletRequest request, Model model) {

        model.addAttribute("title", "Finance Calendar");
        
        return "auth";
    }

}
