package net.lwenstrom.cooklion.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.cooklion.auth.service.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String jwt = extractJwtFromRequest(request);

        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!jwtService.isTokenValid(jwt)) {
                log.debug("Invalid JWT token provided");
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtService.extractUsername(jwt);
            final List<String> roles = jwtService.extractClaim(jwt, claims -> claims.get("authorities", List.class));

            if (StringUtils.hasText(username)
                    && roles != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                if (!authorities.isEmpty()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {} with authorities: {}", username, authorities);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("JWT token expired for request: {}", request.getRequestURI());
            response.setHeader("X-Auth-Error", "Token expired");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed JWT token received from IP: {}", getClientIpAddress(request));
            response.setHeader("X-Auth-Error", "Malformed token");
        } catch (Exception e) {
            log.error("Unexpected JWT authentication error: {}", e.getMessage());
            response.setHeader("X-Auth-Error", "Authentication failed");
        } finally {
            // Ensure we don't leave partial authentication state
            if (SecurityContextHolder.getContext().getAuthentication() == null
                    || SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getAuthorities()
                            .isEmpty()) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only process API endpoints
        return !request.getServletPath().startsWith("/api/");
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
