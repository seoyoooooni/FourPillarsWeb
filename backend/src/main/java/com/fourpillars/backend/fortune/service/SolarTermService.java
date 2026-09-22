package com.fourpillars.backend.fortune.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SolarTermService {
    public static final ZoneId KOREA = ZoneId.of("Asia/Seoul");
    private static final List<TermDefinition> DEFINITIONS = List.of(
            new TermDefinition(1, 6, 285, 11), new TermDefinition(2, 4, 315, 0),
            new TermDefinition(3, 6, 345, 1), new TermDefinition(4, 5, 15, 2),
            new TermDefinition(5, 6, 45, 3), new TermDefinition(6, 6, 75, 4),
            new TermDefinition(7, 7, 105, 5), new TermDefinition(8, 8, 135, 6),
            new TermDefinition(9, 8, 165, 7), new TermDefinition(10, 8, 195, 8),
            new TermDefinition(11, 7, 225, 9), new TermDefinition(12, 7, 255, 10));
    private final Map<Integer, List<SolarTerm>> termsByYear = new ConcurrentHashMap<>();

    public SolarTermContext contextAt(ZonedDateTime dateTime) {
        var terms = termsAround(dateTime.getYear());
        var previous = terms.stream().filter(t -> !t.dateTime().isAfter(dateTime))
                .max(Comparator.comparing(SolarTerm::dateTime)).orElseThrow();
        var next = terms.stream().filter(t -> t.dateTime().isAfter(dateTime))
                .min(Comparator.comparing(SolarTerm::dateTime)).orElseThrow();
        int pillarYear = dateTime.isBefore(term(dateTime.getYear(), 315).dateTime())
                ? dateTime.getYear() - 1 : dateTime.getYear();
        return new SolarTermContext(pillarYear, previous.monthIndex(), previous, next);
    }

    private List<SolarTerm> termsAround(int year) {
        var result = new ArrayList<SolarTerm>(36);
        for (int y = year - 1; y <= year + 1; y++)
            result.addAll(termsByYear.computeIfAbsent(y, this::calculateTerms));
        return result;
    }

    private SolarTerm term(int year, double longitude) {
        int index = -1;
        for (int i = 0; i < DEFINITIONS.size(); i++) if (DEFINITIONS.get(i).longitude() == longitude) index = i;
        if (index < 0) throw new IllegalArgumentException("지원하지 않는 절입 황경입니다: " + longitude);
        return termsByYear.computeIfAbsent(year, this::calculateTerms).get(index);
    }

    private List<SolarTerm> calculateTerms(int year) {
        return DEFINITIONS.stream().map(definition -> calculateTerm(year, definition)).toList();
    }

    private SolarTerm calculateTerm(int year, TermDefinition definition) {
        var center = ZonedDateTime.of(LocalDate.of(year, definition.month(), definition.day()), LocalTime.NOON, KOREA).toInstant();
        var low = center.minus(4, ChronoUnit.DAYS);
        var high = center.plus(4, ChronoUnit.DAYS);
        for (int i = 0; i < 50; i++) {
            var middle = low.plusMillis(ChronoUnit.MILLIS.between(low, high) / 2);
            if (signedAngle(apparentSolarLongitude(middle) - definition.longitude()) < 0) low = middle;
            else high = middle;
        }
        return new SolarTerm(high.atZone(KOREA), definition.monthIndex());
    }

    private double apparentSolarLongitude(Instant instant) {
        double julianDay = instant.toEpochMilli() / 86_400_000d + 2_440_587.5d;
        double t = (julianDay - 2_451_545d) / 36_525d;
        double meanLongitude = normalize(280.46646d + 36_000.76983d * t + 0.0003032d * t * t);
        double anomaly = Math.toRadians(normalize(357.52911d + 35_999.05029d * t - 0.0001537d * t * t));
        double equation = (1.914602d - 0.004817d * t - 0.000014d * t * t) * Math.sin(anomaly)
                + (0.019993d - 0.000101d * t) * Math.sin(2 * anomaly)
                + 0.000289d * Math.sin(3 * anomaly);
        double omega = Math.toRadians(125.04d - 1934.136d * t);
        return normalize(meanLongitude + equation - 0.00569d - 0.00478d * Math.sin(omega));
    }

    private double normalize(double angle) { double value = angle % 360d; return value < 0 ? value + 360d : value; }
    private double signedAngle(double angle) { double value = normalize(angle); return value > 180d ? value - 360d : value; }

    private record TermDefinition(int month, int day, double longitude, int monthIndex) {}
    public record SolarTerm(ZonedDateTime dateTime, int monthIndex) {}
    public record SolarTermContext(int pillarYear, int monthIndex, SolarTerm previous, SolarTerm next) {}
}
