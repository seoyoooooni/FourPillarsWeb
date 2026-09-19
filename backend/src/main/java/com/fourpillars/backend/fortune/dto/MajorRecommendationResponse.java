package com.fourpillars.backend.fortune.dto;

import java.util.List;
import java.util.Map;

public record MajorRecommendationResponse(
        String studentType,
        Map<String, Integer> traits,
        List<DepartmentRecommendation> recommendations,
        String disclaimer) {
    public record DepartmentRecommendation(
            String department, int score, List<String> reasons, String sourceUrl, String admissionNote) {}
}
