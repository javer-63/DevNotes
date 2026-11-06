package com.example.DevNotes.services;

import com.example.DevNotes.repos.PostRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ViewCounterService {

    private final PostRepo postRepo;

    @Value("${app.views-cookie-max-age-seconds}")
    private int maxAge;

    @Transactional
    public void registerView(String url, HttpServletRequest request, HttpServletResponse response) {
        String cookieName = "post_viewed_" + url;
        boolean viewed = request.getCookies() != null
                && Arrays.stream(request.getCookies()).anyMatch(c -> cookieName.equals(c.getName()));
        if (viewed) return;

        postRepo.incrementViews(url);
        Cookie cookie = new Cookie(cookieName, "true");
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}