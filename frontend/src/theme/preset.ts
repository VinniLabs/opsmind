import { definePreset } from '@primeuix/themes'
import Aura from '@primeuix/themes/aura'

/**
 * OpsMind theme preset.
 *
 * Extends PrimeVue's Aura preset and remaps the primary palette to a
 * sky/cyan tone that matches the observability-style dark UI used across
 * Grafana, Datadog and New Relic.
 */
export const OpsMindPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{sky.50}',
      100: '{sky.100}',
      200: '{sky.200}',
      300: '{sky.300}',
      400: '{sky.400}',
      500: '{sky.500}',
      600: '{sky.600}',
      700: '{sky.700}',
      800: '{sky.800}',
      900: '{sky.900}',
      950: '{sky.950}',
    },
  },
})
