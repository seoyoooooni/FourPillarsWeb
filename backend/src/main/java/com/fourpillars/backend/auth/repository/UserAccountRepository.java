// 회원의 정규화 이메일 중복 확인과 저장·조회 기능을 제공함.
package com.fourpillars.backend.auth.repository;

import com.fourpillars.backend.auth.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    boolean existsByEmailNormalized(String emailNormalized);

    Optional<UserAccount> findByEmailNormalized(String emailNormalized);
}
