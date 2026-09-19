package com.fourpillars.backend.fortune.dto;

import java.util.List;
import java.util.Map;

public record MajorRecommendationResponse(
        String studentType,
        Map<String, Integer> traits,
        Map<String, Integer> aptitudes,
        List<DepartmentRecommendation> recommendations,
        String disclaimer) {
    public record DepartmentRecommendation(
            String role, String department, double score, List<String> reasons,
            String sourceUrl, String admissionNote) {}
}
