package com.fourpillars.backend.fortune.service;

import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.dto.MajorRecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MajorRecommendationService {
    private static final List<String> TRAITS = List.of("분석", "창의", "소통", "실행", "탐구");

    private static final List<Faculty> FACULTIES = List.of(
            faculty("기계공학부", 95, 35, 25, 90, 70),
            faculty("IT융합공학부", 95, 65, 35, 75, 90),
            faculty("지구환경신소재공학부", 85, 45, 30, 70, 95),
            faculty("건축디자인학부", 65, 95, 55, 80, 60),
            faculty("서비스경영학부", 60, 60, 95, 75, 45),
            faculty("자유전공학부", 60, 70, 70, 50, 90),
            faculty("고숙련미래인재학부", 60, 40, 55, 100, 45)
    );

    public MajorRecommendationResponse recommend(FortuneCalculationResponse fortune) {
        var traits = studentTraits(fortune);
        var ranked = FACULTIES.stream()
                .map(faculty -> recommendation(faculty, traits))
                .sorted(Comparator.comparingInt(MajorRecommendationResponse.FacultyRecommendation::score).reversed()
                        .thenComparing(MajorRecommendationResponse.FacultyRecommendation::faculty))
                .limit(3)
                .toList();
        return new MajorRecommendationResponse(
                studentType(traits), traits, ranked,
                "사주 성향을 활용한 탐색용 결과이며 입학·전공 결정을 대신하지 않습니다.");
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

        var normalized = new LinkedHashMap<String, Integer>();
        score.forEach((key, value) -> normalized.put(key, Math.min(100, value)));
        return normalized;
    }

    private MajorRecommendationResponse.FacultyRecommendation recommendation(Faculty faculty, Map<String, Integer> student) {
        int distance = 0;
        for (String trait : TRAITS) distance += Math.abs(student.get(trait) - faculty.traits().get(trait));
        int match = Math.max(0, 100 - Math.round(distance / (float) TRAITS.size()));

        var strengths = TRAITS.stream()
                .sorted(Comparator.<String>comparingInt(trait -> Math.min(student.get(trait), faculty.traits().get(trait))).reversed())
                .limit(2)
                .toList();
        var reasons = strengths.stream().map(trait -> trait + " 성향이 학부 특성과 잘 맞아요").toList();

        // 실제 세부학과 목록이 등록되면 같은 성향 벡터 방식으로 학부 내부 TOP 3를 계산한다.
        var departments = faculty.departments().stream()
                .map(department -> departmentRecommendation(department, student))
                .sorted(Comparator.comparingInt(MajorRecommendationResponse.DepartmentRecommendation::score).reversed())
                .limit(3)
                .toList();
        return new MajorRecommendationResponse.FacultyRecommendation(faculty.name(), match, reasons, departments);
    }

    private MajorRecommendationResponse.DepartmentRecommendation departmentRecommendation(Department department, Map<String, Integer> student) {
        int distance = 0;
        for (String trait : TRAITS) distance += Math.abs(student.get(trait) - department.traits().get(trait));
        int match = Math.max(0, 100 - Math.round(distance / (float) TRAITS.size()));
        var strongest = TRAITS.stream().max(Comparator.comparingInt(t -> Math.min(student.get(t), department.traits().get(t)))).orElse("탐구");
        return new MajorRecommendationResponse.DepartmentRecommendation(department.name(), match, List.of(strongest + " 성향과 잘 맞아요"));
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
        TRAITS.forEach(trait -> result.put(trait, 35));
        return result;
    }

    private static void add(Map<String, Integer> scores, String trait, int value) {
        scores.compute(trait, (key, current) -> current + value);
    }

    private static Faculty faculty(String name, int analysis, int creativity, int communication, int practice, int exploration) {
        return new Faculty(name, traitMap(analysis, creativity, communication, practice, exploration), new ArrayList<>());
    }

    private static Map<String, Integer> traitMap(int analysis, int creativity, int communication, int practice, int exploration) {
        var result = new LinkedHashMap<String, Integer>();
        result.put("분석", analysis); result.put("창의", creativity); result.put("소통", communication);
        result.put("실행", practice); result.put("탐구", exploration);
        return result;
    }

    private record Faculty(String name, Map<String, Integer> traits, List<Department> departments) {}
    private record Department(String name, Map<String, Integer> traits) {}
}
