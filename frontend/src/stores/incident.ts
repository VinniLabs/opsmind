import { defineStore } from 'pinia'
import { ref } from 'vue'
import { resolveErrorMessage } from '@/api/http'
import { incidentAnalysisService } from '@/services/incidentAnalysisService'
import type { IncidentAnalysis } from '@/types/incident'

/**
 * Holds the state of the incident analysis screen: the latest result,
 * the loading flag used by the UI and any error message to display.
 */
export const useIncidentStore = defineStore('incident', () => {
  const result = ref<IncidentAnalysis | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function analyze(content: string): Promise<void> {
    loading.value = true
    error.value = null

    try {
      result.value = await incidentAnalysisService.analyze(content)
    } catch (err) {
      result.value = null
      error.value = resolveErrorMessage(err)
    } finally {
      loading.value = false
    }
  }

  function reset(): void {
    result.value = null
    error.value = null
  }

  return { result, loading, error, analyze, reset }
})
