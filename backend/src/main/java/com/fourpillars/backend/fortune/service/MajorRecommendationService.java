package com.fourpillars.backend.fortune.service;

import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.dto.MajorRecommendationResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MajorRecommendationService {
    private static final List<String> TRAITS = List.of("분석", "창의", "소통", "실행", "탐구");
    // Curated activities are ordered by relevance to each department's official description.
    private static final int[] ACTIVITY_WEIGHTS = {45, 35, 20};
    private static final List<Department> DEPARTMENTS = loadDepartments();

    public MajorRecommendationResponse recommend(FortuneCalculationResponse fortune) {
        var traits = studentTraits(fortune);
        var ranked = DEPARTMENTS.stream()
                .map(department -> departmentRecommendation(department, traits))
                .sorted(Comparator.comparingInt(MajorRecommendationResponse.DepartmentRecommendation::score).reversed()
                        .thenComparing(MajorRecommendationResponse.DepartmentRecommendation::department))
                .limit(3).toList();
        return new MajorRecommendationResponse(studentType(traits), traits, ranked,
                "공식 학과소개에 나온 학습 활동과 사주 성향을 연결한 탐색 결과예요. 점수는 적합 확률이 아니며, 지원 전 모집요강을 확인해 주세요.");
    }

    private Map<String, Integer> studentTraits(FortuneCalculationResponse fortune) {
        var score = baseTraits();
        var elements = fortune.analysis().elementCounts();
        add(score, "탐구", elements.getOrDefault("목", 0) * 5);
        add(score, "창의", elements.getOrDefault("화", 0) * 6);
        add(score, "소통", elements.getOrDefault("화", 0) * 3);
        add(score, "실행", elements.getOrDefault("토", 0) * 6);
        add(score, "분석", elements.getOrDefault("금", 0) * 6);
        add(score, "탐구", elements.getOrDefault("수", 0) * 6);
        var categories = fortune.yongsinAnalysis().categoryCounts();
        add(score, "실행", categories.getOrDefault("비겁", 0) * 4);
        add(score, "창의", categories.getOrDefault("식상", 0) * 6);
        add(score, "소통", categories.getOrDefault("식상", 0) * 3);
        add(score, "실행", categories.getOrDefault("재성", 0) * 5);
        add(score, "소통", categories.getOrDefault("재성", 0) * 3);
        add(score, "분석", categories.getOrDefault("관성", 0) * 5);
        add(score, "실행", categories.getOrDefault("관성", 0) * 3);
        add(score, "탐구", categories.getOrDefault("인성", 0) * 6);
        add(score, "분석", categories.getOrDefault("인성", 0) * 3);
        return normalizeTraits(score);
    }

    private MajorRecommendationResponse.DepartmentRecommendation departmentRecommendation(
            Department department, Map<String, Integer> student) {
        int weightedScore = 0;
        for (int index = 0; index < ACTIVITY_WEIGHTS.length; index++) {
            weightedScore += student.get(department.activities().get(index).trait()) * ACTIVITY_WEIGHTS[index];
        }
        int score = Math.round(weightedScore / 100f);
        var reasons = department.activities().stream()
                .sorted(Comparator.comparingInt((Activity activity) -> student.get(activity.trait())).reversed()
                        .thenComparing(Activity::label))
                .limit(2)
                .map(activity -> "사주에서 %s 성향이 %d점으로 나타났어요. 이 학과에서는 ‘%s’을 다뤄요."
                        .formatted(activity.trait(), student.get(activity.trait()), activity.label()))
                .toList();
        String admissionNote = department.specialAdmission()
                ? "고숙련 일학습병행 등 별도 모집 과정입니다. 지원 자격과 전형을 확인해 주세요."
                : null;
        return new MajorRecommendationResponse.DepartmentRecommendation(
                department.name(), score, reasons, department.sourceUrl(), admissionNote);
    }

    private Map<String, Integer> normalizeTraits(Map<String, Integer> raw) {
        int minimum = raw.values().stream().min(Integer::compareTo).orElse(0);
        int maximum = raw.values().stream().max(Integer::compareTo).orElse(0);
        var normalized = new LinkedHashMap<String, Integer>();
        if (maximum == minimum) {
            TRAITS.forEach(trait -> normalized.put(trait, 65));
            return normalized;
        }
        TRAITS.forEach(trait -> normalized.put(trait,
                35 + Math.round((raw.get(trait) - minimum) * 60f / (maximum - minimum))));
        return normalized;
    }

    private String studentType(Map<String, Integer> traits) {
        var strongest = traits.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        return switch (strongest) {
            case "분석" -> "논리적인 문제 해결형";
            case "창의" -> "아이디어를 만드는 창작형";
            case "소통" -> "사람을 연결하는 협력형";
            case "실행" -> "직접 완성하는 실전형";
            default -> "깊이 파고드는 탐구형";
        };
    }

    private static LinkedHashMap<String, Integer> baseTraits() {
        var result = new LinkedHashMap<String, Integer>();
        TRAITS.forEach(trait -> result.put(trait, 0));
        return result;
    }

    private static void add(Map<String, Integer> scores, String trait, int value) {
        scores.compute(trait, (key, current) -> current + value);
    }

    private static List<Department> loadDepartments() {
        var resource = MajorRecommendationService.class.getResourceAsStream("/major-departments.tsv");
        if (resource == null) throw new IllegalStateException("Missing major-departments.tsv");
        try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            var departments = reader.lines()
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(MajorRecommendationService::parseDepartment).toList();
            if (departments.stream().map(Department::name).distinct().count() != departments.size()) {
                throw new IllegalStateException("Duplicate department in major-departments.tsv");
            }
            return departments;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load major-departments.tsv", exception);
        }
    }

    private static Department parseDepartment(String line) {
        var columns = line.split("\\|", -1);
        if (columns.length != 6) throw new IllegalStateException("Invalid department profile: " + line);
        var activities = List.of(parseActivity(columns[3]), parseActivity(columns[4]), parseActivity(columns[5]));
        if (activities.stream().map(Activity::trait).distinct().count() != activities.size()) {
            throw new IllegalStateException("Repeated activity trait for " + columns[0]);
        }
        return new Department(columns[0], "별도모집".equals(columns[1]), columns[2], activities);
    }

    private static Activity parseActivity(String value) {
        var parts = value.split(":", 2);
        if (parts.length != 2 || !TRAITS.contains(parts[0]) || parts[1].isBlank()) {
            throw new IllegalStateException("Invalid department activity: " + value);
        }
        return new Activity(parts[0], parts[1]);
    }

    private record Department(String name, boolean specialAdmission, String sourceUrl, List<Activity> activities) {}
    private record Activity(String trait, String label) {}
}
