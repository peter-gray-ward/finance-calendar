package peter.finance_calendar.utils;

import java.util.Calendar;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import peter.finance_calendar.models.User;

@Component
public class SessionUtil {

    public static void login(HttpSession session, User user) {
        System.out.println("Logging into session");
        
        try {
            String accessToken = user.getAccessToken();
            String userId = user.getId();

            Calendar todayCal = Calendar.getInstance();

            session.setAttribute(userId + ".year", todayCal.get(Calendar.YEAR));
            session.setAttribute(userId + ".month", todayCal.get(Calendar.MONTH));
            session.setAttribute(userId + ".access_token", accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logout(HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static String getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }   
}
