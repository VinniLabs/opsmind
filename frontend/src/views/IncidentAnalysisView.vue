<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import AnalysisResultCard from '@/components/AnalysisResultCard.vue'
import { useIncidentStore } from '@/stores/incident'

const incidentStore = useIncidentStore()
const { result, loading, error } = storeToRefs(incidentStore)

const content = ref('')
const validationError = ref<string | null>(null)

const characterCount = computed<number>(() => content.value.length)
const isEmpty = computed<boolean>(() => content.value.trim().length === 0)
const canClear = computed<boolean>(
  () => !isEmpty.value || result.value !== null || error.value !== null,
)

const SAMPLE_INCIDENT = `[2026-06-07T14:32:11Z] ERROR payment-service - Failed to process txn_91f2: connection timeout to db-prod-eu-west-1 after 30000ms
[2026-06-07T14:32:11Z] WARN  payment-service - HikariPool-1 - Connection is not available, request timed out after 30001ms
[2026-06-07T14:32:12Z] ERROR api-gateway - Upstream 504 from payment-service POST /v1/charge (p99 latency 31.2s)
[2026-06-07T14:32:13Z] INFO  payment-service - Circuit breaker OPEN for db-prod-eu-west-1

Customers report checkout failures across the EU region.
Error rate spiked from 0.2% to 47% in under 5 minutes.`

async function handleAnalyze(): Promise<void> {
  validationError.value = null

  if (isEmpty.value) {
    validationError.value = 'Paste incident logs or a description before running the analysis.'
    return
  }

  await incidentStore.analyze(content.value.trim())
}

function handleClear(): void {
  content.value = ''
  validationError.value = null
  incidentStore.reset()
}

function loadSample(): void {
  content.value = SAMPLE_INCIDENT
  validationError.value = null
}
</script>

<template>
  <section class="incident-view">
    <header class="hero">
      <div class="hero__brand">
        <span class="hero__logo" aria-hidden="true">
          <svg viewBox="0 0 64 64" width="40" height="40">
            <defs>
              <linearGradient id="hero-grad" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
                <stop stop-color="#38bdf8" />
                <stop offset="1" stop-color="#818cf8" />
              </linearGradient>
            </defs>
            <path
              d="M10 34 H22 L27 21 L34 45 L39 31 L43 38 H54"
              fill="none"
              stroke="url(#hero-grad)"
              stroke-width="4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </span>
        <h1 class="hero__title">OpsMind</h1>
      </div>
      <p class="hero__subtitle">AI-Powered Incident Analysis</p>
      <p class="hero__hint">
        Paste raw logs, stack traces, alerts or a short description. OpsMind correlates the
        signals and returns severity, root cause, impact and a recommended action.
      </p>
    </header>

    <div class="panel input-panel">
      <div class="panel__header">
        <span class="panel__heading">
          <i class="pi pi-align-left" aria-hidden="true"></i>
          Incident logs &amp; description
        </span>
        <Button
          label="Load sample"
          icon="pi pi-bolt"
          text
          size="small"
          :disabled="loading"
          @click="loadSample"
        />
      </div>

      <Textarea
        v-model="content"
        class="input-panel__textarea"
        :rows="14"
        :disabled="loading"
        placeholder="e.g. [2026-06-07T14:32:11Z] ERROR payment-service - connection timeout to db-prod ..."
        aria-label="Incident logs and description"
      />

      <div class="input-panel__footer">
        <span class="char-count">{{ characterCount }} characters</span>
        <div class="input-panel__actions">
          <Button
            label="Clear"
            icon="pi pi-times"
            severity="secondary"
            outlined
            :disabled="loading || !canClear"
            @click="handleClear"
          />
          <Button
            label="Analyze Incident"
            icon="pi pi-sparkles"
            :loading="loading"
            @click="handleAnalyze"
          />
        </div>
      </div>

      <Message
        v-if="validationError"
        severity="warn"
        :closable="false"
        class="input-panel__message"
      >
        {{ validationError }}
      </Message>
    </div>

    <div v-if="loading" class="panel state-panel" role="status" aria-live="polite">
      <ProgressSpinner
        class="state-panel__spinner"
        stroke-width="3"
        animation-duration="1.1s"
      />
      <p class="state-panel__title">Analyzing incident…</p>
      <p class="state-panel__text">
        OpsMind is correlating the provided signals and reasoning about the root cause.
      </p>
    </div>

    <Message v-else-if="error" severity="error" :closable="false" class="result-error">
      <div class="result-error__body">
        <strong>Analysis failed</strong>
        <span>{{ error }}</span>
      </div>
    </Message>

    <AnalysisResultCard v-else-if="result" :analysis="result" />
  </section>
</template>

<style scoped>
.incident-view {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.hero {
  text-align: center;
  padding: 0.5rem 0 0.25rem;
}

.hero__brand {
  display: inline-flex;
  align-items: center;
  gap: 0.85rem;
}

.hero__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(140deg, rgba(56, 189, 248, 0.18), rgba(129, 140, 248, 0.18));
  border: 1px solid var(--om-border-strong);
}

.hero__title {
  font-size: clamp(2.4rem, 6vw, 3.4rem);
  font-weight: 700;
  letter-spacing: -0.03em;
  background: linear-gradient(120deg, #e7eef9 0%, var(--om-accent) 55%, var(--om-accent-2) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero__subtitle {
  margin-top: 0.5rem;
  font-size: clamp(1rem, 2.5vw, 1.2rem);
  font-weight: 500;
  color: var(--om-accent);
}

.hero__hint {
  max-width: 62ch;
  margin: 1rem auto 0;
  color: var(--om-text-muted);
  font-size: 0.95rem;
  line-height: 1.6;
}

.panel {
  background: var(--om-surface);
  border: 1px solid var(--om-border);
  border-radius: var(--om-radius-lg);
  padding: 1.5rem;
  box-shadow: var(--om-shadow);
}

.input-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.panel__heading {
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  font-weight: 600;
  color: var(--om-text);
}

.panel__heading i {
  color: var(--om-accent);
}

.input-panel__textarea {
  width: 100%;
  min-height: 260px;
}

.input-panel__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.char-count {
  font-family: var(--om-font-mono);
  font-size: 0.78rem;
  color: var(--om-text-dim);
}

.input-panel__actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.input-panel__message {
  margin: 0;
}

.state-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 0.5rem;
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
  font-size: 1.05rem;
  color: var(--om-text);
}

.state-panel__text {
  max-width: 46ch;
  color: var(--om-text-muted);
}

.result-error :deep(.p-message-text) {
  width: 100%;
}

.result-error__body {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

@media (max-width: 560px) {
  .panel {
    padding: 1.2rem;
  }

  .input-panel__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .input-panel__actions {
    width: 100%;
  }

  .input-panel__actions :deep(.p-button) {
    flex: 1;
    justify-content: center;
  }
}
</style>
