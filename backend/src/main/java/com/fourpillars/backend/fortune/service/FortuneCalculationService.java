package com.fourpillars.backend.fortune.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourpillars.backend.fortune.dto.FortuneCalculationRequest;
import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.dto.TodayFortuneResponse;
import com.fourpillars.backend.profile.dto.BirthProfileResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FortuneCalculationService {
    private static final List<String> STEMS = List.of("갑", "을", "병", "정", "무", "기", "경", "신", "임", "계");
    private static final List<String> BRANCHES = List.of("자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해");
    private static final Set<String> YANG_STEMS = Set.of("갑", "병", "무", "경", "임");
    private static final List<String> STAGE_NAMES = List.of("장생", "목욕", "관대", "건록", "제왕", "쇠", "병", "사", "묘", "절", "태", "양");
    private static final List<String> SINSAL_NAMES = List.of("겁살", "재살", "천살", "지살", "년살", "월살", "망신살", "장성살", "반안살", "역마살", "육해살", "화개살");
    private static final List<List<String>> GONGMANG = List.of(List.of("술", "해"), List.of("신", "유"), List.of("오", "미"), List.of("진", "사"), List.of("인", "묘"), List.of("자", "축"));
    private static final Map<String, StemInfo> STEM_INFO = Map.ofEntries(
            Map.entry("갑", new StemInfo("목", true)), Map.entry("을", new StemInfo("목", false)),
            Map.entry("병", new StemInfo("화", true)), Map.entry("정", new StemInfo("화", false)),
            Map.entry("무", new StemInfo("토", true)), Map.entry("기", new StemInfo("토", false)),
            Map.entry("경", new StemInfo("금", true)), Map.entry("신", new StemInfo("금", false)),
            Map.entry("임", new StemInfo("수", true)), Map.entry("계", new StemInfo("수", false)));
    private static final Map<String, String> BRANCH_ELEMENTS = Map.ofEntries(
            Map.entry("자", "수"), Map.entry("축", "토"), Map.entry("인", "목"), Map.entry("묘", "목"),
            Map.entry("진", "토"), Map.entry("사", "화"), Map.entry("오", "화"), Map.entry("미", "토"),
            Map.entry("신", "금"), Map.entry("유", "금"), Map.entry("술", "토"), Map.entry("해", "수"));
    private static final Map<String, List<RawHiddenStem>> HIDDEN = Map.ofEntries(
            Map.entry("자", hs("임", "여기", "계", "정기")), Map.entry("축", hs("계", "여기", "신", "중기", "기", "정기")),
            Map.entry("인", hs("무", "여기", "병", "중기", "갑", "정기")), Map.entry("묘", hs("갑", "여기", "을", "정기")),
            Map.entry("진", hs("을", "여기", "계", "중기", "무", "정기")), Map.entry("사", hs("무", "여기", "경", "중기", "병", "정기")),
            Map.entry("오", hs("병", "여기", "기", "중기", "정", "정기")), Map.entry("미", hs("정", "여기", "을", "중기", "기", "정기")),
            Map.entry("신", hs("무", "여기", "임", "중기", "경", "정기")), Map.entry("유", hs("경", "여기", "신", "정기")),
            Map.entry("술", hs("신", "여기", "정", "중기", "무", "정기")), Map.entry("해", hs("무", "여기", "갑", "중기", "임", "정기")));
    private static final Map<String, String> GENERATES = Map.of("목", "화", "화", "토", "토", "금", "금", "수", "수", "목");
    private static final Map<String, String> CONTROLS = Map.of("목", "토", "토", "수", "수", "화", "화", "금", "금", "목");
    private static final Map<String, String> FIRST_HOUR_STEM = Map.of("갑", "갑", "기", "갑", "을", "병", "경", "병", "병", "무", "신", "무", "정", "경", "임", "경", "무", "임", "계", "임");
    private static final Map<String, String> JANGSAENG = Map.of("갑", "해", "을", "오", "병", "인", "정", "유", "무", "인", "기", "유", "경", "사", "신", "자", "임", "신", "계", "묘");
    private static final Map<String, String> TRIO = Map.ofEntries(Map.entry("인", "화"), Map.entry("오", "화"), Map.entry("술", "화"), Map.entry("신", "수"), Map.entry("자", "수"), Map.entry("진", "수"), Map.entry("사", "금"), Map.entry("유", "금"), Map.entry("축", "금"), Map.entry("해", "목"), Map.entry("묘", "목"), Map.entry("미", "목"));
    private static final Map<String, String> GEOBSAL = Map.of("화", "해", "수", "사", "금", "인", "목", "신");
    private static final Map<String, String> CATEGORY = Map.ofEntries(Map.entry("비견", "비겁"), Map.entry("겁재", "비겁"), Map.entry("식신", "식상"), Map.entry("상관", "식상"), Map.entry("편재", "재성"), Map.entry("정재", "재성"), Map.entry("편관", "관성"), Map.entry("정관", "관성"), Map.entry("편인", "인성"), Map.entry("정인", "인성"));
    private static final Map<String, List<String>> CHEONEUL = Map.ofEntries(Map.entry("갑", List.of("축", "미")), Map.entry("무", List.of("축", "미")), Map.entry("경", List.of("축", "미")), Map.entry("을", List.of("자", "신")), Map.entry("기", List.of("자", "신")), Map.entry("병", List.of("해", "유")), Map.entry("정", List.of("해", "유")), Map.entry("임", List.of("사", "묘")), Map.entry("계", List.of("사", "묘")), Map.entry("신", List.of("오", "인")));
    private static final Map<String, String> MUNCHANG = Map.of("갑", "사", "을", "오", "병", "신", "정", "유", "무", "신", "기", "유", "경", "해", "신", "자", "임", "인", "계", "묘");
    private static final Map<String, String> WOLDEOK = Map.of("화", "병", "수", "임", "금", "경", "목", "갑");
    private static final Map<String, String> CHEONDEOK = Map.ofEntries(Map.entry("인", "정"), Map.entry("묘", "신"), Map.entry("진", "임"), Map.entry("사", "신"), Map.entry("오", "해"), Map.entry("미", "갑"), Map.entry("신", "계"), Map.entry("유", "인"), Map.entry("술", "병"), Map.entry("해", "을"), Map.entry("자", "사"), Map.entry("축", "경"));
    private static final Set<String> CHEONDEOK_BRANCH_MONTHS = Set.of("묘", "오", "유", "자");
    private static final Map<String, String> AMROK = Map.of("갑", "해", "을", "술", "병", "신", "정", "미", "무", "신", "기", "미", "경", "사", "신", "진", "임", "인", "계", "축");
    private static final Map<String, String> YANGIN = Map.of("갑", "묘", "병", "오", "무", "오", "경", "유", "임", "자");
    private static final Map<String, String> HONGYEOM = Map.of("갑", "오", "을", "오", "병", "인", "정", "미", "무", "진", "기", "진", "경", "술", "신", "유", "임", "자", "계", "신");
    private static final Set<String> BAEKHO = Set.of("갑진", "을미", "병술", "정축", "무진", "임술", "계축");
    private static final Set<String> GOEGANG = Set.of("경진", "경술", "임진", "임술");
    private static final Set<String> WONJIN = Set.of("자:미", "축:오", "인:유", "묘:신", "진:해", "사:술");
    private static final Set<String> STEM_COMBINATIONS = pairSet("갑기", "을경", "병신", "정임", "무계");
    private static final Set<String> STEM_CLASHES = pairSet("갑경", "을신", "병임", "정계");
    private static final Set<String> BRANCH_COMBINATIONS = pairSet("자축", "인해", "묘술", "진유", "사신", "오미");
    private static final Set<String> BRANCH_CLASHES = pairSet("자오", "축미", "인신", "묘유", "진술", "사해");
    private static final Set<String> BRANCH_HARMS = pairSet("자미", "축오", "인사", "묘진", "신해", "유술");
    private static final Set<String> BRANCH_BREAKS = pairSet("자유", "축진", "인해", "묘오", "사신", "미술");
    private static final Set<String> BRANCH_PUNISHMENTS = pairSet("인사", "사신", "신인", "축술", "술미", "미축", "자묘");
    private static final Set<String> SELF_PUNISHMENTS = Set.of("진", "오", "유", "해");

    private final Map<String, PillarRecord> solarRecords = new LinkedHashMap<>();
    private final Map<String, PillarRecord> lunarRecords = new LinkedHashMap<>();
    private final Set<String> lunarMonths = new java.util.HashSet<>();
    private final Set<String> leapLunarMonths = new java.util.HashSet<>();
    private final SolarTermService solarTermService;

    public FortuneCalculationService() throws IOException {
        this(new SolarTermService());
    }

    @Autowired
    public FortuneCalculationService(SolarTermService solarTermService) throws IOException {
        this.solarTermService = solarTermService;
        var objectMapper = new ObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        var resource = new ClassPathResource("fortune/pillars.json");
        List<PillarRecord> records = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        for (var record : records) {
            solarRecords.put(record.solarDate(), record);
            var monthKey = "%04d-%02d".formatted(record.lunarYear(), record.lunarMonth());
            lunarMonths.add(monthKey);
            if (record.isLeapMonth()) leapLunarMonths.add(monthKey);
            lunarRecords.put("%s-%02d-%d".formatted(monthKey, record.lunarDay(), record.isLeapMonth() ? 1 : 0), record);
        }
    }

    public FortuneCalculationResponse calculate(FortuneCalculationRequest request) {
        validateBirthDate(request.birthDate(), request.calendarType(), request.leapMonth());
        var record = request.calendarType().name().equals("SOLAR")
                ? solarRecords.get(request.birthDate().toString())
                : lunarRecords.get("%04d-%02d-%02d-%d".formatted(request.birthDate().getYear(), request.birthDate().getMonthValue(), request.birthDate().getDayOfMonth(), request.leapMonth() ? 1 : 0));
        if (record == null) throw new IllegalArgumentException(request.calendarType().name().equals("LUNAR")
                ? "선택한 음력 날짜는 존재하지 않습니다. 날짜를 다시 확인해 주세요."
                : "선택한 날짜는 만세력 지원 범위(1926-01-01~2027-12-31) 밖입니다.");
        var solarDate = LocalDate.parse(record.solarDate());
        var birthDateTime = ZonedDateTime.of(solarDate, request.birthTime(), SolarTermService.KOREA);
        var termContext = solarTermService.contextAt(birthDateTime);
        var year = yearPillar(termContext.pillarYear());
        var month = monthPillar(year.stem(), termContext.monthIndex());
        var dayRecord = request.birthTime().getHour() == 23 ? solarRecords.get(solarDate.plusDays(1).toString()) : record;
        if (dayRecord == null) throw new IllegalArgumentException("야자시 일주 계산에 필요한 다음 날 만세력 데이터가 없습니다.");
        var day = split(dayRecord.dayPillar());
        var hour = calculateHour(day.stem(), request.birthTime());
        var pillars = new Pillars(year.stem(), year.branch(), month.stem(), month.branch(), day.stem(), day.branch(), hour.stem(), hour.branch());
        var basic = analyzeBasic(pillars);
        var resultPillars = new LinkedHashMap<String, String>();
        resultPillars.put("year", year.stem() + year.branch()); resultPillars.put("month", month.stem() + month.branch());
        resultPillars.put("day", day.stem() + day.branch()); resultPillars.put("hour", hour.stem() + hour.branch());
        var daeun = analyzeDaeun(pillars, birthDateTime, termContext, request.gender());
        return new FortuneCalculationResponse(record.solarDate(), new FortuneCalculationResponse.LunarDate(record.lunarYear(), record.lunarMonth(), record.lunarDay(), record.isLeapMonth()), resultPillars, basic, analyzeStageTwo(pillars), analyzeStageThree(pillars), analyzeYongsin(basic), daeun);
    }

    public void validateBirthDate(LocalDate date, com.fourpillars.backend.profile.domain.CalendarType calendarType,
                                  boolean leapMonth) {
        if (calendarType == com.fourpillars.backend.profile.domain.CalendarType.SOLAR) {
            if (leapMonth) throw new IllegalArgumentException("양력 날짜에는 윤달을 선택할 수 없습니다.");
            return;
        }
        var monthKey = "%04d-%02d".formatted(date.getYear(), date.getMonthValue());
        if (!lunarMonths.contains(monthKey)) {
            throw new IllegalArgumentException("선택한 음력 연월은 만세력 지원 범위 밖입니다.");
        }
        if (leapMonth && !leapLunarMonths.contains(monthKey)) {
            throw new IllegalArgumentException("%d년 음력 %d월에는 윤달이 없습니다. 평달을 선택해 주세요."
                    .formatted(date.getYear(), date.getMonthValue()));
        }
    }

    public TodayFortuneResponse today(BirthProfileResponse profile) {
        return forDate(profile, LocalDate.now(ZoneId.of("Asia/Seoul")));
    }

    public TodayFortuneResponse forDate(BirthProfileResponse profile, LocalDate targetDate) {
        var natalCalculation = calculate(new FortuneCalculationRequest(profile.birthDate(), profile.birthTime(), profile.calendarType(), profile.leapMonth()));
        var p = natalCalculation.pillars();
        var y = split(p.get("year")); var m = split(p.get("month")); var d = split(p.get("day")); var h = split(p.get("hour"));
        var natal = new Pillars(y.stem(), y.branch(), m.stem(), m.branch(), d.stem(), d.branch(), h.stem(), h.branch());
        var today = targetDate;
        var current = solarRecords.get(today.toString());
        if (current == null) throw new IllegalArgumentException("오늘 날짜는 아직 만세력 데이터 지원 범위에 포함되지 않습니다.");
        var fortune = dailyFortune(natal, current, natalCalculation.yongsinAnalysis().yongsinElement());
        var annual = solarRecords.values().stream().filter(r -> r.solarDate().startsWith(today.getYear() + "-")).map(r -> dailyFortune(natal, r, natalCalculation.yongsinAnalysis().yongsinElement()).overall()).toList();
        int rank = (int) annual.stream().filter(score -> score > fortune.overall()).count() + 1;
        var recent = new ArrayList<TodayFortuneResponse.TrendPoint>();
        for (int offset = -6; offset <= 0; offset++) {
            var date = today.plusDays(offset); var record = solarRecords.get(date.toString());
            if (record != null) recent.add(new TodayFortuneResponse.TrendPoint(date, dailyFortune(natal, record, natalCalculation.yongsinAnalysis().yongsinElement()).overall(), offset == 0));
        }
        return new TodayFortuneResponse(fortune, new TodayFortuneResponse.AnnualRank(rank, annual.size(), Math.max(1, (int) Math.ceil(rank * 100d / annual.size()))), recent);
    }

    public Map<LocalDate, TodayFortuneResponse.DailyFortune> calculateYear(BirthProfileResponse profile, int year) {
        var natalCalculation = calculate(new FortuneCalculationRequest(profile.birthDate(), profile.birthTime(), profile.calendarType(), profile.leapMonth()));
        var p = natalCalculation.pillars();
        var y = split(p.get("year")); var m = split(p.get("month")); var d = split(p.get("day")); var h = split(p.get("hour"));
        var natal = new Pillars(y.stem(), y.branch(), m.stem(), m.branch(), d.stem(), d.branch(), h.stem(), h.branch());
        var usefulElement = natalCalculation.yongsinAnalysis().yongsinElement();
        var result = new LinkedHashMap<LocalDate, TodayFortuneResponse.DailyFortune>();
        solarRecords.values().stream()
                .filter(record -> record.solarDate().startsWith(year + "-"))
                .forEach(record -> result.put(LocalDate.parse(record.solarDate()),
                        dailyFortune(natal, record, usefulElement)));
        return result;
    }

    private TodayFortuneResponse.DailyFortune dailyFortune(Pillars natal, PillarRecord record, String usefulElement) {
        var day = split(record.dayPillar()); var todayTenGod = tenGod(natal.dayStem(), day.stem());
        int[] scores = {60, 60, 60, 60, 60, 60}; add(scores, switch (todayTenGod) {
            case "정재" -> new int[]{12,5,1,5,3,1}; case "편재" -> new int[]{9,4,0,4,7,0};
            case "정관" -> new int[]{3,5,1,12,5,4}; case "편관" -> new int[]{1,3,-5,8,1,2};
            case "정인" -> new int[]{1,2,6,3,4,12}; case "편인" -> new int[]{0,0,2,1,-1,9}; case "식신" -> new int[]{5,5,8,3,8,4};
            case "상관" -> new int[]{4,3,1,-3,4,7}; case "비견" -> new int[]{1,2,5,4,7,3}; case "겁재" -> new int[]{-7,1,3,2,5,1}; default -> new int[6]; });
        if (STEM_INFO.get(day.stem()).element().equals(usefulElement)) add(scores, new int[]{3,3,5,4,4,4});
        if (BRANCH_ELEMENTS.get(day.branch()).equals(usefulElement)) add(scores, new int[]{3,3,5,4,4,4});
        var interactions = findInteractions(natal, day);
        interactions.forEach(i -> { for (int x = 0; x < 6; x++) scores[x] += i.effect(); });
        for (int x = 0; x < 6; x++) scores[x] = Math.max(0, Math.min(100, scores[x]));
        int overall = (int) Math.floor((java.util.Arrays.stream(scores).average().orElse(0)) + .5d);
        var description = energyDescription(todayTenGod, overall, scores);
        return new TodayFortuneResponse.DailyFortune(overall, scores[0], scores[1], scores[2], scores[3], scores[4], scores[5], day.stem() + day.branch(), todayTenGod, description, interactions);
    }

    private String energyDescription(String tenGod, int overall, int[] scores) {
        var tenGodMessage = switch (tenGod) {
            case "비견" -> "내 기준을 세우고 직접 움직일수록 힘이 실리는 날입니다.";
            case "겁재" -> "경쟁심과 추진력이 커지기 쉬워 선택의 우선순위가 중요한 날입니다.";
            case "식신" -> "여유 있게 표현하고 꾸준히 만들어 가는 일에 흐름이 좋은 날입니다.";
            case "상관" -> "생각을 밖으로 드러내는 힘이 강해지지만 말의 속도는 조절하는 편이 좋습니다.";
            case "편재" -> "새로운 사람이나 기회를 빠르게 알아보는 감각이 살아나는 날입니다.";
            case "정재" -> "현실적인 판단과 차분한 관리가 좋은 결과로 이어지기 쉬운 날입니다.";
            case "편관" -> "긴장과 책임이 커질 수 있어 무리한 정면 돌파보다 순서를 정하는 것이 좋습니다.";
            case "정관" -> "원칙을 지키고 맡은 일을 정돈할수록 신뢰를 얻기 좋은 날입니다.";
            case "편인" -> "익숙한 방식보다 직감과 새로운 관점에서 실마리를 찾기 좋은 날입니다.";
            case "정인" -> "배우고 정리하며 도움을 받아들이는 과정에서 힘을 얻는 날입니다.";
            default -> "평소의 리듬을 지키며 주변의 변화를 살펴보기 좋은 날입니다.";
        };
        var labels = List.of("재물", "애정", "건강", "직업", "관계", "학업");
        int strongest = 0; int weakest = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[strongest]) strongest = i;
            if (scores[i] < scores[weakest]) weakest = i;
        }
        var balanceMessage = scores[weakest] <= 55
                ? " 특히 %s 운은 좋지만, %s에서는 서두르지 않는 편이 좋겠습니다.".formatted(labels.get(strongest), labels.get(weakest))
                : overall >= 70
                ? " 특히 %s 운이 오늘의 흐름을 든든하게 받쳐줍니다.".formatted(labels.get(strongest))
                : " 그중 %s 운을 중심으로 움직이면 하루의 균형을 잡기 좋겠습니다.".formatted(labels.get(strongest));
        return tenGodMessage + balanceMessage;
    }

    private List<TodayFortuneResponse.Interaction> findInteractions(Pillars p, PillarPart today) {
        var result = new ArrayList<TodayFortuneResponse.Interaction>();
        var stems = List.of(new NamedValue("연간", p.yearStem()), new NamedValue("월간", p.monthStem()), new NamedValue("일간", p.dayStem()), new NamedValue("시간", p.hourStem()));
        for (var v : stems) { match(result, STEM_COMBINATIONS, "천간합", v, today.stem(), 3); match(result, STEM_CLASHES, "천간충", v, today.stem(), -3); }
        var branches = List.of(new NamedValue("연지", p.yearBranch()), new NamedValue("월지", p.monthBranch()), new NamedValue("일지", p.dayBranch()), new NamedValue("시지", p.hourBranch()));
        for (var v : branches) { match(result, BRANCH_COMBINATIONS, "지지합", v, today.branch(), 4); match(result, BRANCH_CLASHES, "지지충", v, today.branch(), -5); match(result, BRANCH_PUNISHMENTS, "지지형", v, today.branch(), -3); if (v.value().equals(today.branch()) && SELF_PUNISHMENTS.contains(v.value())) result.add(new TodayFortuneResponse.Interaction("지지형", v.name(), v.value(), today.branch(), -3)); match(result, BRANCH_BREAKS, "지지파", v, today.branch(), -2); match(result, BRANCH_HARMS, "지지해", v, today.branch(), -3); }
        return result;
    }

    private void match(List<TodayFortuneResponse.Interaction> result, Set<String> table, String type, NamedValue natal, String today, int effect) { if (table.contains(pairKey(natal.value(), today))) result.add(new TodayFortuneResponse.Interaction(type, natal.name(), natal.value(), today, effect)); }
    private static void add(int[] scores, int[] effects) { for (int i = 0; i < scores.length; i++) scores[i] += effects[i]; }

    private FortuneCalculationResponse.DaeunAnalysis analyzeDaeun(Pillars p, ZonedDateTime birthDateTime,
                                                                  SolarTermService.SolarTermContext termContext,
                                                                  com.fourpillars.backend.profile.domain.GenderBasis gender) {
        if (gender == null) return null;
        boolean yangYear = YANG_STEMS.contains(p.yearStem());
        boolean forward = yangYear == (gender == com.fourpillars.backend.profile.domain.GenderBasis.MALE);
        long secondsToBoundary = forward
                ? java.time.Duration.between(birthDateTime, termContext.next().dateTime()).getSeconds()
                : java.time.Duration.between(termContext.previous().dateTime(), birthDateTime).getSeconds();
        int startAge = Math.max(0, (int) Math.round(secondsToBoundary / (3d * 86_400d)));
        int monthIndex = ganziIndex(p.monthStem(), p.monthBranch());
        var periods = new ArrayList<FortuneCalculationResponse.DaeunPeriod>();
        for (int i = 1; i <= 8; i++) {
            int idx = Math.floorMod(monthIndex + (forward ? i : -i), 60);
            var stem = STEMS.get(idx % 10); var branch = BRANCHES.get(idx % 12);
            periods.add(new FortuneCalculationResponse.DaeunPeriod(i, startAge + (i - 1) * 10, stem, branch,
                    tenGod(p.dayStem(), stem), tenGod(p.dayStem(), HIDDEN.get(branch).getLast().stem()), twelveStage(p.dayStem(), branch)));
        }
        return new FortuneCalculationResponse.DaeunAnalysis(forward, startAge, periods);
    }

    private FortuneCalculationResponse.BasicAnalysis analyzeBasic(Pillars p) {
        var stems = ordered("year_gan", p.yearStem(), "month_gan", p.monthStem(), "day_gan", p.dayStem(), "hour_gan", p.hourStem());
        var branches = ordered("year_ji", p.yearBranch(), "month_ji", p.monthBranch(), "day_ji", p.dayBranch(), "hour_ji", p.hourBranch());
        var gods = new LinkedHashMap<String, String>();
        stems.forEach((k, v) -> gods.put(k, k.equals("day_gan") ? "본원(나)" : tenGod(p.dayStem(), v)));
        branches.forEach((k, v) -> gods.put(k, tenGod(p.dayStem(), HIDDEN.get(v).getLast().stem())));
        var hidden = new LinkedHashMap<String, List<FortuneCalculationResponse.HiddenStem>>();
        branches.forEach((k, v) -> hidden.put(k, HIDDEN.get(v).stream().map(h -> new FortuneCalculationResponse.HiddenStem(h.stem(), tenGod(p.dayStem(), h.stem()), h.position())).toList()));
        var elements = new LinkedHashMap<String, Integer>(); List.of("목", "화", "토", "금", "수").forEach(e -> elements.put(e, 0));
        stems.values().forEach(v -> elements.compute(STEM_INFO.get(v).element(), (k, n) -> n + 1));
        branches.values().forEach(v -> elements.compute(BRANCH_ELEMENTS.get(v), (k, n) -> n + 1));
        return new FortuneCalculationResponse.BasicAnalysis(p.dayStem(), gods, hidden, elements);
    }

    private FortuneCalculationResponse.StageTwoAnalysis analyzeStageTwo(Pillars p) {
        var branches = ordered("year", p.yearBranch(), "month", p.monthBranch(), "day", p.dayBranch(), "hour", p.hourBranch());
        var stages = new LinkedHashMap<String, String>(); var byYear = new LinkedHashMap<String, String>(); var byDay = new LinkedHashMap<String, String>();
        branches.forEach((k, v) -> { stages.put(k, twelveStage(p.dayStem(), v)); byYear.put(k, sinsal(p.yearBranch(), v)); byDay.put(k, sinsal(p.dayBranch(), v)); });
        return new FortuneCalculationResponse.StageTwoAnalysis(stages, GONGMANG.get(ganziIndex(p.dayStem(), p.dayBranch()) / 10), byYear, byDay);
    }

    private FortuneCalculationResponse.StageThreeAnalysis analyzeStageThree(Pillars p) {
        var stems = ordered("year", p.yearStem(), "month", p.monthStem(), "day", p.dayStem(), "hour", p.hourStem());
        var branches = ordered("year", p.yearBranch(), "month", p.monthBranch(), "day", p.dayBranch(), "hour", p.hourBranch());
        var good = new LinkedHashMap<String, List<String>>(); var caution = new LinkedHashMap<String, List<String>>();
        var cheondeokTarget = CHEONDEOK.get(p.monthBranch());
        var cheondeokAsBranch = CHEONDEOK_BRANCH_MONTHS.contains(p.monthBranch());
        for (var item : branches.entrySet()) {
            var key = item.getKey(); var branch = item.getValue(); var goodHits = new ArrayList<String>(); var cautionHits = new ArrayList<String>();
            if (CHEONEUL.get(p.dayStem()).contains(branch)) goodHits.add("천을귀인");
            if (MUNCHANG.get(p.dayStem()).equals(branch)) goodHits.add("문창귀인");
            if (AMROK.get(p.dayStem()).equals(branch)) goodHits.add("암록");
            if (WOLDEOK.get(TRIO.get(p.monthBranch())).equals(stems.get(key))) goodHits.add("월덕귀인");
            if (cheondeokAsBranch ? cheondeokTarget.equals(branch) : cheondeokTarget.equals(stems.get(key))) goodHits.add("천덕귀인");
            if (YANGIN.get(p.dayStem()) != null && YANGIN.get(p.dayStem()).equals(branch)) cautionHits.add("양인살");
            if (HONGYEOM.get(p.dayStem()).equals(branch)) cautionHits.add("홍염살");
            if (BAEKHO.contains(stems.get(key) + branch)) cautionHits.add("백호살");
            if (GOEGANG.contains(stems.get(key) + branch)) cautionHits.add("괴강살");
            if (!goodHits.isEmpty()) good.put(key, goodHits); if (!cautionHits.isEmpty()) caution.put(key, cautionHits);
        }
        var pairs = new ArrayList<FortuneCalculationResponse.PillarPair>(); var items = new ArrayList<>(branches.entrySet());
        for (int i = 0; i < items.size(); i++) for (int j = i + 1; j < items.size(); j++) if (isWonjin(items.get(i).getValue(), items.get(j).getValue())) pairs.add(new FortuneCalculationResponse.PillarPair(items.get(i).getKey(), items.get(j).getKey()));
        return new FortuneCalculationResponse.StageThreeAnalysis(cheondeokTarget, good, caution, pairs);
    }

    private FortuneCalculationResponse.YongsinAnalysis analyzeYongsin(FortuneCalculationResponse.BasicAnalysis basic) {
        var counts = new LinkedHashMap<String, Integer>(); List.of("비겁", "식상", "재성", "관성", "인성").forEach(v -> counts.put(v, 0));
        basic.tenGods().forEach((key, god) -> {
            var category = CATEGORY.get(god);
            if (key.endsWith("_gan") && category != null) counts.compute(category, (k, n) -> n + 2);
        });
        basic.hiddenStems().forEach((key, stems) -> stems.forEach(hidden -> {
            var category = CATEGORY.get(hidden.tenGod());
            if (category != null) {
                int weight = switch (hidden.position()) { case "정기" -> 3; case "중기" -> 2; default -> 1; };
                if (key.equals("month_ji")) weight *= 2;
                int addition = weight;
                counts.compute(category, (k, n) -> n + addition);
            }
        }));
        int score = counts.get("비겁") + counts.get("인성") - counts.get("식상") - counts.get("재성") - counts.get("관성");
        var dayElement = STEM_INFO.get(basic.dayMaster()).element(); var strong = score >= 0;
        var weakPriority = lowestCategory(counts, "인성", "비겁");
        var strongPriority = lowestCategory(counts, "관성", "식상", "재성");
        var priority = strong ? strongPriority : weakPriority;
        var element = categoryElement(dayElement, priority);
        var reason = strong
                ? "계절과 지장간 가중치를 반영한 간이 균형 분석에서 일간이 신강하여, 견제 범주 중 가장 부족한 " + priority + " 오행을 추천함"
                : "계절과 지장간 가중치를 반영한 간이 균형 분석에서 일간이 신약하여, 보완 범주 중 가장 부족한 " + priority + " 오행을 추천함";
        return new FortuneCalculationResponse.YongsinAnalysis(score, strong ? "신강" : "신약", counts, element, reason, weakPriority, strongPriority);
    }

    private String lowestCategory(Map<String, Integer> counts, String... candidates) {
        var best = candidates[0];
        for (var candidate : candidates) if (counts.get(candidate) < counts.get(best)) best = candidate;
        return best;
    }

    private String categoryElement(String dayElement, String category) {
        return switch (category) {
            case "비겁" -> dayElement;
            case "인성" -> reverse(GENERATES, dayElement);
            case "관성" -> reverse(CONTROLS, dayElement);
            case "식상" -> GENERATES.get(dayElement);
            case "재성" -> CONTROLS.get(dayElement);
            default -> throw new IllegalStateException("알 수 없는 십성 범주: " + category);
        };
    }

    private String tenGod(String dayStem, String targetStem) {
        var day = STEM_INFO.get(dayStem); var target = STEM_INFO.get(targetStem); boolean same = day.yang() == target.yang();
        if (target.element().equals(day.element())) return same ? "비견" : "겁재";
        if (GENERATES.get(day.element()).equals(target.element())) return same ? "식신" : "상관";
        if (GENERATES.get(target.element()).equals(day.element())) return same ? "편인" : "정인";
        if (CONTROLS.get(day.element()).equals(target.element())) return same ? "편재" : "정재";
        return same ? "편관" : "정관";
    }

    private PillarPart calculateHour(String dayStem, LocalTime time) {
        int h = time.getHour(); int index = (h == 23 || h == 0) ? 0 : (h + 1) / 2;
        return new PillarPart(STEMS.get((STEMS.indexOf(FIRST_HOUR_STEM.get(dayStem)) + index) % 10), BRANCHES.get(index));
    }
    private PillarPart yearPillar(int year) {
        int index = Math.floorMod(year - 1984, 60);
        return new PillarPart(STEMS.get(index % 10), BRANCHES.get(index % 12));
    }
    private PillarPart monthPillar(String yearStem, int monthIndex) {
        int firstStem = (STEMS.indexOf(yearStem) % 5 * 2 + 2) % 10;
        return new PillarPart(STEMS.get((firstStem + monthIndex) % 10), BRANCHES.get((2 + monthIndex) % 12));
    }
    private String twelveStage(String stem, String branch) { int start = BRANCHES.indexOf(JANGSAENG.get(stem)), target = BRANCHES.indexOf(branch); int offset = YANG_STEMS.contains(stem) ? Math.floorMod(target - start, 12) : Math.floorMod(start - target, 12); return STAGE_NAMES.get(offset); }
    private String sinsal(String reference, String target) { return SINSAL_NAMES.get(Math.floorMod(BRANCHES.indexOf(target) - BRANCHES.indexOf(GEOBSAL.get(TRIO.get(reference))), 12)); }
    private int ganziIndex(String stem, String branch) { for (int i = 0; i < 60; i++) if (i % 10 == STEMS.indexOf(stem) && i % 12 == BRANCHES.indexOf(branch)) return i; throw new IllegalArgumentException("잘못된 간지 조합"); }
    private boolean isWonjin(String a, String b) { return WONJIN.contains(a + ":" + b) || WONJIN.contains(b + ":" + a); }
    private String reverse(Map<String, String> map, String value) { return map.entrySet().stream().filter(e -> e.getValue().equals(value)).findFirst().orElseThrow().getKey(); }
    private PillarPart split(String value) { var chars = value.trim().codePoints().mapToObj(c -> new String(Character.toChars(c))).toList(); return new PillarPart(chars.get(0), chars.get(1)); }
    private static List<RawHiddenStem> hs(String... values) { var result = new ArrayList<RawHiddenStem>(); for (int i = 0; i < values.length; i += 2) result.add(new RawHiddenStem(values[i], values[i + 1])); return result; }
    private static LinkedHashMap<String, String> ordered(String... values) { var result = new LinkedHashMap<String, String>(); for (int i = 0; i < values.length; i += 2) result.put(values[i], values[i + 1]); return result; }
    private static Set<String> pairSet(String... pairs) { return java.util.Arrays.stream(pairs).map(p -> pairKey(p.substring(0, 1), p.substring(1))).collect(java.util.stream.Collectors.toUnmodifiableSet()); }
    private static String pairKey(String a, String b) { return a.compareTo(b) <= 0 ? a + ":" + b : b + ":" + a; }

    private record StemInfo(String element, boolean yang) {}
    private record RawHiddenStem(String stem, String position) {}
    private record PillarPart(String stem, String branch) {}
    private record Pillars(String yearStem, String yearBranch, String monthStem, String monthBranch, String dayStem, String dayBranch, String hourStem, String hourBranch) {}
    private record PillarRecord(String solarDate, String yearPillar, String monthPillar, String dayPillar, int lunarYear, int lunarMonth, int lunarDay, boolean isLeapMonth) {}
    private record NamedValue(String name, String value) {}
}
