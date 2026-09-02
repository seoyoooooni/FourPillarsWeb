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
        YongsinAnalysis yongsinAnalysis) {

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
}
