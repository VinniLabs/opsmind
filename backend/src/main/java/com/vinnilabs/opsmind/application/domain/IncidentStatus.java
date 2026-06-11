package com.vinnilabs.opsmind.application.domain;

import java.util.List;

public enum IncidentStatus {
    NOVO,
    EM_ANALISE,
    DIAGNOSTICADO,
    CORRECAO_PROPOSTA,
    VALIDACAO_HUMANA,
    RESOLVIDO,
    ENCERRADO;

    private static final List<IncidentStatus> FLOW = List.of(
            NOVO,
            EM_ANALISE,
            DIAGNOSTICADO,
            CORRECAO_PROPOSTA,
            VALIDACAO_HUMANA,
            RESOLVIDO,
            ENCERRADO
    );

    public boolean canTransitionTo(IncidentStatus target) {
        if (target == null) {
            return false;
        }
        int currentIndex = FLOW.indexOf(this);
        int targetIndex = FLOW.indexOf(target);
        return targetIndex == currentIndex + 1;
    }
}

