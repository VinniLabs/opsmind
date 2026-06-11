import { http } from '@/api/http'
import type {
  CreateIncidentRequest,
  Incident,
  UpdateIncidentAnalysisRequest,
  UpdateIncidentStatusRequest,
  UpdateIncidentValidationRequest,
} from '@/types/incident'

const INCIDENTS_ENDPOINT = '/incidents'

/**
 * Service responsible for managing the incident lifecycle through the
 * OpsMind backend (`/api/v1/incidents`).
 */
export const incidentService = {
  /** Lists every incident. `GET /incidents`. */
  async list(): Promise<Incident[]> {
    const { data } = await http.get<Incident[]>(INCIDENTS_ENDPOINT)
    return data
  },

  /** Fetches a single incident by id. `GET /incidents/{id}`. */
  async getById(id: string): Promise<Incident> {
    const { data } = await http.get<Incident>(`${INCIDENTS_ENDPOINT}/${id}`)
    return data
  },

  /** Creates a new incident. `POST /incidents`. */
  async create(payload: CreateIncidentRequest): Promise<Incident> {
    const { data } = await http.post<Incident>(INCIDENTS_ENDPOINT, payload)
    return data
  },

  /** Advances (or sets) the incident status. `PATCH /incidents/{id}/status`. */
  async updateStatus(id: string, payload: UpdateIncidentStatusRequest): Promise<Incident> {
    const { data } = await http.patch<Incident>(`${INCIDENTS_ENDPOINT}/${id}/status`, payload)
    return data
  },

  /** Updates the AI diagnosis / real root cause. `PATCH /incidents/{id}/analysis`. */
  async updateAnalysis(id: string, payload: UpdateIncidentAnalysisRequest): Promise<Incident> {
    const { data } = await http.patch<Incident>(`${INCIDENTS_ENDPOINT}/${id}/analysis`, payload)
    return data
  },

  /** Saves the human validation of the incident. `PATCH /incidents/{id}/validation`. */
  async updateValidation(
    id: string,
    payload: UpdateIncidentValidationRequest,
  ): Promise<Incident> {
    const { data } = await http.patch<Incident>(`${INCIDENTS_ENDPOINT}/${id}/validation`, payload)
    return data
  },
}

export type IncidentService = typeof incidentService
