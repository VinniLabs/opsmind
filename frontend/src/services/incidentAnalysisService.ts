import { http } from '@/api/http'
import type { AnalyzeIncidentRequest, IncidentAnalysis } from '@/types/incident'

const ANALYZE_ENDPOINT = '/incidents/analyze'

/**
 * Service responsible for talking to the OpsMind incident analysis API.
 */
export const incidentAnalysisService = {
  /**
   * Sends incident content (logs, stack traces or a description) to the
   * backend and returns the structured AI analysis.
   *
   * @param content Raw incident text to be analyzed.
   */
  async analyze(content: string): Promise<IncidentAnalysis> {
    const payload: AnalyzeIncidentRequest = { content }
    const { data } = await http.post<IncidentAnalysis>(ANALYZE_ENDPOINT, payload)
    return data
  },
}

export type IncidentAnalysisService = typeof incidentAnalysisService
