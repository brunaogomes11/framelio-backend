package com.gomes.photographer_manager.domain.photographer.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    List<TeamMember> findByPhotographerIdOrderByCreatedAtAsc(String photographerId);
    void deleteByIdAndPhotographerId(String id, String photographerId);
    long countByPhotographerId(String photographerId);
}
