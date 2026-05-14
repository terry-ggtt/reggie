package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String[] PUBLIC_URLS = new String[]{
            "/employee/login",
            "/backend/**",
            "/front/**",
            "/common/**",
            "/user/sendMsg",
            "/user/login",
            "/report/**"
    };

    private static final String[] EMPLOYEE_URLS = new String[]{
            "/employee/**",
            "/category",
            "/category/page",
            "/dish",
            "/dish/page",
            "/dish/status/**",
            "/setmeal",
            "/setmeal/page",
            "/setmeal/status/**",
            "/order/page",
            "/order",
            "/orderDetail/**",
            "/orderReview/page",
            "/orderReview/reply",
            "/orderReview/status",
            "/afterSale/page",
            "/afterSale/handle"
    };

    private static final String[] USER_URLS = new String[]{
            "/user/loginout",
            "/user/status",
            "/addressBook",
            "/addressBook/**",
            "/shoppingCart/**",
            "/order/submit",
            "/order/userPage",
            "/order/list",
            "/order/again",
            "/orderReview",
            "/orderReview/myPage",
            "/afterSale",
            "/afterSale/myPage"
    };

    private static final String[] SHARED_LOGIN_URLS = new String[]{
            "/category/list",
            "/dish/list",
            "/setmeal/list",
            "/setmeal/dish/**",
            "/orderReview/public"
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        log.info("intercept request: {}", requestURI);

        if (match(PUBLIC_URLS, requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Long employeeId = getSessionId(session, "employee");
        Long userId = getSessionId(session, "user");

        try {
            if (match(EMPLOYEE_URLS, requestURI)) {
                passAsEmployee(employeeId, request, response, filterChain);
                return;
            }

            if (match(USER_URLS, requestURI)) {
                passAsUser(userId, request, response, filterChain);
                return;
            }

            if (match(SHARED_LOGIN_URLS, requestURI)) {
                passAsAnyLoggedIn(employeeId, userId, request, response, filterChain);
                return;
            }

            passAsAnyLoggedIn(employeeId, userId, request, response, filterChain);
        } finally {
            BaseContext.removeCurrentId();
        }
    }

    private void passAsEmployee(Long employeeId, HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain) throws IOException, ServletException {
        if (employeeId == null) {
            writeNotLogin(response);
            return;
        }
        BaseContext.setCurrentId(employeeId);
        filterChain.doFilter(request, response);
    }

    private void passAsUser(Long userId, HttpServletRequest request, HttpServletResponse response,
                            FilterChain filterChain) throws IOException, ServletException {
        if (userId == null) {
            writeNotLogin(response);
            return;
        }
        BaseContext.setCurrentId(userId);
        filterChain.doFilter(request, response);
    }

    private void passAsAnyLoggedIn(Long employeeId, Long userId, HttpServletRequest request,
                                   HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (userId != null) {
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }
        if (employeeId != null) {
            BaseContext.setCurrentId(employeeId);
            filterChain.doFilter(request, response);
            return;
        }
        writeNotLogin(response);
    }

    private Long getSessionId(HttpSession session, String key) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(key);
        return value instanceof Long ? (Long) value : null;
    }

    private boolean match(String[] urls, String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }

    private void writeNotLogin(HttpServletResponse response) throws IOException {
        log.info("user not logged in");
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }
}
