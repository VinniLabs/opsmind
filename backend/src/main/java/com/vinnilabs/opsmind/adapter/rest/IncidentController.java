package com.vinnilabs.opsmind.adapter.rest;

import com.vinnilabs.opsmind.adapter.rest.mapper.IncidentAnalysisMapper;
import com.vinnilabs.opsmind.adapter.rest.mapper.IncidentMapper;
import com.vinnilabs.opsmind.adapter.rest.request.CreateIncidentRequest;
import com.vinnilabs.opsmind.adapter.rest.request.IncidentAnalysisRequest;
import com.vinnilabs.opsmind.adapter.rest.request.UpdateIncidentAnalysisRequest;
import com.vinnilabs.opsmind.adapter.rest.request.UpdateIncidentStatusRequest;
import com.vinnilabs.opsmind.adapter.rest.request.ValidateIncidentRequest;
import com.vinnilabs.opsmind.adapter.rest.response.IncidentAnalysisResponse;
import com.vinnilabs.opsmind.adapter.rest.response.IncidentResponse;
import com.vinnilabs.opsmind.application.domain.IncidentStatus;
import com.vinnilabs.opsmind.application.exception.InvalidStatusValueException;
import com.vinnilabs.opsmind.application.usecase.AdvanceIncidentStatusUseCase;
import com.vinnilabs.opsmind.application.usecase.AnalyzeIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.CreateIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.GetIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.ListIncidentsUseCase;
import com.vinnilabs.opsmind.application.usecase.ReanalyzeIncidentUseCase;
import com.vinnilabs.opsmind.application.usecase.UpdateIncidentAnalysisUseCase;
import com.vinnilabs.opsmind.application.usecase.ValidateIncidentDiagnosisUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final AnalyzeIncidentUseCase analyzeIncidentUseCase;
    private final IncidentAnalysisMapper incidentAnalysisMapper;

    private final CreateIncidentUseCase createIncidentUseCase;
    private final ListIncidentsUseCase listIncidentsUseCase;
    private final GetIncidentUseCase getIncidentUseCase;
    private final AdvanceIncidentStatusUseCase advanceIncidentStatusUseCase;
    private final UpdateIncidentAnalysisUseCase updateIncidentAnalysisUseCase;
    private final ReanalyzeIncidentUseCase reanalyzeIncidentUseCase;
    private final com.vinnilabs.opsmind.application.usecase.ValidateIncidentDiagnosisUseCase validateIncidentDiagnosisUseCase;
    private final IncidentMapper incidentMapper;

    @PostMapping("/analyze")
    public ResponseEntity<IncidentAnalysisResponse> analyze(@Valid @RequestBody IncidentAnalysisRequest request) {
        IncidentAnalysisResponse response = incidentAnalysisMapper.toResponse(
                analyzeIncidentUseCase.analyze(request.getContent())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody CreateIncidentRequest request) {
        IncidentResponse response = incidentMapper.toResponse(
                createIncidentUseCase.create(
                        request.getTitle(),
                        request.getDescription(),
                        request.getErrorLog()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> list() {
        List<IncidentResponse> response = listIncidentsUseCase.list().stream()
                .map(incidentMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(incidentMapper.toResponse(getIncidentUseCase.getById(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IncidentResponse> updateStatus(@PathVariable String id,
                                                         @Valid @RequestBody UpdateIncidentStatusRequest request) {
        IncidentResponse response = incidentMapper.toResponse(
                advanceIncidentStatusUseCase.advanceStatus(id, parseStatus(request.getStatus()))
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/analysis")
    public ResponseEntity<IncidentResponse> updateAnalysis(@PathVariable String id,
                                                           @Valid @RequestBody UpdateIncidentAnalysisRequest request) {
        IncidentResponse response = incidentMapper.toResponse(
                updateIncidentAnalysisUseCase.updateAnalysis(
                        id,
                        request.getAiDiagnosis(),
                        request.getRealRootCause()
                )
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reanalyze")
    public ResponseEntity<IncidentResponse> reanalyze(@PathVariable String id) {
        IncidentResponse response = incidentMapper.toResponse(
                reanalyzeIncidentUseCase.reanalyze(id)
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/validation")
    public ResponseEntity<IncidentResponse> validate(@PathVariable String id,
                                                     @Valid @RequestBody ValidateIncidentRequest request) {
        IncidentResponse response = incidentMapper.toResponse(
                validateIncidentDiagnosisUseCase.validateDiagnosis(
                        id,
                        request.getRealRootCause(),
                        request.getAppliedSolution(),
                        parseDiagnosisAccuracy(request.getDiagnosisCorrect()),
                        request.getValidationNotes()
                )
        );

        return ResponseEntity.ok(response);
    }

    private IncidentStatus parseStatus(String status) {
        try {
            return IncidentStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new InvalidStatusValueException(status);
        }
    }

    private com.vinnilabs.opsmind.application.domain.DiagnosisAccuracy parseDiagnosisAccuracy(String value) {
        try {
            return com.vinnilabs.opsmind.application.domain.DiagnosisAccuracy.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new com.vinnilabs.opsmind.application.exception.InvalidDiagnosisAccuracyValueException(value);
        }
    }
}
