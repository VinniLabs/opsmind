import { createRouter, createWebHistory } from 'vue-router'
import IncidentAnalysisView from '@/views/IncidentAnalysisView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'incident-analysis',
      component: IncidentAnalysisView,
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

export default router
