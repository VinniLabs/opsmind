/**
 * Domain types shared across the incident analysis feature.
 */

/** Severity levels returned by the backend AI analyzer. */
export type IncidentSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

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
