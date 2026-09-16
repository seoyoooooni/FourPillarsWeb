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
                "사주 성향을 바탕으로 한 가벼운 탐색 결과로 참고해 주세요.");
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

    private MajorRecommendationResponse.FacultyRecommendation recommendation(Faculty faculty, Map<String, Integer> student) {
        int match = matchScore(student, faculty.traits());
        var reasons = facultyReasons(faculty.name(), student);

        // 실제 세부학과 목록이 등록되면 같은 성향 벡터 방식으로 학부 내부 TOP 3를 계산한다.
        var departments = faculty.departments().stream()
                .map(department -> departmentRecommendation(department, student))
                .sorted(Comparator.comparingInt(MajorRecommendationResponse.DepartmentRecommendation::score).reversed())
                .limit(3)
                .toList();
        return new MajorRecommendationResponse.FacultyRecommendation(faculty.name(), match, reasons, departments);
    }

    private List<String> facultyReasons(String faculty, Map<String, Integer> student) {
        return switch (faculty) {
            case "기계공학부" -> List.of(
                    combination(student, "실행", "분석") + "이 설계한 것을 실제 구조와 장치로 구현하는 흐름에 잘 맞아요",
                    score(student, "탐구") + "의 탐구 성향이 작동 원리를 파고들고 개선점을 찾는 데 힘을 보태요");
            case "IT융합공학부" -> List.of(
                    combination(student, "분석", "탐구") + "이 논리를 세우고 새로운 기술을 탐색하는 과정에 강점이 있어요",
                    score(student, "창의") + "의 창의 성향을 더해 기술을 새로운 서비스와 문제 해결 방식으로 연결할 수 있어요");
            case "지구환경신소재공학부" -> List.of(
                    combination(student, "탐구", "실행") + "이 현장 관찰과 소재 실험을 반복하는 연구 방식에 잘 맞아요",
                    score(student, "분석") + "의 분석 성향이 실험 결과에서 의미 있는 차이를 찾아내는 데 도움이 돼요");
            case "건축디자인학부" -> List.of(
                    combination(student, "창의", "실행") + "이 아이디어를 실제 공간과 형태로 발전시키는 과정에 잘 맞아요",
                    score(student, "소통") + "의 소통 성향이 사용자 요구를 듣고 설계 의도를 설명하는 데 힘이 돼요");
            case "서비스경영학부" -> List.of(
                    combination(student, "소통", "실행") + "이 사람의 요구를 파악하고 서비스 운영으로 옮기는 데 강점이 있어요",
                    score(student, "분석") + "의 분석 성향이 고객과 시장의 흐름을 수치로 판단하는 데 도움이 돼요");
            case "자유전공학부" -> List.of(
                    combination(student, "탐구", "창의") + "이 여러 분야를 넘나들며 나만의 전공 방향을 찾는 데 잘 맞아요",
                    score(student, "소통") + "의 소통 성향이 서로 다른 관점과 지식을 연결하는 데 힘을 보태요");
            case "고숙련미래인재학부" -> List.of(
                    combination(student, "실행", "분석") + "이 실무 기술을 빠르게 익히고 작업의 완성도를 높이는 데 잘 맞아요",
                    score(student, "탐구") + "의 탐구 성향이 현장에서 필요한 새 기술을 꾸준히 습득하는 데 도움이 돼요");
            default -> recommendationReasons(student, Map.of());
        };
    }

    private String combination(Map<String, Integer> student, String first, String second) {
        return "%s · %s 조합".formatted(score(student, first), score(student, second));
    }

    private String score(Map<String, Integer> student, String trait) {
        return "%s %d".formatted(trait, student.get(trait));
    }

    private MajorRecommendationResponse.DepartmentRecommendation departmentRecommendation(Department department, Map<String, Integer> student) {
        int match = matchScore(student, department.traits());
        return new MajorRecommendationResponse.DepartmentRecommendation(department.name(), match, recommendationReasons(student, department.traits()));
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

    private int matchScore(Map<String, Integer> student, Map<String, Integer> faculty) {
        double studentAverage = TRAITS.stream().mapToInt(student::get).average().orElse(0);
        double facultyAverage = TRAITS.stream().mapToInt(faculty::get).average().orElse(0);
        double numerator = 0; double studentVariance = 0; double facultyVariance = 0;
        int distance = 0;
        for (String trait : TRAITS) {
            double studentOffset = student.get(trait) - studentAverage;
            double facultyOffset = faculty.get(trait) - facultyAverage;
            numerator += studentOffset * facultyOffset;
            studentVariance += studentOffset * studentOffset;
            facultyVariance += facultyOffset * facultyOffset;
            distance += Math.abs(student.get(trait) - faculty.get(trait));
        }
        double correlation = studentVariance == 0 || facultyVariance == 0
                ? 0 : numerator / Math.sqrt(studentVariance * facultyVariance);
        double patternScore = (correlation + 1) * 50;
        double distanceScore = 100 - distance / (double) TRAITS.size();
        return Math.max(0, Math.min(96, (int) Math.round(patternScore * .65 + distanceScore * .35)));
    }

    private List<String> recommendationReasons(Map<String, Integer> student, Map<String, Integer> faculty) {
        var sharedStrengths = TRAITS.stream()
                .filter(trait -> student.get(trait) >= 60 && faculty.get(trait) >= 60)
                .sorted(Comparator.<String>comparingInt(trait ->
                        Math.min(student.get(trait), faculty.get(trait)) - Math.abs(student.get(trait) - faculty.get(trait)) / 2).reversed())
                .limit(2)
                .map(trait -> trait + " 성향이 학생과 학부 모두에서 강하게 나타나요")
                .toList();
        if (sharedStrengths.size() == 2) return sharedStrengths;

        var reasons = new ArrayList<>(sharedStrengths);
        TRAITS.stream()
                .filter(trait -> reasons.stream().noneMatch(reason -> reason.startsWith(trait + " ")))
                .sorted(Comparator.comparingInt(trait -> Math.abs(student.get(trait) - faculty.get(trait))))
                .limit(2 - reasons.size())
                .map(trait -> trait + " 성향의 강도가 학부 특성과 비슷해요")
                .forEach(reasons::add);
        return reasons;
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
