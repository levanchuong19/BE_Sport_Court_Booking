package com.example.BE_SportCourtBooking.config;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.exception.AuthenException;
import com.example.BE_SportCourtBooking.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class Filter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;

    @Autowired
    HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isPublicAPI = checkIsPublicAPI(request.getRequestURI());
        if (isPublicAPI) {
            filterChain.doFilter(request, response);
        } else {
            String token = getToken(request);
            if (token == null){
                handlerExceptionResolver.resolveException(request,response,null, new AuthenException("Empty Token"));
                return;
            }
            Account account ;
            try{
                account = tokenService.getAccountByToken(token);

            }catch (ExpiredJwtException e){
                handlerExceptionResolver.resolveException(request,response,null, new AuthenException("Expired Token"));
                return;
            }catch (MalformedJwtException e){
                handlerExceptionResolver.resolveException(request,response,null, new AuthenException("Invalid Token"));
                return;
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    account,
                    token,
                    account.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        }
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null )return null;
        return token.substring(7);
    }

    private final List<String> AUTH_PERMISSION = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/login",
            "/api/register",
            "/api/forgot-password",
            "/websocket/**"
    );

    public boolean checkIsPublicAPI(String uri) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return AUTH_PERMISSION.stream().anyMatch(pattern ->  pathMatcher.match(pattern, uri));

    }
}
