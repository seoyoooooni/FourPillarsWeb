// 회원 식별자로 출생 프로필을 저장하고 조회함.
package com.fourpillars.backend.profile.repository;

import com.fourpillars.backend.profile.domain.BirthProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BirthProfileRepository extends JpaRepository<BirthProfile, UUID> {
}
