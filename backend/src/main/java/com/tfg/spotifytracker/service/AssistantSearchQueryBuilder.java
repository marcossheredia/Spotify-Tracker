package com.tfg.spotifytracker.service;


import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AssistantSearchQueryBuilder {

    public List<String> buildQueries(AssistantPlaylistPlanDTO plan) {
        Set<String> unique = new LinkedHashSet<>();
        if (plan == null) {
            return List.of();
        }

        String primaryGenre = normalizeQuery(plan.getPrimaryGenre());
        List<String> genres = filterValues(plan.getGenres());
        List<String> secondaryGenres = filterValues(plan.getSecondaryGenres());
        List<String> moods = filterValues(plan.getMoods());
        String eventContext = normalizeQuery(plan.getEventContext());
        String audience = normalizeQuery(plan.getTargetAudience());
        String language = normalizeQuery(plan.getLanguage());
        Integer yearStart = plan.getYearStart();
        Integer yearEnd = plan.getYearEnd();

        List<String> allGenres = new ArrayList<>();
        if (StringUtils.hasText(primaryGenre)) {
            allGenres.add(primaryGenre);
        }
        allGenres.addAll(genres);
        allGenres.addAll(secondaryGenres);
        allGenres = allGenres.stream().distinct().toList();

        String decadeHint = "";
        if (yearStart != null && yearEnd != null) {
            decadeHint = toDecadeHint(yearStart, yearEnd);
        }

        // Layer 1: intent-based queries
        if ("wedding".equals(eventContext)) {
            unique.add("romantic wedding songs");
            unique.add("wedding love songs");
            unique.add("elegant romantic ballads");
            unique.add("classic love songs");
            unique.add("wedding dinner music");
            if (StringUtils.hasText(language) && language.startsWith("es")) {
                unique.add("spanish romantic songs");
            }
        }

        if ("classroom".equals(eventContext) || "kids".equals(audience)) {
            unique.add("calm kids music");
            unique.add("relaxing children songs");
            unique.add("instrumental calm classroom");
            unique.add("family friendly calm music");
        }

        // Layer 2: genre/context queries
        for (String genre : allGenres) {
            if (!StringUtils.hasText(genre)) {
                continue;
            }
            if (StringUtils.hasText(decadeHint)) {
                unique.add(normalizeQuery(genre + " " + decadeHint + " classics"));
            }
            unique.add(normalizeQuery("classic " + genre));
            if (StringUtils.hasText(eventContext)) {
                unique.add(normalizeQuery(genre + " " + eventContext + " music"));
            }
            if (StringUtils.hasText(audience) && !"general".equals(audience)) {
                unique.add(normalizeQuery(genre + " " + audience + " friendly"));
            }
            if (yearStart != null && yearEnd != null) {
                unique.add(normalizeQuery(genre + " year:" + yearStart + "-" + yearEnd));
            }
        }

        for (String mood : moods) {
            if (StringUtils.hasText(primaryGenre)) {
                unique.add(normalizeQuery(primaryGenre + " " + mood));
            }
            if (StringUtils.hasText(eventContext)) {
                unique.add(normalizeQuery(mood + " " + eventContext + " songs"));
            }
            unique.add(normalizeQuery(mood + " music"));
        }

        // Layer 3: broad fallback queries
        unique.add("pop love songs");
        unique.add("romantic hits");
        unique.add("party classics");
        unique.add("calm music");
        unique.add("workout rock");

        List<String> queries = new ArrayList<>();
        for (String query : unique) {
            if (StringUtils.hasText(query)) {
                queries.add(query);
            }
        }
        return queries;
    }

    private String toDecadeHint(Integer yearStart, Integer yearEnd) {
        if (yearStart == null || yearEnd == null) {
            return "";
        }
        if (yearStart == 1970 && yearEnd == 1979) return "70s";
        if (yearStart == 1980 && yearEnd == 1989) return "80s";
        if (yearStart == 1990 && yearEnd == 1999) return "90s";
        if (yearStart == 2000 && yearEnd == 2009) return "2000s";
        return yearStart + " " + yearEnd;
    }

    private List<String> filterValues(List<String> values) {
        if (values == null) {
            return List.of();
        }

        List<String> filtered = new ArrayList<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                filtered.add(value.trim());
            }
        }
        return filtered;
    }

    private String normalizeQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }
}
