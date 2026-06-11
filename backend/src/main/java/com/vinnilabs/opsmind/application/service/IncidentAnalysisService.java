package com.vinnilabs.opsmind.application.service;

import com.vinnilabs.opsmind.application.ai.AiProviderRouter;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.domain.SolutionKnowledgeBase;
import com.vinnilabs.opsmind.application.repository.IncidentAnalysisRepository;
import com.vinnilabs.opsmind.application.repository.SolutionKnowledgeBaseRepository;
import com.vinnilabs.opsmind.application.usecase.AnalyzeIncidentUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentAnalysisService implements AnalyzeIncidentUseCase {

    private static final Logger log = LoggerFactory.getLogger(IncidentAnalysisService.class);

    private final AiProviderRouter aiProviderRouter;
    private final IncidentAnalysisRepository incidentAnalysisRepository;
    private final SolutionKnowledgeBaseRepository solutionKnowledgeBaseRepository;
    private final IncidentContextBuilder contextBuilder;

    @Override
    public IncidentAnalysis analyze(String content) {
        log.info("Starting incident analysis contentLength={}", content.length());

        List<IncidentAnalysis> similarIncidents = incidentAnalysisRepository.findSimilar(content);
        List<SolutionKnowledgeBase> validatedSolutions = solutionKnowledgeBaseRepository.findSimilar(content);

        log.info("Context enriched similarIncidents={} validatedSolutions={}",
                similarIncidents.size(), validatedSolutions.size());

        String enrichedContext = contextBuilder.build(content, similarIncidents, validatedSolutions);

        IncidentAnalysis analysis = aiProviderRouter.analyze(enrichedContext);
        IncidentAnalysis saved = incidentAnalysisRepository.save(content, analysis);

        log.info("Incident analysis completed analysisId={} severity={}",
                saved.getId(), saved.getSeverity());

        return saved;
    }
}
