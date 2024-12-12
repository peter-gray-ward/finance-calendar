package peter.finance_calendar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.AccountInfo;
import peter.finance_calendar.services.AccountService;

@Controller
public class PageController {

   private AccountService accountService;

   public PageController(AccountService accountService) {
        this.accountService = accountService;
   }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        System.out.println("HomeController.home");

        try {
            return this.sendIndex(session, model);
        } catch (Exception e) {
            return "error"; // Return an error view in case of an exception
        }
    }

    @GetMapping("/auth")
    public String auth(HttpServletRequest request, Model model) {

        model.addAttribute("title", "Finance Calendar");
        
        return "auth";
    }

    protected String sendIndex(HttpSession session, Model model) {
        String name = (String) session.getAttribute("name");
        AccountInfo info = accountService.getAccountInfo(name);

        if (info == null) {
            return "error";
        }


        model.addAttribute("accountInfo", info);
        model.addAttribute("title", "Finance Calendar");
        model.addAttribute("name", name);

        return "index";
    }
}
