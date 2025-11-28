package com.digitalwallet.authservice.config;

import com.digitalwallet.authservice.service.AuthService;
import com.digitalwallet.authservice.dto.ValidateResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Skip filter for public endpoints
        if (path.equals("/auth/register") || path.equals("/auth/login") || path.equals("/auth/validate")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        ValidateResponse validateResponse = authService.validateToken(authHeader);

        if (!validateResponse.isValid()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or missing token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}