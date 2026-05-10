package com.aethertrack.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request, extracts the Bearer token from the Authorization header,
 * validates it, and populates the Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider    jwtTokenProvider;
  private final UserDetailsService  userDetailsService;

  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider,
      UserDetailsService userDetailsService) {
    this.jwtTokenProvider  = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest  request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain          chain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (StringUtils.hasText(token)
        && jwtTokenProvider.validateToken(token)
        && !jwtTokenProvider.isRefreshToken(token)) {

      String username = jwtTokenProvider.extractUsername(token);
      var userDetails = userDetailsService.loadUserByUsername(username);

      var auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    chain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
