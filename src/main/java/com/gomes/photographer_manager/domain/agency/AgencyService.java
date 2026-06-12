package com.gomes.photographer_manager.domain.agency;

import com.gomes.photographer_manager.domain.agency.request.AgencyRequest;
import com.gomes.photographer_manager.domain.agency.response.AgencyResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgencyService {

    private final AgencyRepository repository;

    public AgencyService(AgencyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AgencyResponse create(AgencyRequest request, String userId) {
        if (repository.existsByUserId(userId)) {
            throw new DataIntegrityViolationException("Já existe uma agência cadastrada para este usuário");
        }
        Agency agency = new Agency(request, userId);
        return new AgencyResponse(repository.save(agency));
    }

    @Transactional(readOnly = true)
    public AgencyResponse findByUserId(String userId) {
        return new AgencyResponse(findEntity(userId));
    }

    @Transactional
    public AgencyResponse update(String userId, AgencyRequest request) {
        Agency agency = findEntity(userId);
        agency.update(request);
        return new AgencyResponse(repository.save(agency));
    }

    @Transactional
    public void delete(String userId) {
        Agency agency = findEntity(userId);
        repository.delete(agency);
    }

    private Agency findEntity(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found for user id: " + userId));
    }
}
