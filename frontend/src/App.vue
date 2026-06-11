<script setup lang="ts">
const apiTarget = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1').replace(
  /^https?:\/\//,
  '',
)
</script>

<template>
  <div class="app-shell">
    <header class="app-nav">
      <div class="app-nav__brand">
        <span class="app-nav__mark" aria-hidden="true">
          <svg viewBox="0 0 64 64" width="22" height="22">
            <defs>
              <linearGradient id="nav-grad" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
                <stop stop-color="#38bdf8" />
                <stop offset="1" stop-color="#818cf8" />
              </linearGradient>
            </defs>
            <path
              d="M10 34 H22 L27 21 L34 45 L39 31 L43 38 H54"
              fill="none"
              stroke="url(#nav-grad)"
              stroke-width="5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </span>
        <span class="app-nav__name">OpsMind</span>
        <span class="app-nav__tag">beta</span>
      </div>

      <nav class="app-nav__links">
        <RouterLink class="app-nav__link" to="/">
          <i class="pi pi-sparkles" aria-hidden="true"></i>
          Análise
        </RouterLink>
        <RouterLink class="app-nav__link" to="/incidents">
          <i class="pi pi-list" aria-hidden="true"></i>
          Incidentes
        </RouterLink>
      </nav>

      <div class="app-nav__meta">
        <span class="app-nav__endpoint" :title="`API endpoint: ${apiTarget}`">
          <i class="pi pi-server" aria-hidden="true"></i>
          {{ apiTarget }}
        </span>
        <span class="app-nav__status">
          <span class="app-nav__status-dot" aria-hidden="true"></span>
          System online
        </span>
      </div>
    </header>

    <main class="app-content">
      <RouterView />
    </main>

    <footer class="app-footer">
      <span>OpsMind · AI-Powered Incident Analysis</span>
      <span>© 2026 VinniLabs</span>
    </footer>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-nav {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.85rem clamp(1rem, 4vw, 2.5rem);
  background: rgba(10, 14, 23, 0.72);
  border-bottom: 1px solid var(--om-border);
  backdrop-filter: blur(12px);
}

.app-nav__brand {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.app-nav__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(140deg, rgba(56, 189, 248, 0.18), rgba(129, 140, 248, 0.18));
  border: 1px solid var(--om-border-strong);
}

.app-nav__name {
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: -0.02em;
}

.app-nav__tag {
  font-size: 0.62rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--om-accent);
  padding: 0.12rem 0.42rem;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.25);
}

.app-nav__meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.app-nav__links {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  margin: 0 auto;
}

.app-nav__link {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.75rem;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--om-text-muted);
  border: 1px solid transparent;
  transition: color 0.15s ease, background 0.15s ease, border-color 0.15s ease;
}

.app-nav__link:hover {
  color: var(--om-text);
  background: var(--om-surface);
  text-decoration: none;
}

.app-nav__link.router-link-active {
  color: var(--om-accent);
  background: rgba(56, 189, 248, 0.12);
  border-color: rgba(56, 189, 248, 0.25);
}

.app-nav__endpoint {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-family: var(--om-font-mono);
  font-size: 0.74rem;
  color: var(--om-text-muted);
  padding: 0.32rem 0.6rem;
  border-radius: 8px;
  background: var(--om-surface);
  border: 1px solid var(--om-border);
}

.app-nav__status {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.74rem;
  color: var(--om-text-muted);
}

.app-nav__status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--om-sev-low);
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.18);
  animation: om-pulse 2s ease-in-out infinite;
}

@keyframes om-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.45;
  }
}

.app-content {
  flex: 1;
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: clamp(1.5rem, 4vw, 3rem) clamp(1rem, 4vw, 2rem) 3rem;
}

.app-footer {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding: 1.25rem clamp(1rem, 4vw, 2.5rem);
  border-top: 1px solid var(--om-border);
  color: var(--om-text-dim);
  font-size: 0.78rem;
}

@media (max-width: 600px) {
  .app-nav__endpoint {
    display: none;
  }
}
</style>

