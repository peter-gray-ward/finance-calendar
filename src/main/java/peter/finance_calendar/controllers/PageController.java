package peter.finance_calendar.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.AccountInfo;
import peter.finance_calendar.models.Day;
import peter.finance_calendar.models.Event;
import peter.finance_calendar.models.SyncData;
import peter.finance_calendar.models.User;
import peter.finance_calendar.services.AccountService;
import peter.finance_calendar.services.CalendarService;
import peter.finance_calendar.utils.CalendarUtil;

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
    public String home(HttpServletRequest req, HttpSession session, Model model) {
        System.out.println("HomeController.home");

        try {
            return this.sendIndex(req, session, model);
        } catch (Exception e) {
            return "error"; // Return an error view in case of an exception
        }
    }

    @GetMapping("/auth")
    public String auth(HttpServletRequest request, Model model) {

        model.addAttribute("title", "Finance Calendar");
        
        return "auth";
    }

    protected String sendIndex(HttpServletRequest req, HttpSession session, Model model) {
        Cookie[] cookies = req.getCookies();
        User user = accountService.getUser(cookies);

        if (user == null) {
            return "auth";
        }

        AccountInfo info = accountService.getAccountInfo(user);

        int year = 2024;
        int month = 12;

        if (info == null) {
            return "error";
        }

        List<Event> events = calendarService.selectEvents(user, 2024, 11);

        SyncData data = new SyncData();
        
        List<List<Day>> weeks = calendarUtil.getWeeks(month, year, events);

        System.out.println(data.getMonths()[info.getMonth() -  1]);

        model.addAttribute("info", info);
        model.addAttribute("title", "Finance Calendar");
        model.addAttribute("name", user.getName());
        model.addAttribute("events", events);
        model.addAttribute("data", data);
        model.addAttribute("weeks", weeks);

        return "index";
    }
}
