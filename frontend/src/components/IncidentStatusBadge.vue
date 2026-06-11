<script setup lang="ts">
import { computed } from 'vue'
import { INCIDENT_STATUS_LABELS, type IncidentStatus } from '@/types/incident'

const props = defineProps<{
  status: IncidentStatus
}>()

const STATUS_MODIFIER: Record<IncidentStatus, string> = {
  NOVO: 'is-novo',
  EM_ANALISE: 'is-analise',
  DIAGNOSTICADO: 'is-diagnosticado',
  CORRECAO_PROPOSTA: 'is-correcao',
  VALIDACAO_HUMANA: 'is-validacao',
  RESOLVIDO: 'is-resolvido',
  ENCERRADO: 'is-encerrado',
}

const label = computed<string>(() => INCIDENT_STATUS_LABELS[props.status] ?? props.status)
const modifier = computed<string>(() => STATUS_MODIFIER[props.status] ?? 'is-novo')
</script>

<template>
  <span class="status-badge" :class="modifier">
    <span class="status-badge__dot" aria-hidden="true"></span>
    <span>{{ label }}</span>
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.32rem 0.75rem;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.76rem;
  white-space: nowrap;
  border: 1px solid transparent;
}

.status-badge__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.status-badge.is-novo {
  color: var(--om-accent);
  background: rgba(56, 189, 248, 0.12);
  border-color: rgba(56, 189, 248, 0.3);
}

.status-badge.is-analise {
  color: var(--om-accent-2);
  background: rgba(129, 140, 248, 0.12);
  border-color: rgba(129, 140, 248, 0.3);
}

.status-badge.is-diagnosticado {
  color: var(--om-sev-medium);
  background: rgba(251, 191, 36, 0.12);
  border-color: rgba(251, 191, 36, 0.3);
}

.status-badge.is-correcao {
  color: var(--om-sev-high);
  background: rgba(251, 146, 60, 0.12);
  border-color: rgba(251, 146, 60, 0.3);
}

.status-badge.is-validacao {
  color: #c084fc;
  background: rgba(192, 132, 252, 0.12);
  border-color: rgba(192, 132, 252, 0.3);
}

.status-badge.is-resolvido {
  color: var(--om-sev-low);
  background: rgba(52, 211, 153, 0.12);
  border-color: rgba(52, 211, 153, 0.3);
}

.status-badge.is-encerrado {
  color: var(--om-text-dim);
  background: rgba(98, 117, 143, 0.14);
  border-color: rgba(98, 117, 143, 0.3);
}
</style>
