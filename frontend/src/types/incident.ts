/**
 * Domain types shared across the incident analysis feature.
 */

/** Severity levels returned by the backend AI analyzer. */
export type IncidentSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

/**
 * Human assessment of how accurate the AI diagnosis was during the
 * human validation step.
 */
export type DiagnosisCorrectness = 'CORRECT' | 'PARTIAL' | 'INCORRECT'

/** Human-readable labels for each diagnosis correctness option. */
export const DIAGNOSIS_CORRECTNESS_LABELS: Record<DiagnosisCorrectness, string> = {
  CORRECT: 'Correto',
  PARTIAL: 'Parcial',
  INCORRECT: 'Incorreto',
}

/** Request payload sent to `POST /api/v1/incidents/analyze`. */
export interface AnalyzeIncidentRequest {
  content: string
}

/** Structured analysis returned by the backend. */
export interface IncidentAnalysis {
  severity: IncidentSeverity
  rootCause: string
  impact: string
  recommendation: string
}

/** Response body of the analyze endpoint. */
export type AnalyzeIncidentResponse = IncidentAnalysis

/* ------------------------------------------------------------------ *
 * Incident lifecycle
 * ------------------------------------------------------------------ */

/**
 * Lifecycle status of an incident, following the OpsMind workflow:
 * NOVO → EM_ANALISE → DIAGNOSTICADO → CORRECAO_PROPOSTA →
 * VALIDACAO_HUMANA → RESOLVIDO → ENCERRADO.
 */
export type IncidentStatus =
  | 'NOVO'
  | 'EM_ANALISE'
  | 'DIAGNOSTICADO'
  | 'CORRECAO_PROPOSTA'
  | 'VALIDACAO_HUMANA'
  | 'RESOLVIDO'
  | 'ENCERRADO'

/** Ordered list of statuses describing the incident lifecycle. */
export const INCIDENT_STATUS_FLOW: readonly IncidentStatus[] = [
  'NOVO',
  'EM_ANALISE',
  'DIAGNOSTICADO',
  'CORRECAO_PROPOSTA',
  'VALIDACAO_HUMANA',
  'RESOLVIDO',
  'ENCERRADO',
] as const

/** Human-readable labels for each status. */
export const INCIDENT_STATUS_LABELS: Record<IncidentStatus, string> = {
  NOVO: 'Novo',
  EM_ANALISE: 'Em análise',
  DIAGNOSTICADO: 'Diagnosticado',
  CORRECAO_PROPOSTA: 'Correção proposta',
  VALIDACAO_HUMANA: 'Validação humana',
  RESOLVIDO: 'Resolvido',
  ENCERRADO: 'Encerrado',
}

/**
 * Returns the next status in the lifecycle, or `null` when the incident
 * is already in the final (`ENCERRADO`) state.
 */
export function getNextIncidentStatus(status: IncidentStatus): IncidentStatus | null {
  const index = INCIDENT_STATUS_FLOW.indexOf(status)
  if (index < 0 || index >= INCIDENT_STATUS_FLOW.length - 1) {
    return null
  }
  return INCIDENT_STATUS_FLOW[index + 1]
}

/** An incident as returned by the backend. */
export interface Incident {
  id: string
  title: string
  description: string
  errorLog: string
  status: IncidentStatus
  aiDiagnosis: string | null
  realRootCause: string | null
  /** Structured AI diagnosis fields (newer incidents). */
  aiSeverity: IncidentSeverity | null
  aiRootCause: string | null
  aiImpact: string | null
  aiProposedSolution: string | null
  aiProvider: string | null
  /** Human validation fields. */
  appliedSolution: string | null
  diagnosisCorrect: DiagnosisCorrectness | null
  validationNotes: string | null
  validatedAt: string | null
  createdAt: string
  updatedAt: string
}

/** Request payload sent to `POST /api/v1/incidents`. */
export interface CreateIncidentRequest {
  title: string
  description: string
  errorLog: string
}

/** Request payload sent to `PATCH /api/v1/incidents/{id}/status`. */
export interface UpdateIncidentStatusRequest {
  status: IncidentStatus
}

/** Request payload sent to `PATCH /api/v1/incidents/{id}/analysis`. */
export interface UpdateIncidentAnalysisRequest {
  aiDiagnosis: string
  realRootCause: string
}

/** Request payload sent to `PATCH /api/v1/incidents/{id}/validation`. */
export interface UpdateIncidentValidationRequest {
  realRootCause: string
  appliedSolution: string
  diagnosisCorrect: DiagnosisCorrectness
  validationNotes: string
}