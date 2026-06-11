package com.vinnilabs.opsmind.application.service;

import com.vinnilabs.opsmind.application.domain.Incident;
import com.vinnilabs.opsmind.application.domain.DiagnosisAccuracy;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.domain.IncidentStatus;
import com.vinnilabs.opsmind.application.exception.IncidentNotFoundException;
import com.vinnilabs.opsmind.application.exception.InvalidStatusTransitionException;
import com.vinnilabs.opsmind.application.repository.IncidentRepository;
import com.vinnilabs.opsmind.application.usecase.AdvanceIncidentStatusUseCase;
import com.vinnilabs.opsmind.application.usecase.AnalyzeIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.CreateIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.GetIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.ListIncidentsUseCase;
import com.vinnilabs.opsmind.application.usecase.ReanalyzeIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.UpdateIncidentAnalysisUseCase;
import com.vinnilabs.opsmind.application.usecase.ValidateIncidentDiagnosisUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IncidentManagementService implements
        CreateIncidentUseCase,
        ListIncidentsUseCase,
        GetIncidentUseCase,
        AdvanceIncidentStatusUseCase,
        UpdateIncidentAnalysisUseCase,
        ReanalyzeIncidentUseCase,
        ValidateIncidentDiagnosisUseCase {

    private static final Logger log = LoggerFactory.getLogger(IncidentManagementService.class);

    private final IncidentRepository incidentRepository;
    private final AnalyzeIncidentUseCase analyzeIncidentUseCase;

    @Override
    public Incident create(String title, String description, String errorLog) {
        log.info("Creating incident title={}", title);

        LocalDateTime now = LocalDateTime.now();
        Incident incident = Incident.builder()
                .title(title)
                .description(description)
                .errorLog(errorLog)
                .status(IncidentStatus.NOVO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Incident saved = incidentRepository.save(incident);

        log.info("Incident created incidentId={} status={}", saved.getId(), saved.getStatus());

        return saved;
    }

    @Override
    public List<Incident> list() {
        return incidentRepository.findAll();
    }

    @Override
    public Incident getById(String id) {
        return findOrThrow(id);
    }

    @Override
    public Incident advanceStatus(String id, IncidentStatus newStatus) {
        log.info("Advancing incident status incidentId={} newStatus={}", id, newStatus);

        Incident incident = findOrThrow(id);

        if (!incident.getStatus().canTransitionTo(newStatus)) {
            log.warn("Invalid status transition incidentId={} from={} to={}",
                    id, incident.getStatus(), newStatus);
            throw new InvalidStatusTransitionException(incident.getStatus(), newStatus);
        }

        Incident updated;
        if (incident.getStatus() == IncidentStatus.EM_ANALISE && newStatus == IncidentStatus.DIAGNOSTICADO) {
            String content = buildAnalysisContent(incident);
            IncidentAnalysis analysis = analyzeIncidentUseCase.analyze(content);

            updated = applyAnalysis(incident.toBuilder(), analysis)
                    .status(newStatus)
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {
            updated = incident.toBuilder()
                    .status(newStatus)
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        Incident saved = incidentRepository.save(updated);

        log.info("Incident status advanced incidentId={} status={}", saved.getId(), saved.getStatus());

        return saved;
    }

    @Override
    public Incident updateAnalysis(String id, String aiDiagnosis, String realRootCause) {
        log.info("Updating incident analysis incidentId={}", id);

        Incident incident = findOrThrow(id);

        Incident updated = incident.toBuilder()
                .aiDiagnosis(aiDiagnosis)
                .realRootCause(realRootCause)
                .updatedAt(LocalDateTime.now())
                .build();

        return incidentRepository.save(updated);
    }

    @Override
    public Incident validateDiagnosis(String id,
                                      String realRootCause,
                                      String appliedSolution,
                                      DiagnosisAccuracy diagnosisCorrect,
                                      String validationNotes) {
        log.info("Validating incident diagnosis incidentId={} diagnosisCorrect={}", id, diagnosisCorrect);

        Incident incident = findOrThrow(id);

        // Human validation only updates the validation fields; status is not advanced automatically.
        Incident updated = incident.toBuilder()
                .realRootCause(realRootCause)
                .appliedSolution(appliedSolution)
                .diagnosisCorrect(diagnosisCorrect)
                .validationNotes(validationNotes)
                .validatedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Incident saved = incidentRepository.save(updated);

        log.info("Incident diagnosis validated incidentId={} validatedAt={}", saved.getId(), saved.getValidatedAt());

        return saved;
    }

    @Override
    public Incident reanalyze(String id) {
        log.info("Reanalyzing incident incidentId={}", id);

        Incident incident = findOrThrow(id);

        String content = buildAnalysisContent(incident);
        IncidentAnalysis analysis = analyzeIncidentUseCase.analyze(content);

        Incident updated = applyAnalysis(incident.toBuilder(), analysis)
                .updatedAt(LocalDateTime.now())
                .build();

        Incident saved = incidentRepository.save(updated);

        log.info("Incident reanalysis completed incidentId={} status={}", saved.getId(), saved.getStatus());

        return saved;
    }

    /**
     * Populates the structured AI fields on the incident from the analysis result,
     * keeping the aggregated {@code aiDiagnosis} for backward compatibility.
     */
    private Incident.IncidentBuilder applyAnalysis(Incident.IncidentBuilder builder, IncidentAnalysis analysis) {
        return builder
                .aiDiagnosis(formatDiagnosis(analysis))
                .aiSeverity(analysis.getSeverity())
                .aiRootCause(analysis.getRootCause())
                .aiImpact(analysis.getImpact())
                .aiProposedSolution(analysis.getRecommendation())
                .aiProvider(analysis.getProvider());
    }

    private String buildAnalysisContent(Incident incident) {
        String description = Objects.toString(incident.getDescription(), "").trim();
        String errorLog = Objects.toString(incident.getErrorLog(), "").trim();

        if (errorLog.isEmpty()) {
            return description;
        }
        if (description.isEmpty()) {
            return errorLog;
        }
        return description + "\n\n" + errorLog;
    }

    private String formatDiagnosis(IncidentAnalysis analysis) {
        return "Severity: " + analysis.getSeverity() + "\n"
                + "Root cause: " + analysis.getRootCause() + "\n"
                + "Impact: " + analysis.getImpact() + "\n"
                + "Recommendation: " + analysis.getRecommendation();
    }

    private Incident findOrThrow(String id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));
    }
}
