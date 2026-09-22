package com.fourpillars.backend.fortune.dto;

import java.util.List;
import java.util.Map;

public record FortuneCalculationResponse(
        String solarDate,
        LunarDate lunarDate,
        Map<String, String> pillars,
        BasicAnalysis analysis,
        StageTwoAnalysis stageTwoAnalysis,
        StageThreeAnalysis stageThreeAnalysis,
        YongsinAnalysis yongsinAnalysis,
        DaeunAnalysis daeunAnalysis) {

    // 대운 없이(성별 미지정 등) 기존 7개 필드만으로 만들던 코드/테스트와의 호환용.
    public FortuneCalculationResponse(String solarDate, LunarDate lunarDate, Map<String, String> pillars,
                                      BasicAnalysis analysis, StageTwoAnalysis stageTwoAnalysis,
                                      StageThreeAnalysis stageThreeAnalysis, YongsinAnalysis yongsinAnalysis) {
        this(solarDate, lunarDate, pillars, analysis, stageTwoAnalysis, stageThreeAnalysis, yongsinAnalysis, null);
    }

    public record LunarDate(int year, int month, int day, boolean leapMonth) {}
    public record HiddenStem(String stem, String tenGod, String position) {}
    public record PillarPair(String first, String second) {}
    public record BasicAnalysis(String dayMaster, Map<String, String> tenGods,
                                Map<String, List<HiddenStem>> hiddenStems, Map<String, Integer> elementCounts) {}
    public record StageTwoAnalysis(Map<String, String> twelveStages, List<String> gongmang,
                                   Map<String, String> twelveSinsalByYearBranch,
                                   Map<String, String> twelveSinsalByDayBranch) {}
    public record StageThreeAnalysis(String cheondeokValue, Map<String, List<String>> gilsinByPillar,
                                     Map<String, List<String>> hyungsalByPillar, List<PillarPair> wonjinPairs) {}
    public record YongsinAnalysis(int strengthScore, String strength, Map<String, Integer> categoryCounts,
                                  String yongsinElement, String yongsinReason,
                                  String weakPriority, String strongPriority) {}
    // 대운 하나의 정보. startAge는 이 대운이 시작되는 나이(만 나이 근사, 일 단위 계산이라 시간 단위 절입 오차는 반영 안 됨).
    public record DaeunPeriod(int order, int startAge, String stem, String branch,
                              String stemTenGod, String branchTenGod, String twelveStage) {}
    // forward: 순행 여부(연간 음양·성별 기준). startAge: 첫 대운이 시작하는 나이(대운수).
    // gender가 없으면(성별 미지정) 이 필드 전체가 null로 내려감.
    public record DaeunAnalysis(boolean forward, int startAge, List<DaeunPeriod> periods) {}
}
