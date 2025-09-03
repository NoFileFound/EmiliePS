package org.emilieps.config.webserver.handlers;

// Imports
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.emilieps.Application;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public final class IPWhitelistHandler extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        if((!Application.getApplicationConfig().whitelist_ips.isEmpty() && !Application.getApplicationConfig().whitelist_ips.contains(clientIp)) || Application.getApplicationConfig().blacklist_ips.contains(clientIp)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: IP not allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}