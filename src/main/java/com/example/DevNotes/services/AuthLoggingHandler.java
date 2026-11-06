package com.example.DevNotes.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthLoggingHandler implements AuthenticationSuccessHandler {

    private final ClientInfoResolver clientInfo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("Вход администратора: устройство - {}, ip - {}",
                clientInfo.detectDevice(request.getHeader("User-Agent")),
                clientInfo.resolveClientIp(request));
        response.sendRedirect("/admin");
    }
}