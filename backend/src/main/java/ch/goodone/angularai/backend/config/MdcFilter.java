package ch.goodone.angularai.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String SESSION_ID = "sessionId";
    private static final String USER_LOGIN = "userLogin";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            MDC.put(SESSION_ID, session.getId());
        }

        if (httpRequest.getUserPrincipal() != null) {
            MDC.put(USER_LOGIN, httpRequest.getUserPrincipal().getName());
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
