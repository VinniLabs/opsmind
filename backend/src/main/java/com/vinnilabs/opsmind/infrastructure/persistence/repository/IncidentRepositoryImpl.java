package com.vinnilabs.opsmind.infrastructure.persistence.repository;

import com.vinnilabs.opsmind.application.domain.Incident;
import com.vinnilabs.opsmind.application.repository.IncidentRepository;
import com.vinnilabs.opsmind.infrastructure.persistence.entity.IncidentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IncidentRepositoryImpl implements IncidentRepository {

    private final IncidentMongoRepository mongoRepository;

    @Override
    public Incident save(Incident incident) {
        IncidentEntity saved = mongoRepository.save(toEntity(incident));
        return toDomain(saved);
    }

    @Override
    public Optional<Incident> findById(String id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Incident> findAll() {
        return mongoRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private IncidentEntity toEntity(Incident incident) {
        return IncidentEntity.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .errorLog(incident.getErrorLog())
                .status(incident.getStatus())
                .aiDiagnosis(incident.getAiDiagnosis())
                .aiSeverity(incident.getAiSeverity())
                .aiRootCause(incident.getAiRootCause())
                .aiImpact(incident.getAiImpact())
                .aiProposedSolution(incident.getAiProposedSolution())
                .aiProvider(incident.getAiProvider())
                .realRootCause(incident.getRealRootCause())
                .appliedSolution(incident.getAppliedSolution())
                .diagnosisCorrect(incident.getDiagnosisCorrect())
                .validationNotes(incident.getValidationNotes())
                .validatedAt(incident.getValidatedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    private Incident toDomain(IncidentEntity entity) {
        return Incident.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .errorLog(entity.getErrorLog())
                .status(entity.getStatus())
                .aiDiagnosis(entity.getAiDiagnosis())
                .aiSeverity(entity.getAiSeverity())
                .aiRootCause(entity.getAiRootCause())
                .aiImpact(entity.getAiImpact())
                .aiProposedSolution(entity.getAiProposedSolution())
                .aiProvider(entity.getAiProvider())
                .realRootCause(entity.getRealRootCause())
                .appliedSolution(entity.getAppliedSolution())
                .diagnosisCorrect(entity.getDiagnosisCorrect())
                .validationNotes(entity.getValidationNotes())
                .validatedAt(entity.getValidatedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

