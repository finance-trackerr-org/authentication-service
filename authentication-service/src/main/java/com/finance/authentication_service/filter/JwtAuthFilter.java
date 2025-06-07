package com.finance.authentication_service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.authentication_service.service.JwtService;
import com.finance.authentication_service.service.UserInfoDetailService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserInfoDetailService userInfoDetailService;
    private final JwtService jwtService;

    @Autowired
    public JwtAuthFilter(UserInfoDetailService userInfoDetailService, JwtService jwtService) {
        this.userInfoDetailService = userInfoDetailService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer"))
                throw new JwtException("Invalid Token");

            String token = authHeader.substring(7);
            String userName = jwtService.extractUsername(token);
            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userInfoDetailService.loadUserByUsername(userName);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);
                }
                throw new JwtException("Unauthorized user");
            }
            throw new JwtException("Unauthorized user");
        }catch (ExpiredJwtException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    Map.of("status", HttpStatus.UNAUTHORIZED.value(),
                            "message", "Token has expired")
            ));
        } catch (JwtException | IllegalArgumentException ex) {
            String error = ex.getMessage();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    Map.of("status", HttpStatus.BAD_REQUEST.value(),
                            "message", (error != null || error!="") ? "Invalid token" : error)
            ));
        }
    }
}
