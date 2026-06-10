package com.vinnilabs.opsmind.adapter.rest;

import com.vinnilabs.opsmind.adapter.rest.mapper.IncidentAnalysisMapper;
import com.vinnilabs.opsmind.adapter.rest.request.IncidentAnalysisRequest;
import com.vinnilabs.opsmind.adapter.rest.response.IncidentAnalysisResponse;
import com.vinnilabs.opsmind.application.usecase.AnalyzeIncidentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final AnalyzeIncidentUseCase analyzeIncidentUseCase;
    private final IncidentAnalysisMapper incidentAnalysisMapper;

    @PostMapping("/analyze")
    public ResponseEntity<IncidentAnalysisResponse> analyze(@Valid @RequestBody IncidentAnalysisRequest request) {
        IncidentAnalysisResponse response = incidentAnalysisMapper.toResponse(
                analyzeIncidentUseCase.analyze(request.getContent())
        );

        return ResponseEntity.ok(response);
    }
}
