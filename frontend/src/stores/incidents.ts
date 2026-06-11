import { defineStore } from 'pinia'
import { ref } from 'vue'
import { resolveErrorMessage } from '@/api/http'
import { incidentService } from '@/services/incidentService'
import type {
  CreateIncidentRequest,
  Incident,
  IncidentStatus,
  UpdateIncidentAnalysisRequest,
  UpdateIncidentValidationRequest,
} from '@/types/incident'

/**
 * Holds the state of the incident lifecycle screens: the list of
 * incidents, the currently selected incident, loading flags and any
 * error message to display.
 */
export const useIncidentsStore = defineStore('incidents', () => {
  const incidents = ref<Incident[]>([])
  const selected = ref<Incident | null>(null)

  const loading = ref(false)
  const saving = ref(false)
  const error = ref<string | null>(null)

  function upsert(incident: Incident): void {
    const index = incidents.value.findIndex((item) => item.id === incident.id)
    if (index >= 0) {
      incidents.value[index] = incident
    } else {
      incidents.value.unshift(incident)
    }
  }

  async function fetchAll(): Promise<void> {
    loading.value = true
    error.value = null

    try {
      incidents.value = await incidentService.list()
    } catch (err) {
      error.value = resolveErrorMessage(err)
    } finally {
      loading.value = false
    }
  }

  async function fetchById(id: string): Promise<void> {
    loading.value = true
    error.value = null
    selected.value = null

    try {
      selected.value = await incidentService.getById(id)
    } catch (err) {
      error.value = resolveErrorMessage(err)
    } finally {
      loading.value = false
    }
  }

  async function create(payload: CreateIncidentRequest): Promise<Incident | null> {
    saving.value = true
    error.value = null

    try {
      const incident = await incidentService.create(payload)
      upsert(incident)
      return incident
    } catch (err) {
      error.value = resolveErrorMessage(err)
      return null
    } finally {
      saving.value = false
    }
  }

  async function advanceStatus(id: string, status: IncidentStatus): Promise<Incident | null> {
    saving.value = true
    error.value = null

    try {
      const incident = await incidentService.updateStatus(id, { status })
      upsert(incident)
      if (selected.value?.id === id) {
        selected.value = incident
      }
      return incident
    } catch (err) {
      error.value = resolveErrorMessage(err)
      return null
    } finally {
      saving.value = false
    }
  }

  async function saveAnalysis(
    id: string,
    payload: UpdateIncidentAnalysisRequest,
  ): Promise<Incident | null> {
    saving.value = true
    error.value = null

    try {
      const incident = await incidentService.updateAnalysis(id, payload)
      upsert(incident)
      if (selected.value?.id === id) {
        selected.value = incident
      }
      return incident
    } catch (err) {
      error.value = resolveErrorMessage(err)
      return null
    } finally {
      saving.value = false
    }
  }

  async function saveValidation(
    id: string,
    payload: UpdateIncidentValidationRequest,
  ): Promise<Incident | null> {
    saving.value = true
    error.value = null

    try {
      const incident = await incidentService.updateValidation(id, payload)
      upsert(incident)
      if (selected.value?.id === id) {
        selected.value = incident
      }
      return incident
    } catch (err) {
      error.value = resolveErrorMessage(err)
      return null
    } finally {
      saving.value = false
    }
  }

  function clearError(): void {
    error.value = null
  }

  return {
    incidents,
    selected,
    loading,
    saving,
    error,
    fetchAll,
    fetchById,
    create,
    advanceStatus,
    saveAnalysis,
    saveValidation,
    clearError,
  }
})
