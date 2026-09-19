package com.fourpillars.backend.fortune.service;

import com.fourpillars.backend.profile.dto.BirthProfileResponse;
import java.util.UUID;

public record ProfileFortuneRefreshRequested(UUID userId, BirthProfileResponse profile) {}
