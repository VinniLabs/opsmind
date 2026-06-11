<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import SelectButton from 'primevue/selectbutton'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import IncidentStatusBadge from '@/components/IncidentStatusBadge.vue'
import { useIncidentsStore } from '@/stores/incidents'
import {
  getNextIncidentStatus,
  INCIDENT_STATUS_LABELS,
  INCIDENT_STATUS_FLOW,
  DIAGNOSIS_CORRECTNESS_LABELS,
  type IncidentSeverity,
  type DiagnosisCorrectness,
} from '@/types/incident'

const route = useRoute()
const router = useRouter()
const incidentsStore = useIncidentsStore()
const { selected, loading, saving, error } = storeToRefs(incidentsStore)

const incidentId = computed<string>(() => String(route.params.id))

const editingAnalysis = ref(false)
const analysisDraft = ref({ aiDiagnosis: '', realRootCause: '' })

const nextStatus = computed(() =>
  selected.value ? getNextIncidentStatus(selected.value.status) : null,
)

const nextStatusLabel = computed(() =>
  nextStatus.value ? INCIDENT_STATUS_LABELS[nextStatus.value] : null,
)

const statusFlow = computed(() => {
  if (!selected.value) {
    return []
  }
  const currentIndex = INCIDENT_STATUS_FLOW.indexOf(selected.value.status)
  return INCIDENT_STATUS_FLOW.map((status, index) => ({
    status,
    label: INCIDENT_STATUS_LABELS[status],
    done: index < currentIndex,
    current: index === currentIndex,
  }))
})

interface SeverityMeta {
  label: string
  icon: string
  modifier: string
}

const SEVERITY_META: Record<IncidentSeverity, SeverityMeta> = {
  LOW: { label: 'Baixa', icon: 'pi pi-info-circle', modifier: 'is-low' },
  MEDIUM: { label: 'Média', icon: 'pi pi-exclamation-circle', modifier: 'is-medium' },
  HIGH: { label: 'Alta', icon: 'pi pi-exclamation-triangle', modifier: 'is-high' },
  CRITICAL: { label: 'Crítica', icon: 'pi pi-bolt', modifier: 'is-critical' },
}

const severityMeta = computed<SeverityMeta | null>(() => {
  const severity = selected.value?.aiSeverity
  return severity ? (SEVERITY_META[severity] ?? null) : null
})

/** True when the incident carries the newer structured AI diagnosis fields. */
const hasStructuredDiagnosis = computed<boolean>(() => {
  const incident = selected.value
  if (!incident) {
    return false
  }
  return Boolean(
    incident.aiSeverity ||
      incident.aiProvider ||
      incident.aiRootCause ||
      incident.aiImpact ||
      incident.aiProposedSolution,
  )
})

function formatDate(value: string): string {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

async function handleAdvance(): Promise<void> {
  if (!selected.value || !nextStatus.value) {
    return
  }
  await incidentsStore.advanceStatus(selected.value.id, nextStatus.value)
}

function startEditingAnalysis(): void {
  analysisDraft.value = {
    aiDiagnosis: selected.value?.aiDiagnosis ?? '',
    realRootCause: selected.value?.realRootCause ?? '',
  }
  editingAnalysis.value = true
}

function cancelEditingAnalysis(): void {
  editingAnalysis.value = false
}

async function handleSaveAnalysis(): Promise<void> {
  if (!selected.value) {
    return
  }
  const updated = await incidentsStore.saveAnalysis(selected.value.id, {
    aiDiagnosis: analysisDraft.value.aiDiagnosis.trim(),
    realRootCause: analysisDraft.value.realRootCause.trim(),
  })
  if (updated) {
    editingAnalysis.value = false
  }
}

const DIAGNOSIS_OPTIONS: Array<{ label: string; value: DiagnosisCorrectness }> = (
  Object.entries(DIAGNOSIS_CORRECTNESS_LABELS) as [DiagnosisCorrectness, string][]
).map(([value, label]) => ({ value, label }))

const validationDraft = ref({
  realRootCause: '',
  appliedSolution: '',
  diagnosisCorrect: null as DiagnosisCorrectness | null,
  validationNotes: '',
})
const validationSuccess = ref(false)

function syncValidationDraft(): void {
  validationDraft.value = {
    realRootCause: selected.value?.realRootCause ?? '',
    appliedSolution: selected.value?.appliedSolution ?? '',
    diagnosisCorrect: selected.value?.diagnosisCorrect ?? null,
    validationNotes: selected.value?.validationNotes ?? '',
  }
}

async function handleSaveValidation(): Promise<void> {
  if (!selected.value || !validationDraft.value.diagnosisCorrect) {
    return
  }
  validationSuccess.value = false
  const updated = await incidentsStore.saveValidation(selected.value.id, {
    realRootCause: validationDraft.value.realRootCause.trim(),
    appliedSolution: validationDraft.value.appliedSolution.trim(),
    diagnosisCorrect: validationDraft.value.diagnosisCorrect,
    validationNotes: validationDraft.value.validationNotes.trim(),
  })
  if (updated) {
    validationSuccess.value = true
  }
}

function goBack(): void {
  router.push({ name: 'incidents' })
}

watch(incidentId, (id) => {
  if (id) {
    void incidentsStore.fetchById(id)
  }
})

watch(selected, () => {
  validationSuccess.value = false
  syncValidationDraft()
})

onMounted(() => {
  void incidentsStore.fetchById(incidentId.value)
})
</script>

<template>
  <section class="detail-view">
    <Button
      label="Voltar para incidentes"
      icon="pi pi-arrow-left"
      text
      class="detail-view__back"
      @click="goBack"
    />

    <div v-if="loading" class="panel state-panel" role="status" aria-live="polite">
      <ProgressSpinner class="state-panel__spinner" stroke-width="3" animation-duration="1.1s" />
      <p class="state-panel__title">Carregando incidente…</p>
    </div>

    <Message v-else-if="error && !selected" severity="error" :closable="false">
      {{ error }}
    </Message>

    <template v-else-if="selected">
      <header class="panel detail-header">
        <div class="detail-header__top">
          <div class="detail-header__heading">
            <h1 class="detail-header__title">{{ selected.title }}</h1>
            <IncidentStatusBadge :status="selected.status" />
          </div>
          <Button
            v-if="nextStatus"
            :label="`Avançar para ${nextStatusLabel}`"
            icon="pi pi-arrow-right"
            icon-pos="right"
            :loading="saving"
            @click="handleAdvance"
          />
          <span v-else class="detail-header__closed">
            <i class="pi pi-check-circle" aria-hidden="true"></i>
            Incidente encerrado
          </span>
        </div>

        <p class="detail-header__desc">{{ selected.description }}</p>

        <div class="detail-header__meta">
          <span><i class="pi pi-hashtag" aria-hidden="true"></i> {{ selected.id }}</span>
          <span><i class="pi pi-calendar-plus" aria-hidden="true"></i> Criado: {{ formatDate(selected.createdAt) }}</span>
          <span><i class="pi pi-pencil" aria-hidden="true"></i> Atualizado: {{ formatDate(selected.updatedAt) }}</span>
        </div>

        <Message v-if="error" severity="error" :closable="false" class="detail-header__error">
          {{ error }}
        </Message>
      </header>

      <div class="panel detail-section">
        <h2 class="detail-section__title">
          <i class="pi pi-sitemap" aria-hidden="true"></i>
          Ciclo de vida
        </h2>
        <ol class="status-flow">
          <li
            v-for="step in statusFlow"
            :key="step.status"
            class="status-flow__step"
            :class="{ 'is-done': step.done, 'is-current': step.current }"
          >
            <span class="status-flow__marker" aria-hidden="true"></span>
            <span class="status-flow__label">{{ step.label }}</span>
          </li>
        </ol>
      </div>

      <div class="panel detail-section" v-if="selected.errorLog">
        <h2 class="detail-section__title">
          <i class="pi pi-align-left" aria-hidden="true"></i>
          Log de erro
        </h2>
        <pre class="detail-section__log">{{ selected.errorLog }}</pre>
      </div>

      <div class="panel detail-section">
        <div class="detail-section__header">
          <h2 class="detail-section__title">
            <i class="pi pi-sparkles" aria-hidden="true"></i>
            Análise
          </h2>
          <Button
            v-if="!editingAnalysis"
            label="Editar análise"
            icon="pi pi-pencil"
            severity="secondary"
            outlined
            size="small"
            @click="startEditingAnalysis"
          />
        </div>

        <template v-if="!editingAnalysis">
          <div class="ai-diagnosis">
            <div class="ai-diagnosis__head">
              <span class="analysis-block__label">
                <i class="pi pi-bolt" aria-hidden="true"></i> Diagnóstico da IA
              </span>
              <span
                v-if="severityMeta"
                class="severity-badge"
                :class="severityMeta.modifier"
              >
                <span class="severity-badge__dot" aria-hidden="true"></span>
                <i :class="severityMeta.icon" aria-hidden="true"></i>
                <span>Severidade {{ severityMeta.label }}</span>
              </span>
            </div>

            <template v-if="hasStructuredDiagnosis">
              <dl class="ai-diagnosis__grid">
                <div v-if="selected.aiProvider" class="ai-diagnosis__item">
                  <dt class="ai-diagnosis__term">
                    <i class="pi pi-server" aria-hidden="true"></i> Provider
                  </dt>
                  <dd class="ai-diagnosis__desc">{{ selected.aiProvider }}</dd>
                </div>
                <div v-if="selected.aiRootCause" class="ai-diagnosis__item">
                  <dt class="ai-diagnosis__term">
                    <i class="pi pi-search" aria-hidden="true"></i> Causa raiz provável
                  </dt>
                  <dd class="ai-diagnosis__desc">{{ selected.aiRootCause }}</dd>
                </div>
                <div v-if="selected.aiImpact" class="ai-diagnosis__item">
                  <dt class="ai-diagnosis__term">
                    <i class="pi pi-chart-line" aria-hidden="true"></i> Impacto
                  </dt>
                  <dd class="ai-diagnosis__desc">{{ selected.aiImpact }}</dd>
                </div>
                <div v-if="selected.aiProposedSolution" class="ai-diagnosis__item">
                  <dt class="ai-diagnosis__term">
                    <i class="pi pi-wrench" aria-hidden="true"></i> Solução proposta
                  </dt>
                  <dd class="ai-diagnosis__desc">{{ selected.aiProposedSolution }}</dd>
                </div>
              </dl>
            </template>

            <template v-else>
              <p v-if="selected.aiDiagnosis" class="analysis-block__text">{{ selected.aiDiagnosis }}</p>
              <p v-else class="analysis-block__empty">Ainda não há diagnóstico da IA.</p>
            </template>
          </div>

          <div class="analysis-block">
            <span class="analysis-block__label">
              <i class="pi pi-search" aria-hidden="true"></i> Causa raiz real
            </span>
            <p v-if="selected.realRootCause" class="analysis-block__text">{{ selected.realRootCause }}</p>
            <p v-else class="analysis-block__empty">Causa raiz real ainda não informada.</p>
          </div>
        </template>

        <template v-else>
          <label class="analysis-edit__field">
            <span class="analysis-block__label">Diagnóstico da IA</span>
            <Textarea v-model="analysisDraft.aiDiagnosis" :rows="4" :disabled="saving" />
          </label>
          <label class="analysis-edit__field">
            <span class="analysis-block__label">Causa raiz real</span>
            <Textarea v-model="analysisDraft.realRootCause" :rows="4" :disabled="saving" />
          </label>
          <div class="analysis-edit__actions">
            <Button
              label="Cancelar"
              severity="secondary"
              outlined
              :disabled="saving"
              @click="cancelEditingAnalysis"
            />
            <Button label="Salvar análise" icon="pi pi-check" :loading="saving" @click="handleSaveAnalysis" />
          </div>
        </template>
      </div>

      <div class="panel detail-section">
        <div class="detail-section__header">
          <h2 class="detail-section__title">
            <i class="pi pi-verified" aria-hidden="true"></i>
            Validação humana
          </h2>
          <span v-if="selected.validatedAt" class="validation__validated">
            <i class="pi pi-check-circle" aria-hidden="true"></i>
            Validado em {{ formatDate(selected.validatedAt) }}
          </span>
        </div>

        <Message
          v-if="validationSuccess"
          severity="success"
          :closable="false"
          class="validation__message"
        >
          Validação salva com sucesso.
        </Message>
        <Message
          v-if="error"
          severity="error"
          :closable="false"
          class="validation__message"
        >
          {{ error }}
        </Message>

        <label class="analysis-edit__field">
          <span class="analysis-block__label">
            <i class="pi pi-search" aria-hidden="true"></i> Causa raiz real
          </span>
          <Textarea
            v-model="validationDraft.realRootCause"
            :rows="3"
            :disabled="saving"
            placeholder="Descreva a causa raiz confirmada."
          />
        </label>

        <label class="analysis-edit__field">
          <span class="analysis-block__label">
            <i class="pi pi-wrench" aria-hidden="true"></i> Solução aplicada
          </span>
          <Textarea
            v-model="validationDraft.appliedSolution"
            :rows="3"
            :disabled="saving"
            placeholder="Descreva a solução que foi aplicada."
          />
        </label>

        <div class="analysis-edit__field">
          <span class="analysis-block__label">
            <i class="pi pi-check-square" aria-hidden="true"></i> Diagnóstico da IA estava
          </span>
          <SelectButton
            v-model="validationDraft.diagnosisCorrect"
            :options="DIAGNOSIS_OPTIONS"
            option-label="label"
            option-value="value"
            :allow-empty="false"
            :disabled="saving"
            aria-label="Avaliação do diagnóstico da IA"
          />
        </div>

        <label class="analysis-edit__field">
          <span class="analysis-block__label">
            <i class="pi pi-comment" aria-hidden="true"></i> Observações da validação
          </span>
          <Textarea
            v-model="validationDraft.validationNotes"
            :rows="3"
            :disabled="saving"
            placeholder="Notas adicionais sobre a validação (opcional)."
          />
        </label>

        <div class="analysis-edit__actions">
          <Button
            label="Salvar validação"
            icon="pi pi-check"
            :loading="saving"
            :disabled="!validationDraft.diagnosisCorrect"
            @click="handleSaveValidation"
          />
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.detail-view {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.detail-view__back {
  align-self: flex-start;
}

.panel {
  background: var(--om-surface);
  border: 1px solid var(--om-border);
  border-radius: var(--om-radius-lg);
  padding: 1.5rem;
  box-shadow: var(--om-shadow);
}

.state-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 0.6rem;
  padding: 2.75rem 1.5rem;
}

.state-panel__spinner {
  width: 52px;
  height: 52px;
}

.state-panel__spinner :deep(.p-progressspinner-circle) {
  stroke: var(--om-accent);
  animation: p-progressspinner-dash 1.4s ease-in-out infinite;
}

.state-panel__title {
  font-weight: 600;
  color: var(--om-text);
}

.detail-header {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-header__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.detail-header__heading {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  flex-wrap: wrap;
}

.detail-header__title {
  font-size: clamp(1.4rem, 3.5vw, 1.9rem);
}

.detail-header__closed {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  color: var(--om-sev-low);
  font-weight: 600;
  font-size: 0.9rem;
}

.detail-header__desc {
  color: var(--om-text-muted);
  line-height: 1.6;
}

.detail-header__meta {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
  color: var(--om-text-dim);
  font-size: 0.8rem;
}

.detail-header__meta i {
  color: var(--om-accent);
  margin-right: 0.3rem;
}

.detail-header__error {
  margin: 0;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.detail-section__title {
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  font-size: 1.05rem;
  font-weight: 600;
}

.detail-section__title i {
  color: var(--om-accent);
}

.detail-section__log {
  margin: 0;
  padding: 1rem;
  background: var(--om-bg);
  border: 1px solid var(--om-border);
  border-radius: var(--om-radius-sm);
  font-family: var(--om-font-mono);
  font-size: 0.8rem;
  color: var(--om-text-muted);
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 320px;
  overflow: auto;
}

.status-flow {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1.25rem;
}

.status-flow__step {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--om-text-dim);
  font-size: 0.85rem;
}

.status-flow__marker {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  border: 2px solid var(--om-border-strong);
  background: transparent;
}

.status-flow__step.is-done {
  color: var(--om-text-muted);
}

.status-flow__step.is-done .status-flow__marker {
  background: var(--om-sev-low);
  border-color: var(--om-sev-low);
}

.status-flow__step.is-current {
  color: var(--om-accent);
  font-weight: 600;
}

.status-flow__step.is-current .status-flow__marker {
  background: var(--om-accent);
  border-color: var(--om-accent);
  box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.18);
}

.analysis-block {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.analysis-block__label {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-weight: 600;
  font-size: 0.88rem;
  color: var(--om-text);
}

.analysis-block__label i {
  color: var(--om-accent);
}

.analysis-block__text {
  color: var(--om-text-muted);
  line-height: 1.65;
  white-space: pre-line;
}

.analysis-block__empty {
  color: var(--om-text-dim);
  font-style: italic;
}

.ai-diagnosis {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--om-bg);
  border: 1px solid var(--om-border);
  border-radius: var(--om-radius-md, 12px);
}

.ai-diagnosis__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.ai-diagnosis__grid {
  margin: 0;
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 260px), 1fr));
}

.ai-diagnosis__item {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.ai-diagnosis__term {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--om-text);
}

.ai-diagnosis__term i {
  color: var(--om-accent);
}

.ai-diagnosis__desc {
  margin: 0;
  color: var(--om-text-muted);
  line-height: 1.65;
  white-space: pre-line;
}

.severity-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.8rem;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.78rem;
  white-space: nowrap;
  border: 1px solid transparent;
}

.severity-badge__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.severity-badge.is-low {
  color: var(--om-sev-low);
  background: rgba(52, 211, 153, 0.12);
  border-color: rgba(52, 211, 153, 0.3);
}

.severity-badge.is-medium {
  color: var(--om-sev-medium);
  background: rgba(251, 191, 36, 0.12);
  border-color: rgba(251, 191, 36, 0.3);
}

.severity-badge.is-high {
  color: var(--om-sev-high);
  background: rgba(251, 146, 60, 0.12);
  border-color: rgba(251, 146, 60, 0.3);
}

.severity-badge.is-critical {
  color: var(--om-sev-critical);
  background: rgba(248, 113, 113, 0.14);
  border-color: rgba(248, 113, 113, 0.35);
}

.validation__validated {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  color: var(--om-sev-low);
  font-weight: 600;
  font-size: 0.82rem;
}

.validation__message {
  margin: 0;
}

.analysis-edit__field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.analysis-edit__field :deep(.p-textarea) {
  width: 100%;
}

.analysis-edit__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
}

@media (max-width: 560px) {
  .panel {
    padding: 1.2rem;
  }
}
</style>
