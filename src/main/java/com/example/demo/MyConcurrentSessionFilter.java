package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class MyConcurrentSessionFilter extends GenericFilterBean {
    @Autowired
    private MockRedis mockRedis;
    private LogoutHandler handlers = new CompositeLogoutHandler(new LogoutHandler[]{new SecurityContextLogoutHandler()});
    private final SessionRegistry sessionRegistry;
    private SessionInformationExpiredStrategy sessionInformationExpiredStrategy;

    public MyConcurrentSessionFilter(SessionRegistry sessionRegistry, SessionInformationExpiredStrategy sessionInformationExpiredStrategy) {
        this.sessionRegistry = sessionRegistry;
        this.sessionInformationExpiredStrategy = sessionInformationExpiredStrategy;
    }

    public void afterPropertiesSet() {
        Assert.notNull(this.sessionRegistry, "SessionRegistry required");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionInformation info = this.sessionRegistry.getSessionInformation(session.getId());
            if (info != null) {
                if (info.isExpired() || !mockRedis.isLatest(session.getId())) {
                    this.logger.debug(LogMessage.of(() -> {
                        return "Requested session ID " + request.getRequestedSessionId() + " has expired.";
                    }));
                    this.doLogout(request, response);
                    this.sessionInformationExpiredStrategy.onExpiredSessionDetected(new SessionInformationExpiredEvent(info, request, response));
                    return;
                }

                this.sessionRegistry.refreshLastRequest(info.getSessionId());
            }
        }

        chain.doFilter(request, response);
    }

    private void doLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        this.handlers.logout(request, response, auth);
    }

    public void setLogoutHandlers(LogoutHandler[] handlers) {
        this.handlers = new CompositeLogoutHandler(handlers);
    }

    public void setLogoutHandlers(List<LogoutHandler> handlers) {
        this.handlers = new CompositeLogoutHandler(handlers);
    }
}
