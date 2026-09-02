// 로그인 회원의 출생 프로필을 조회하거나 새 값으로 저장함.
package com.fourpillars.backend.profile.service;

import com.fourpillars.backend.auth.repository.UserAccountRepository;
import com.fourpillars.backend.profile.domain.BirthProfile;
import com.fourpillars.backend.profile.dto.BirthProfileRequest;
import com.fourpillars.backend.profile.dto.BirthProfileResponse;
import com.fourpillars.backend.profile.exception.ProfileNotFoundException;
import com.fourpillars.backend.profile.repository.BirthProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BirthProfileService {
    private final BirthProfileRepository profileRepository;
    private final UserAccountRepository userRepository;

    public BirthProfileService(BirthProfileRepository profileRepository, UserAccountRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BirthProfileResponse get(UUID userId) {
        return profileRepository.findById(userId).map(BirthProfileResponse::from)
                .orElseThrow(ProfileNotFoundException::new);
    }

    @Transactional
    public BirthProfileResponse save(UUID userId, BirthProfileRequest request) {
        var profile = profileRepository.findById(userId).orElseGet(() ->
                new BirthProfile(userRepository.getReferenceById(userId)));
        profile.update(request.displayName(), request.birthDate(), request.birthTime(), request.calendarType(),
                request.leapMonth(), request.gender());
        return BirthProfileResponse.from(profileRepository.save(profile));
    }
}
