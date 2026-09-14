package com.fourpillars.backend.fortune.dto;

import java.util.List;
import java.util.Map;

public record MajorRecommendationResponse(
        String studentType,
        Map<String, Integer> traits,
        List<FacultyRecommendation> recommendations,
        String disclaimer) {

    public record FacultyRecommendation(
            String faculty,
            int score,
            List<String> reasons,
            List<DepartmentRecommendation> departments) {}

    public record DepartmentRecommendation(String department, int score, List<String> reasons) {}
}
