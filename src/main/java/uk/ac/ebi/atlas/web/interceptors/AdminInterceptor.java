package uk.ac.ebi.atlas.web.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerInterceptor;

@Controller
public class AdminInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getUserPrincipal() == null) {
            return false;
        }

        LOGGER.info(
                "<preHandle> username: {}, request: {}, query: {}",
                request.getUserPrincipal().getName(),
                request.getRequestURI(),
                request.getQueryString());

        return true;
    }
}
