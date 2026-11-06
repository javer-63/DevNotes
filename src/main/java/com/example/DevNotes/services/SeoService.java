package com.example.DevNotes.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class SeoService {

    public Map<String, String> paginationMeta(int page, int totalPages) {
        String base = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/posts").toUriString();
        String prev = page > 1 ? (page == 2 ? base : base + "/page/" + (page - 1)) : null;
        String next = page < totalPages ? base + "/page/" + (page + 1) : null;

        Map<String, String> meta = new HashMap<>();
        meta.put("canonicalUrl", base);
        meta.put("prevUrl", prev);
        meta.put("nextUrl", next);
        return meta;
    }
}