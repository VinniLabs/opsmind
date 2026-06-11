<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import IncidentStatusBadge from '@/components/IncidentStatusBadge.vue'
import { useIncidentsStore } from '@/stores/incidents'
import type { CreateIncidentRequest } from '@/types/incident'

const router = useRouter()
const incidentsStore = useIncidentsStore()
const { incidents, loading, saving, error } = storeToRefs(incidentsStore)

const dialogVisible = ref(false)
const form = ref<CreateIncidentRequest>({ title: '', description: '', errorLog: '' })
const validationError = ref<string | null>(null)

const sortedIncidents = computed(() =>
  [...incidents.value].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
  ),
)

const isEmpty = computed<boolean>(() => !loading.value && incidents.value.length === 0)

function formatDate(value: string): string {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

function openDialog(): void {
  form.value = { title: '', description: '', errorLog: '' }
  validationError.value = null
  incidentsStore.clearError()
  dialogVisible.value = true
}

function openDetail(id: string): void {
  router.push({ name: 'incident-detail', params: { id } })
}

async function handleCreate(): Promise<void> {
  validationError.value = null

  if (!form.value.title.trim() || !form.value.description.trim()) {
    validationError.value = 'Informe pelo menos o título e a descrição do incidente.'
    return
  }

  const created = await incidentsStore.create({
    title: form.value.title.trim(),
    description: form.value.description.trim(),
    errorLog: form.value.errorLog.trim(),
  })

  if (created) {
    dialogVisible.value = false
    openDetail(created.id)
  }
}

onMounted(() => {
  void incidentsStore.fetchAll()
})
</script>

<template>
  <section class="incidents-view">
    <header class="incidents-view__header">
      <div>
        <h1 class="incidents-view__title">Incidentes</h1>
        <p class="incidents-view__subtitle">
          Acompanhe o ciclo de vida dos incidentes, do registro ao encerramento.
        </p>
      </div>
      <div class="incidents-view__actions">
        <Button
          label="Atualizar"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="incidentsStore.fetchAll()"
        />
        <Button label="Novo incidente" icon="pi pi-plus" @click="openDialog" />
      </div>
    </header>

    <Message v-if="error" severity="error" :closable="false" class="incidents-view__error">
      {{ error }}
    </Message>

    <div v-if="loading" class="panel state-panel" role="status" aria-live="polite">
      <ProgressSpinner class="state-panel__spinner" stroke-width="3" animation-duration="1.1s" />
      <p class="state-panel__title">Carregando incidentes…</p>
    </div>

    <div v-else-if="isEmpty" class="panel empty-state">
      <i class="pi pi-inbox empty-state__icon" aria-hidden="true"></i>
      <p class="empty-state__title">Nenhum incidente registrado</p>
      <p class="empty-state__text">Crie o primeiro incidente para iniciar o acompanhamento.</p>
      <Button label="Novo incidente" icon="pi pi-plus" @click="openDialog" />
    </div>

    <ul v-else class="incident-list">
      <li
        v-for="incident in sortedIncidents"
        :key="incident.id"
        class="incident-card"
        @click="openDetail(incident.id)"
      >
        <div class="incident-card__main">
          <div class="incident-card__top">
            <h2 class="incident-card__title">{{ incident.title }}</h2>
            <IncidentStatusBadge :status="incident.status" />
          </div>
          <p class="incident-card__desc">{{ incident.description }}</p>
        </div>
        <div class="incident-card__meta">
          <span class="incident-card__date">
            <i class="pi pi-clock" aria-hidden="true"></i>
            {{ formatDate(incident.createdAt) }}
          </span>
          <i class="pi pi-angle-right incident-card__chevron" aria-hidden="true"></i>
        </div>
      </li>
    </ul>

    <Dialog
      v-model:visible="dialogVisible"
      modal
      header="Novo incidente"
      :style="{ width: 'min(560px, 92vw)' }"
      :draggable="false"
    >
      <div class="create-form">
        <label class="create-form__field">
          <span class="create-form__label">Título</span>
          <InputText
            v-model="form.title"
            :disabled="saving"
            placeholder="Ex.: Falha no checkout da região EU"
          />
        </label>

        <label class="create-form__field">
          <span class="create-form__label">Descrição</span>
          <Textarea
            v-model="form.description"
            :rows="3"
            :disabled="saving"
            placeholder="Resumo do que está acontecendo e impacto observado"
          />
        </label>

        <label class="create-form__field">
          <span class="create-form__label">Log de erro (opcional)</span>
          <Textarea
            v-model="form.errorLog"
            :rows="6"
            :disabled="saving"
            class="create-form__mono"
            placeholder="Cole aqui logs, stack traces ou alertas relevantes"
          />
        </label>

        <Message v-if="validationError" severity="warn" :closable="false">
          {{ validationError }}
        </Message>
        <Message v-if="error" severity="error" :closable="false">{{ error }}</Message>
      </div>

      <template #footer>
        <Button
          label="Cancelar"
          severity="secondary"
          outlined
          :disabled="saving"
          @click="dialogVisible = false"
        />
        <Button label="Criar incidente" icon="pi pi-check" :loading="saving" @click="handleCreate" />
      </template>
    </Dialog>
  </section>
</template>

<style scoped>
.incidents-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.incidents-view__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.incidents-view__title {
  font-size: clamp(1.6rem, 4vw, 2.1rem);
}

.incidents-view__subtitle {
  margin-top: 0.35rem;
  color: var(--om-text-muted);
  font-size: 0.95rem;
}

.incidents-view__actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.incidents-view__error {
  margin: 0;
}

.panel {
  background: var(--om-surface);
  border: 1px solid var(--om-border);
  border-radius: var(--om-radius-lg);
  padding: 1.5rem;
  box-shadow: var(--om-shadow);
}

.state-panel,
.empty-state {
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

.state-panel__title,
.empty-state__title {
  font-weight: 600;
  font-size: 1.05rem;
  color: var(--om-text);
}

.empty-state__icon {
  font-size: 2.2rem;
  color: var(--om-text-dim);
}

.empty-state__text {
  color: var(--om-text-muted);
}

.incident-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.incident-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.1rem 1.25rem;
  background: var(--om-surface);
  border: 1px solid var(--om-border);
  border-radius: var(--om-radius);
  cursor: pointer;
  transition: border-color 0.15s ease, transform 0.15s ease, background 0.15s ease;
}

.incident-card:hover {
  border-color: var(--om-border-strong);
  background: var(--om-surface-2);
  transform: translateY(-1px);
}

.incident-card__main {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  min-width: 0;
}

.incident-card__top {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.incident-card__title {
  font-size: 1.02rem;
  font-weight: 600;
}

.incident-card__desc {
  color: var(--om-text-muted);
  font-size: 0.9rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.incident-card__meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.incident-card__date {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-family: var(--om-font-mono);
  font-size: 0.74rem;
  color: var(--om-text-dim);
  white-space: nowrap;
}

.incident-card__chevron {
  color: var(--om-text-dim);
  font-size: 1.1rem;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.create-form__field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.create-form__label {
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--om-text);
}

.create-form__field :deep(.p-inputtext),
.create-form__field :deep(.p-textarea) {
  width: 100%;
}

.create-form__mono :deep(textarea),
.create-form__mono {
  font-family: var(--om-font-mono);
  font-size: 0.82rem;
}

@media (max-width: 560px) {
  .panel {
    padding: 1.2rem;
  }

  .incident-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .incident-card__meta {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
