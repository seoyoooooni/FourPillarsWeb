package com.fourpillars.backend.fortune.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_fortunes")
public class DailyFortuneCache {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "fortune_date", nullable = false) private LocalDate fortuneDate;
    private int overall;
    private int wealth;
    private int love;
    private int health;
    private int career;
    private int relationships;
    private int study;
    @Column(name = "today_pillar", nullable = false) private String todayPillar;
    @Column(name = "today_ten_god", nullable = false) private String todayTenGod;
    @Column(name = "energy_description", nullable = false) private String energyDescription;
    @Column(name = "interactions_json", nullable = false) private String interactionsJson;
    @Column(name = "annual_rank", nullable = false) private int annualRank;
    @Column(name = "annual_total_days", nullable = false) private int annualTotalDays;
    @Column(name = "annual_top_percent", nullable = false) private int annualTopPercent;
    @Column(name = "calculation_version", nullable = false) private String calculationVersion;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected DailyFortuneCache() {}

    public DailyFortuneCache(UUID userId, LocalDate fortuneDate, int overall, int wealth, int love, int health,
                             int career, int relationships, int study, String todayPillar, String todayTenGod,
                             String energyDescription, String interactionsJson, int annualRank,
                             int annualTotalDays, int annualTopPercent, String calculationVersion) {
        this.id = UUID.randomUUID(); this.userId = userId; this.fortuneDate = fortuneDate; this.overall = overall;
        this.wealth = wealth; this.love = love; this.health = health; this.career = career;
        this.relationships = relationships; this.study = study; this.todayPillar = todayPillar;
        this.todayTenGod = todayTenGod; this.energyDescription = energyDescription;
        this.interactionsJson = interactionsJson; this.annualRank = annualRank;
        this.annualTotalDays = annualTotalDays; this.annualTopPercent = annualTopPercent;
        this.calculationVersion = calculationVersion; this.createdAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public LocalDate getFortuneDate() { return fortuneDate; }
    public int getOverall() { return overall; }
    public int getWealth() { return wealth; }
    public int getLove() { return love; }
    public int getHealth() { return health; }
    public int getCareer() { return career; }
    public int getRelationships() { return relationships; }
    public int getStudy() { return study; }
    public String getTodayPillar() { return todayPillar; }
    public String getTodayTenGod() { return todayTenGod; }
    public String getEnergyDescription() { return energyDescription; }
    public String getInteractionsJson() { return interactionsJson; }
    public int getAnnualRank() { return annualRank; }
    public int getAnnualTotalDays() { return annualTotalDays; }
    public int getAnnualTopPercent() { return annualTopPercent; }
}
