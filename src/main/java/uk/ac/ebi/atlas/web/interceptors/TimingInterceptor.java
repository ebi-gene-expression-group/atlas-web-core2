package uk.ac.ebi.atlas.web.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import org.springframework.lang.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Named
public class TimingInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimingInterceptor.class);

    static final String STOP_WATCH = "requestURLStopWatch";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURL = getRequestUrl(request);

        LOGGER.info("{} - start", requestURL);

        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());
        stopWatch.start();

        request.setAttribute(STOP_WATCH, stopWatch);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           @Nullable ModelAndView modelAndView) {
        StopWatch stopWatch = (StopWatch) request.getAttribute(STOP_WATCH);
        stopWatch.stop();

        String requestURL = getRequestUrl(request);

        LOGGER.info("{} - time taken {}", requestURL, stopWatch.getTotalTimeSeconds());
    }

    private static String getRequestUrl(HttpServletRequest request) {
        return request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }
}
