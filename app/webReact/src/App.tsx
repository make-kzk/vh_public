import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { LoadingSpinner } from './components/LoadingSpinner'
import { ProtectedRoute } from './components/ProtectedRoute'
import { useAuth } from './hooks/useAuth'
import { EmployerCandidatesPage } from './pages/employer/EmployerCandidatesPage'
import { EmployerCompanyPage } from './pages/employer/EmployerCompanyPage'
import { EmployerDashboardPage } from './pages/employer/EmployerDashboardPage'
import { EmployerProfilesPage } from './pages/employer/EmployerProfilesPage'
import { LoginPage } from './pages/LoginPage'
import { RoleSelectionPage } from './pages/RoleSelectionPage'
import { SeekerDashboardPage } from './pages/seeker/SeekerDashboardPage'
import { SeekerPersonalityPage } from './pages/seeker/SeekerPersonalityPage'
import { SeekerPositionsPage } from './pages/seeker/SeekerPositionsPage'
import { SeekerProfilePage } from './pages/seeker/SeekerProfilePage'
import { SettingsPage } from './pages/shared/SettingsPage'

const SEEKER_NAV = [
  { to: '/seeker', label: 'Дашборд', end: true },
  { to: '/seeker/personality', label: 'Личность' },
  { to: '/seeker/profile', label: 'Профиль' },
  { to: '/seeker/positions', label: 'Должности' },
  { to: '/seeker/settings', label: 'Настройки' },
]

const EMPLOYER_NAV = [
  { to: '/employer', label: 'Дашборд', end: true },
  { to: '/employer/company', label: 'Компания' },
  { to: '/employer/profiles', label: 'Профили' },
  { to: '/employer/settings', label: 'Настройки' },
]

function AuthFlow() {
  const { state, errorMessage, isBusy, signInDev, completeRegistration } = useAuth()
  const location = useLocation()
  const isRolePath = location.pathname.endsWith('/auth/role')

  if (state.kind === 'authenticated') {
    const home = state.user.role === 'EMPLOYER' ? '/employer' : '/seeker'
    if (location.pathname === '/' || isRolePath) {
      return <Navigate to={home} replace />
    }
  }

  if (isRolePath) {
    switch (state.kind) {
      case 'needsRegistration':
        return (
          <RoleSelectionPage
            user={state.user}
            isBusy={isBusy}
            errorMessage={errorMessage}
            onSelectSeeker={() => void completeRegistration('SEEKER')}
            onSelectEmployer={() => void completeRegistration('EMPLOYER')}
          />
        )
      case 'authenticated':
        return <Navigate to={state.user.role === 'EMPLOYER' ? '/employer' : '/seeker'} replace />
      case 'loading':
      case 'unauthenticated':
        return <LoadingSpinner />
    }
  }

  switch (state.kind) {
    case 'loading':
      return <LoadingSpinner />
    case 'unauthenticated':
      return (
        <LoginPage
          isBusy={isBusy}
          errorMessage={errorMessage}
          onSignIn={(email) => void signInDev(email)}
        />
      )
    case 'needsRegistration':
      return (
        <RoleSelectionPage
          user={state.user}
          isBusy={isBusy}
          errorMessage={errorMessage}
          onSelectSeeker={() => void completeRegistration('SEEKER')}
          onSelectEmployer={() => void completeRegistration('EMPLOYER')}
        />
      )
    case 'authenticated':
      return <Navigate to={state.user.role === 'EMPLOYER' ? '/employer' : '/seeker'} replace />
  }
}

function SeekerLayout() {
  const { state, signOut } = useAuth()
  if (state.kind !== 'authenticated') return <LoadingSpinner />
  return (
    <ProtectedRoute state={state} requiredRole="SEEKER">
      <AppShell
        user={state.user}
        role="SEEKER"
        navItems={SEEKER_NAV}
        onLogout={() => void signOut()}
      />
    </ProtectedRoute>
  )
}

function EmployerLayout() {
  const { state, signOut } = useAuth()
  if (state.kind !== 'authenticated') return <LoadingSpinner />
  return (
    <ProtectedRoute state={state} requiredRole="EMPLOYER">
      <AppShell
        user={state.user}
        role="EMPLOYER"
        navItems={EMPLOYER_NAV}
        onLogout={() => void signOut()}
      />
    </ProtectedRoute>
  )
}

function AppRoutes() {
  const { state } = useAuth()

  return (
    <Routes>
      <Route path="/" element={<AuthFlow />} />
      <Route path="/auth/role" element={<AuthFlow />} />
      <Route path="/seeker" element={<SeekerLayout />}>
        <Route index element={<SeekerDashboardPage />} />
        <Route path="personality" element={<SeekerPersonalityPage />} />
        <Route path="profile" element={<SeekerProfilePage />} />
        <Route path="positions" element={<SeekerPositionsPage />} />
        <Route
          path="settings"
          element={
            state.kind === 'authenticated' ? (
              <SettingsPage user={state.user} />
            ) : (
              <LoadingSpinner />
            )
          }
        />
      </Route>
      <Route path="/employer" element={<EmployerLayout />}>
        <Route index element={<EmployerDashboardPage />} />
        <Route path="company" element={<EmployerCompanyPage />} />
        <Route path="profiles" element={<EmployerProfilesPage />} />
        <Route path="profiles/:id/candidates" element={<EmployerCandidatesPage />} />
        <Route
          path="settings"
          element={
            state.kind === 'authenticated' ? (
              <SettingsPage user={state.user} />
            ) : (
              <LoadingSpinner />
            )
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  )
}
