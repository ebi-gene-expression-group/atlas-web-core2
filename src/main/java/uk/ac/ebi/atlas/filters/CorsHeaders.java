package uk.ac.ebi.atlas.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

public class CorsHeaders extends OncePerRequestFilter {

    @Override
    protected final void doFilterInternal(HttpServletRequest request,
                                          @NotNull HttpServletResponse response,
                                          @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod())) {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type");
            response.addHeader("Access-Control-Max-Age", "1728000");
        } else {
            response.addHeader("Access-Control-Allow-Origin", "*");
        }

        filterChain.doFilter(request, response);
    }
}
