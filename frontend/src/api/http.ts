import axios, { type AxiosInstance } from 'axios'

const DEFAULT_BASE_URL = 'http://localhost:8080/api/v1'

/**
 * Shared Axios instance pointing at the OpsMind backend.
 * The base URL can be overridden through the `VITE_API_BASE_URL` env var.
 */
export const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? DEFAULT_BASE_URL,
  timeout: 60_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

interface ApiErrorBody {
  message?: string
  error?: string
}

/**
 * Translates an unknown error (typically thrown by Axios) into a
 * human-readable message suitable for the UI.
 */
export function resolveErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    const data = error.response?.data

    if (data?.message) {
      return data.message
    }

    if (data?.error) {
      return data.error
    }

    if (error.response) {
      return `Request failed with status ${error.response.status}.`
    }

    if (error.code === 'ECONNABORTED') {
      return 'The analysis request timed out. Please try again.'
    }

    return 'Could not reach the OpsMind API. Check if the backend is running on the configured host.'
  }

  return 'An unexpected error occurred while analyzing the incident.'
}
