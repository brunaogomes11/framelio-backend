package com.gomes.photographer_manager.domain.photographer.team;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamMemberService {

    private final TeamMemberRepository repository;

    public TeamMemberService(TeamMemberRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> findByPhotographer(String photographerId) {
        return repository.findByPhotographerIdOrderByCreatedAtAsc(photographerId).stream()
                .map(TeamMemberResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamMember findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Colaborador não encontrado para ID: " + id));
    }

    @Transactional
    public TeamMemberResponse create(TeamMemberRequest request, String photographerId) {
        TeamMember member = new TeamMember(request, photographerId);
        return new TeamMemberResponse(repository.save(member));
    }

    @Transactional
    public TeamMemberResponse update(String id, TeamMemberRequest request, String photographerId) {
        TeamMember member = findOwned(id, photographerId);
        member.update(request);
        return new TeamMemberResponse(repository.save(member));
    }

    @Transactional
    public void delete(String id, String photographerId) {
        findOwned(id, photographerId);
        repository.deleteByIdAndPhotographerId(id, photographerId);
    }

    private TeamMember findOwned(String id, String photographerId) {
        TeamMember member = findById(id);
        if (!member.getPhotographerId().equals(photographerId)) {
            throw new DataIntegrityViolationException("O colaborador não pertence ao fotógrafo autenticado");
        }
        return member;
    }
}
