package com.vinnilabs.opsmind.application.service;

import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.domain.SolutionKnowledgeBase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds an enriched context block to be sent to the AI,
 * combining the current incident with historical incidents and validated solutions.
 */
@Component
class IncidentContextBuilder {

    String build(String rawContent,
                 List<IncidentAnalysis> similarIncidents,
                 List<SolutionKnowledgeBase> validatedSolutions) {

        var sb = new StringBuilder();

        sb.append("Conteúdo do incidente atual:\n").append(rawContent);

        if (!similarIncidents.isEmpty()) {
            sb.append("\n\n--- Incidentes similares do histórico (use como referência) ---\n");
            for (int i = 0; i < similarIncidents.size(); i++) {
                IncidentAnalysis h = similarIncidents.get(i);
                sb.append("\n[Incidente ").append(i + 1).append("]\n");
                sb.append("Severidade: ").append(h.getSeverity()).append("\n");
                sb.append("Causa raiz: ").append(h.getRootCause()).append("\n");
                sb.append("Impacto: ").append(h.getImpact()).append("\n");
                sb.append("Recomendação: ").append(h.getRecommendation()).append("\n");
            }
        }

        if (!validatedSolutions.isEmpty()) {
            sb.append("\n\n--- Soluções validadas da base de conhecimento (use como referência) ---\n");
            for (int i = 0; i < validatedSolutions.size(); i++) {
                SolutionKnowledgeBase s = validatedSolutions.get(i);
                sb.append("\n[Solução ").append(i + 1).append("]\n");
                sb.append("Título: ").append(s.getTitle()).append("\n");
                sb.append("Categoria: ").append(s.getCategory()).append("\n");
                sb.append("Sintomas: ").append(s.getSymptoms()).append("\n");
                sb.append("Causa confirmada: ").append(s.getConfirmedRootCause()).append("\n");
                sb.append("Solução: ").append(s.getSolution()).append("\n");
                if (s.getTags() != null && !s.getTags().isEmpty()) {
                    sb.append("Tags: ").append(String.join(", ", s.getTags())).append("\n");
                }
            }
        }

        return sb.toString();
    }
}

