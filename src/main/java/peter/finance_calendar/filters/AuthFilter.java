package peter.finance_calendar.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/login",
        "/register",
        "/auth"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        HttpSession session = request.getSession(false);

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean authenticated = isAuthenticated(request, session);

        if (authenticated) {
            filterChain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/auth");
        }
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAuthenticated(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return false;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> userIdCookie = Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("fcUserId"))
                    .findFirst();

            String userId = userIdCookie.map(Cookie::getValue).orElse(null);
            if (userId != null) {
                Optional<Cookie> accessTokenCookie = Arrays.stream(cookies)
                        .filter(c -> c.getName().equals("fcAccessToken"))
                        .findFirst();

                String accessToken = accessTokenCookie.map(Cookie::getValue).orElse(null);

                if (accessToken != null && session.getAttribute(userId + ".access_token").equals(accessToken)) {
                    return true;
                }
            }
        }

        return false;
    }
}
