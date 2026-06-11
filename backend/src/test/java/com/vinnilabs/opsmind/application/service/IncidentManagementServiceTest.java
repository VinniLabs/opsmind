package com.vinnilabs.opsmind.application.service;

import com.vinnilabs.opsmind.application.domain.Incident;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.domain.IncidentSeverity;
import com.vinnilabs.opsmind.application.domain.IncidentStatus;
import com.vinnilabs.opsmind.application.exception.IncidentNotFoundException;
import com.vinnilabs.opsmind.application.exception.InvalidStatusTransitionException;
import com.vinnilabs.opsmind.application.repository.IncidentRepository;
import com.vinnilabs.opsmind.application.usecase.AnalyzeIncidentUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentManagementServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AnalyzeIncidentUseCase analyzeIncidentUseCase;

    @InjectMocks
    private IncidentManagementService service;

    @Test
    void shouldCreateIncidentWithStatusNovo() {
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create("Outage", "API down", "stacktrace");

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(captor.capture());

        Incident saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(IncidentStatus.NOVO);
        assertThat(saved.getTitle()).isEqualTo("Outage");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldAdvanceStatusToNextStep() {
        Incident existing = baseIncident(IncidentStatus.NOVO);
        when(incidentRepository.findById("1")).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        Incident result = service.advanceStatus("1", IncidentStatus.EM_ANALISE);

        assertThat(result.getStatus()).isEqualTo(IncidentStatus.EM_ANALISE);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(analyzeIncidentUseCase, never()).analyze(any(String.class));
    }

    @Test
    void shouldRunAiAnalysisWhenAdvancingToDiagnosticated() {
        Incident existing = baseIncident(IncidentStatus.EM_ANALISE);
        when(incidentRepository.findById("1")).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));
        when(analyzeIncidentUseCase.analyze(eq("API down\n\nstacktrace")))
                .thenReturn(IncidentAnalysis.builder()
                        .severity(IncidentSeverity.HIGH)
                        .rootCause("Database connection pool exhausted")
                        .impact("Service unavailable")
                        .recommendation("Increase pool and investigate slow queries")
                        .build());

        Incident result = service.advanceStatus("1", IncidentStatus.DIAGNOSTICADO);

        assertThat(result.getStatus()).isEqualTo(IncidentStatus.DIAGNOSTICADO);
        assertThat(result.getAiDiagnosis()).contains("Severity: HIGH");
        assertThat(result.getAiDiagnosis()).contains("Root cause: Database connection pool exhausted");
        assertThat(result.getAiDiagnosis()).contains("Impact: Service unavailable");
        assertThat(result.getAiDiagnosis()).contains("Recommendation: Increase pool and investigate slow queries");
    }

    @Test
    void shouldNotAdvanceToDiagnosticatedWhenAiFails() {
        Incident existing = baseIncident(IncidentStatus.EM_ANALISE);
        when(incidentRepository.findById("1")).thenReturn(Optional.of(existing));
        when(analyzeIncidentUseCase.analyze(any(String.class))).thenThrow(new RuntimeException("AI unavailable"));

        assertThatThrownBy(() -> service.advanceStatus("1", IncidentStatus.DIAGNOSTICADO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AI unavailable");

        verify(incidentRepository, never()).save(any(Incident.class));
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Incident existing = baseIncident(IncidentStatus.NOVO);
        when(incidentRepository.findById("1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.advanceStatus("1", IncidentStatus.RESOLVIDO))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(incidentRepository, never()).save(any(Incident.class));
    }

    @Test
    void shouldThrowWhenIncidentNotFound() {
        when(incidentRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("404"))
                .isInstanceOf(IncidentNotFoundException.class);
    }

    @Test
    void shouldUpdateAnalysis() {
        Incident existing = baseIncident(IncidentStatus.DIAGNOSTICADO);
        when(incidentRepository.findById("1")).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        Incident result = service.updateAnalysis("1", "AI says X", "Real cause Y");

        assertThat(result.getAiDiagnosis()).isEqualTo("AI says X");
        assertThat(result.getRealRootCause()).isEqualTo("Real cause Y");
        assertThat(result.getStatus()).isEqualTo(IncidentStatus.DIAGNOSTICADO);
    }

    @Test
    void shouldListIncidents() {
        when(incidentRepository.findAll()).thenReturn(List.of(baseIncident(IncidentStatus.NOVO)));

        assertThat(service.list()).hasSize(1);
    }

    private Incident baseIncident(IncidentStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Incident.builder()
                .id("1")
                .title("Outage")
                .description("API down")
                .errorLog("stacktrace")
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
