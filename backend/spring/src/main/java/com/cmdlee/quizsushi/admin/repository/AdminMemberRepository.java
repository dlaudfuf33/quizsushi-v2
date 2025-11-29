package com.cmdlee.quizsushi.admin.repository;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.domain.model.enums.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminMemberRepository extends JpaRepository<AdminMember, Long> {
    Optional<AdminMember> findByUsername(String username);

    boolean existsByRole(AdminRole role);
}
