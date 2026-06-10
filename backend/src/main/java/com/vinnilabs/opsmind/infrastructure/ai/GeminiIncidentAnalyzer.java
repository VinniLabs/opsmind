package com.vinnilabs.opsmind.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinnilabs.opsmind.application.ai.AiIncidentAnalyzer;
import com.vinnilabs.opsmind.application.domain.IncidentAnalysis;
import com.vinnilabs.opsmind.application.domain.IncidentSeverity;
import com.vinnilabs.opsmind.application.exception.AiAnalysisException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiIncidentAnalyzer implements AiIncidentAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GeminiIncidentAnalyzer.class);

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${opsmind.ai.gemini.api-key}")
    private String apiKey;

    @Value("${opsmind.ai.gemini.model}")
    private String model;

    @Value("${opsmind.ai.gemini.url}")
    private String baseUrl;

    @Value("${opsmind.ai.response-language:pt-BR}")
    private String responseLanguage;

    @Override
    public IncidentAnalysis analyze(String content) {
        log.info("Starting AI incident analysis model={}", model);

        try {
            String prompt = buildPrompt(content);

            GeminiRequest request = new GeminiRequest(
                    List.of(new Content(List.of(new Part(prompt))))
            );

            GeminiResponse response = restClientBuilder.build()
                    .post()
                    .uri("%s/%s:generateContent?key=%s".formatted(baseUrl, model, apiKey))
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            String text = response.candidates()
                    .getFirst()
                    .content()
                    .parts()
                    .getFirst()
                    .text();

            log.debug("AI response received length={}", text.length());

            AiAnalysisResponse analysis = objectMapper.readValue(cleanJson(text), AiAnalysisResponse.class);
            IncidentSeverity severity = resolveSeverity(analysis.severity());

            log.info("AI incident analysis completed severity={}", severity);

            return IncidentAnalysis.builder()
                    .severity(severity)
                    .rootCause(analysis.rootCause())
                    .impact(analysis.impact())
                    .recommendation(analysis.recommendation())
                    .build();

        } catch (Exception exception) {
            log.error("AI incident analysis failed model={}", model, exception);
            throw new AiAnalysisException(exception);
        }
    }

    private String buildPrompt(String content) {
        return """
                Você é um especialista SRE e analista de incidentes de produção.

                Idioma obrigatório da resposta: %s.
                Todos os campos do JSON devem ser respondidos nesse idioma.

                Analise apenas problemas relacionados a desenvolvimento de software,
                sustentação, APIs, logs, banco de dados, cloud, filas, containers,
                microsserviços, integrações, deploy, build, configuração e infraestrutura de aplicação.

                Se o conteúdo não estiver relacionado a problemas técnicos de software,
                retorne severity LOW e informe que o conteúdo está fora do escopo técnico.

                Regras de severidade:
                - CRITICAL: use apenas para indisponibilidade total de sistema crítico, perda de dados, falha financeira grave, filas críticas paradas, banco indisponível ou incidente grave em produção.
                - HIGH: use para erros que impedem inicialização da aplicação, falhas importantes em APIs, exceptions recorrentes, falhas de integração ou degradação relevante.
                - MEDIUM: use para erros de regra de negócio, duplicidade, validação, inconsistência pontual ou falha com workaround.
                - LOW: use para alertas informativos, problemas sem impacto relevante ou conteúdo fora do escopo.

                Quando incidentes similares do histórico ou soluções validadas forem fornecidos,
                use-os como referência para enriquecer a análise, mas não os trate como verdade absoluta.
                Sempre avalie o contexto atual e produza uma análise independente e fundamentada.

                Responda obrigatoriamente apenas com JSON válido.

                Regras obrigatórias:
                - Não utilizar markdown.
                - Não utilizar blocos ```json.
                - Não utilizar quebras de linha dentro dos valores.
                - Todos os campos devem ser uma única linha.
                - Escapar corretamente aspas duplas.
                - Retornar somente o JSON.
                - Não usar listas em markdown.
                - Não usar enumerações com quebra de linha.
                - Não usar aspas duplas dentro dos textos dos campos.
                - A recomendação deve ter no máximo 5 ações objetivas.

                Formato obrigatório:

                {
                  "severity": "LOW|MEDIUM|HIGH|CRITICAL",
                  "rootCause": "causa raiz provável em uma única linha",
                  "impact": "impacto técnico e de negócio em uma única linha",
                  "recommendation": "ações recomendadas em uma única linha"
                }

                Conteúdo para análise:

                %s
                """.formatted(responseLanguage, content);
    }

    private String cleanJson(String text) {
        return text
                .replace("```json", "")
                .replace("```", "")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    private IncidentSeverity resolveSeverity(String severity) {
        try {
            return IncidentSeverity.valueOf(severity.toUpperCase());
        } catch (Exception exception) {
            log.warn("Unrecognized severity value={}, defaulting to MEDIUM", severity);
            return IncidentSeverity.MEDIUM;
        }
    }

    private record GeminiRequest(List<Content> contents) {
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Candidate(ContentResponse content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentResponse(List<PartResponse> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PartResponse(String text) {
    }

    private record AiAnalysisResponse(
            String severity,
            String rootCause,
            String impact,
            String recommendation
    ) {
    }
}
