package com.gomes.photographer_manager.domain.team;

import com.gomes.photographer_manager.domain.team.request.TeamRequest;
import com.gomes.photographer_manager.domain.team.response.TeamResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository repository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        List<User> members = resolveMembers(request.memberIds());
        var team = new Team(request, members);
        return new TeamResponse(repository.save(team));
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(String id) {
        return new TeamResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> listAll() {
        return repository.findAll().stream()
                .map(TeamResponse::new)
                .toList();
    }

    @Transactional
    public TeamResponse update(String id, TeamRequest request) {
        var team = findEntity(id);
        List<User> newMembers = resolveMembers(request.memberIds());
        team.update(request, newMembers);
        return new TeamResponse(repository.save(team));
    }

    @Transactional
    public void delete(String id) {
        var team = findEntity(id);
        repository.delete(team);
    }

    private Team findEntity(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
    }

    private List<User> resolveMembers(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return userRepository.findAllById(ids);
    }
}
