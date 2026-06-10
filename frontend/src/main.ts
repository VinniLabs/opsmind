import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'

import App from './App.vue'
import router from './router'
import { OpsMindPreset } from './theme/preset'

import 'primeicons/primeicons.css'
import './styles/theme.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(PrimeVue, {
  theme: {
    preset: OpsMindPreset,
    options: {
      darkModeSelector: '.app-dark',
      cssLayer: false,
    },
  },
})

app.mount('#app')
