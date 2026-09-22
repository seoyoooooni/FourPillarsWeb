package com.fourpillars.backend.fortune.service;

import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.dto.MajorRecommendationResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MajorRecommendationService {
    private static final List<String> TRAITS = List.of("분석", "창의", "소통", "실행", "탐구");
    private static final List<String> APTITUDES = List.of("이론·분석", "설계·창작", "실험·연구", "제작·정비",
            "데이터·디지털", "협업·소통", "서비스·현장", "운영·관리");
    private static final Map<String, Map<String, Integer>> ELEMENT_PROFILES = Map.of(
            "목", traitMap(40, 60, 45, 55, 80),
            "화", traitMap(30, 85, 65, 60, 35),
            "토", traitMap(55, 30, 45, 85, 45),
            "금", traitMap(85, 30, 35, 65, 55),
            "수", traitMap(55, 50, 40, 35, 85));
    private static final Map<String, Map<String, Integer>> CATEGORY_PROFILES = Map.of(
            "비겁", traitMap(45, 40, 55, 85, 40),
            "식상", traitMap(35, 85, 65, 50, 55),
            "재성", traitMap(65, 35, 60, 85, 40),
            "관성", traitMap(85, 30, 45, 70, 50),
            "인성", traitMap(65, 45, 40, 35, 90));
    private static final Map<String, String> STUDENT_TYPES = Map.ofEntries(
            Map.entry("분석:창의", "논리로 새 길을 만드는 전략 기획형"),
            Map.entry("분석:소통", "복잡한 내용을 풀어내는 논리 전달형"),
            Map.entry("분석:실행", "구조를 완성하는 설계 해결형"),
            Map.entry("분석:탐구", "원인을 끝까지 찾는 심층 분석형"),
            Map.entry("창의:분석", "가능성을 구조화하는 콘셉트 설계형"),
            Map.entry("창의:소통", "사람을 움직이는 콘텐츠 표현형"),
            Map.entry("창의:실행", "아이디어를 현실로 만드는 제작 창작형"),
            Map.entry("창의:탐구", "낯선 가능성을 찾는 실험 창작형"),
            Map.entry("소통:분석", "요구를 읽고 조율하는 관계 전략형"),
            Map.entry("소통:창의", "공감을 이야기로 만드는 감성 기획형"),
            Map.entry("소통:실행", "사람과 현장을 이끄는 협업 추진형"),
            Map.entry("소통:탐구", "사람을 깊이 이해하는 공감 탐색형"),
            Map.entry("실행:분석", "계획을 성과로 바꾸는 정밀 실행형"),
            Map.entry("실행:창의", "만들면서 답을 찾는 실전 발명형"),
            Map.entry("실행:소통", "팀의 움직임을 만드는 현장 리더형"),
            Map.entry("실행:탐구", "직접 시험하며 배우는 실험 실천형"),
            Map.entry("탐구:분석", "자료 속 원리를 찾는 지식 탐색형"),
            Map.entry("탐구:창의", "질문에서 가능성을 만드는 발견 창작형"),
            Map.entry("탐구:소통", "배운 것을 나누는 지식 연결형"),
            Map.entry("탐구:실행", "파고들어 결과까지 만드는 연구 실천형"));
    private static final List<Department> DEPARTMENTS = loadDepartments();

    public MajorRecommendationResponse recommend(FortuneCalculationResponse fortune) {
        return recommend(fortune, Map.of(), Map.of());
    }

    public MajorRecommendationResponse recommend(FortuneCalculationResponse fortune,
                                                   Map<String, Integer> interests,
                                                   Map<String, Integer> environments) {
        var traits = studentTraits(fortune);
        var aptitudes = studentAptitudes(fortune, traits);
        boolean personalized = interests != null && !interests.isEmpty()
                && environments != null && !environments.isEmpty();
        var safeInterests = validPreferences(interests);
        var safeEnvironments = validPreferences(environments);
        var candidates = DEPARTMENTS.stream().map(department -> candidate(
                department, aptitudes, safeInterests, safeEnvironments, personalized)).toList();
        var selected = selectThree(candidates, personalized);
        return new MajorRecommendationResponse(studentType(traits), traits, aptitudes, selected,
                personalized
                        ? "사주 적성 70%, 관심 분야 20%, 학습 환경 10%로 계산한 탐색 점수예요. 적합 확률이나 합격 가능성은 아니며, 지원 전 모집요강을 확인해 주세요."
                        : "생년월일과 태어난 시로 본 사주 적성 100% 기반의 탐색 점수예요. 적합 확률이나 합격 가능성은 아니며, 지원 전 모집요강을 확인해 주세요.");
    }

    private Map<String, Integer> studentAptitudes(FortuneCalculationResponse fortune, Map<String, Integer> traits) {
        var e = fortune.analysis().elementCounts();
        var c = fortune.yongsinAnalysis().categoryCounts();
        var result = new LinkedHashMap<String, Integer>();
        result.put("이론·분석", average(traits.get("분석"), traits.get("탐구"), signal(e, "금"), signal(c, "관성")));
        result.put("설계·창작", average(traits.get("창의"), signal(e, "목"), signal(e, "화"), signal(c, "식상")));
        result.put("실험·연구", average(traits.get("탐구"), traits.get("분석"), signal(e, "수"), signal(c, "인성")));
        result.put("제작·정비", average(traits.get("실행"), signal(e, "토"), signal(c, "비겁"), signal(c, "재성")));
        result.put("데이터·디지털", average(traits.get("분석"), traits.get("탐구"), signal(e, "금"), signal(e, "수")));
        result.put("협업·소통", average(traits.get("소통"), signal(c, "비겁"), signal(c, "식상"), balance(e)));
        result.put("서비스·현장", average(traits.get("소통"), traits.get("실행"), signal(e, "화"), signal(c, "재성")));
        result.put("운영·관리", average(traits.get("실행"), traits.get("분석"), signal(e, "토"), signal(c, "관성")));
        return result;
    }

    private static int signal(Map<String, Integer> counts, String key) {
        int total = Math.max(1, counts.values().stream().mapToInt(Integer::intValue).sum());
        return Math.min(95, 35 + (int) Math.round(counts.getOrDefault(key, 0) * 240d / total));
    }

    private static int balance(Map<String, Integer> counts) {
        if (counts.isEmpty()) return 60;
        int max = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int min = counts.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        return Math.max(35, 85 - (max - min) * 10);
    }

    private static int average(int... values) {
        return (int) Math.round(java.util.Arrays.stream(values).average().orElse(60));
    }

    private static Map<String, Integer> validPreferences(Map<String, Integer> input) {
        var result = new LinkedHashMap<String, Integer>();
        if (input != null) APTITUDES.forEach(axis -> result.put(axis,
                Math.max(0, Math.min(100, input.getOrDefault(axis, 50)))));
        if (result.isEmpty()) APTITUDES.forEach(axis -> result.put(axis, 50));
        return result;
    }

    private Map<String, Integer> studentTraits(FortuneCalculationResponse fortune) {
        var elements = fortune.analysis().elementCounts();
        var categories = fortune.yongsinAnalysis().categoryCounts();
        var elementScores = weightedProfile(elements, ELEMENT_PROFILES);
        var categoryScores = weightedProfile(categories, CATEGORY_PROFILES);
        var result = new LinkedHashMap<String, Integer>();
        TRAITS.forEach(trait -> result.put(trait,
                (int) Math.round(elementScores.get(trait) * .35 + categoryScores.get(trait) * .65)));
        return result;
    }

    private Candidate candidate(Department department, Map<String, Integer> aptitudes,
                                Map<String, Integer> interests, Map<String, Integer> environments,
                                boolean personalized) {
        double saju = fit(aptitudes, department.aptitudes());
        double interest = fit(interests, department.aptitudes());
        double environment = fit(environments, department.aptitudes());
        double score = personalized ? round(saju * .70 + interest * .20 + environment * .10) : round(saju);
        var reasons = department.activities().stream()
                .sorted(Comparator.comparingInt((Activity activity) -> aptitudes.get(axisForTrait(activity.trait()))).reversed()
                        .thenComparing(Activity::label))
                .limit(2)
                .map(activity -> "%s 적성이 강점으로 나타났고, 이 학과에는 ‘%s’ 관련 학습이 있어요."
                        .formatted(axisForTrait(activity.trait()), activity.label()))
                .toList();
        return new Candidate(department, score, saju, interest, environment, reasons);
    }

    private static double fit(Map<String, Integer> user, Map<String, Integer> department) {
        double importance = department.values().stream().mapToInt(Integer::intValue).sum();
        return APTITUDES.stream().mapToDouble(axis -> user.get(axis) * department.get(axis)).sum() / importance;
    }

    private List<MajorRecommendationResponse.DepartmentRecommendation> selectThree(List<Candidate> candidates,
                                                                                    boolean personalized) {
        var remaining = new ArrayList<>(candidates);
        var best = remaining.stream().max(Comparator.comparingDouble(Candidate::score)).orElseThrow();
        remaining.remove(best);
        if (!personalized) {
            var unexpected = remaining.stream().max(Comparator.comparingDouble(Candidate::saju)).orElseThrow();
            remaining.remove(unexpected);
            var strength = remaining.stream().max(Comparator.comparingDouble(Candidate::saju)).orElseThrow();
            return List.of(toResponse("사주 적성이 가장 잘 맞는 학과", best),
                    toResponse("의외로 잘 맞는 학과", unexpected),
                    toResponse("강점을 살리기 좋은 학과", strength));
        }
        var interest = remaining.stream().max(Comparator.comparingDouble(Candidate::interest)).orElseThrow();
        remaining.remove(interest);
        var environment = remaining.stream().max(Comparator.comparingDouble(Candidate::environment)).orElseThrow();
        return List.of(toResponse("종합적으로 가장 잘 맞는 학과", best),
                toResponse("관심사를 반영한 학과", interest),
                toResponse("학습 방식과 잘 맞는 학과", environment));
    }

    private MajorRecommendationResponse.DepartmentRecommendation toResponse(String role, Candidate candidate) {
        var d = candidate.department();
        String note = d.specialAdmission() ? "고숙련 일학습병행 등 별도 모집 과정입니다. 지원 자격과 전형을 확인해 주세요." : null;
        return new MajorRecommendationResponse.DepartmentRecommendation(role, d.name(), candidate.score(),
                candidate.reasons(), d.sourceUrl(), note);
    }

    private static double round(double value) { return Math.round(value * 10d) / 10d; }

    private static String axisForTrait(String trait) {
        return switch (trait) {
            case "분석" -> "이론·분석"; case "창의" -> "설계·창작"; case "소통" -> "협업·소통";
            case "실행" -> "제작·정비"; default -> "실험·연구";
        };
    }

    private String studentType(Map<String, Integer> traits) {
        var ranked = TRAITS.stream()
                .sorted(Comparator.comparingInt((String trait) -> traits.get(trait)).reversed()
                        .thenComparingInt(TRAITS::indexOf))
                .limit(2)
                .toList();
        return typeName(ranked.get(0), ranked.get(1));
    }

    public static String typeName(String primary, String secondary) {
        var name = STUDENT_TYPES.get(primary + ":" + secondary);
        if (name == null) throw new IllegalArgumentException("서로 다른 두 성향이 필요합니다.");
        return name;
    }

    private static Map<String, Double> weightedProfile(
            Map<String, Integer> counts, Map<String, Map<String, Integer>> profiles) {
        int total = profiles.keySet().stream().mapToInt(key -> counts.getOrDefault(key, 0)).sum();
        var result = new LinkedHashMap<String, Double>();
        for (String trait : TRAITS) {
            if (total == 0) {
                result.put(trait, 60d);
                continue;
            }
            int weighted = profiles.entrySet().stream()
                    .mapToInt(entry -> counts.getOrDefault(entry.getKey(), 0) * entry.getValue().get(trait))
                    .sum();
            result.put(trait, weighted / (double) total);
        }
        return result;
    }

    private static Map<String, Integer> traitMap(
            int analysis, int creativity, int communication, int execution, int exploration) {
        var result = new LinkedHashMap<String, Integer>();
        result.put("분석", analysis);
        result.put("창의", creativity);
        result.put("소통", communication);
        result.put("실행", execution);
        result.put("탐구", exploration);
        return result;
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
        if (columns.length != 7) throw new IllegalStateException("Invalid department profile: " + line);
        var activities = List.of(parseActivity(columns[3]), parseActivity(columns[4]), parseActivity(columns[5]));
        if (activities.stream().map(Activity::trait).distinct().count() != activities.size()) {
            throw new IllegalStateException("Repeated activity trait for " + columns[0]);
        }
        return new Department(columns[0], "별도모집".equals(columns[1]), columns[2], activities,
                parseAptitudes(columns[6]));
    }

    private static Activity parseActivity(String value) {
        var parts = value.split(":", 2);
        if (parts.length != 2 || !TRAITS.contains(parts[0]) || parts[1].isBlank()) {
            throw new IllegalStateException("Invalid department activity: " + value);
        }
        return new Activity(parts[0], parts[1]);
    }

    private static Map<String, Integer> parseAptitudes(String value) {
        var result = new LinkedHashMap<String, Integer>();
        for (String item : value.split(",")) {
            var parts = item.split("=", 2);
            if (parts.length != 2 || !APTITUDES.contains(parts[0])) throw new IllegalStateException("Invalid aptitude: " + item);
            result.put(parts[0], Integer.parseInt(parts[1]));
        }
        if (!result.keySet().containsAll(APTITUDES)) throw new IllegalStateException("Missing aptitude: " + value);
        return result;
    }

    private record Department(String name, boolean specialAdmission, String sourceUrl, List<Activity> activities,
                              Map<String, Integer> aptitudes) {}
    private record Activity(String trait, String label) {}
    private record Candidate(Department department, double score, double saju, double interest,
                             double environment, List<String> reasons) {}
}
