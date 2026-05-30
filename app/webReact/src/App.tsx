import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { LoadingSpinner } from './components/LoadingSpinner'
import { useAuth } from './hooks/useAuth'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RoleSelectionPage } from './pages/RoleSelectionPage'

function AuthFlow() {
  const { state, errorMessage, isBusy, signInDev, completeRegistration, signOut } =
    useAuth()
  const location = useLocation()
  const isRolePath = location.pathname.endsWith('/auth/role')

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
        return <HomePage user={state.user} onLogout={() => void signOut()} />
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
      return <HomePage user={state.user} onLogout={() => void signOut()} />
  }
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AuthFlow />} />
        <Route path="/auth/role" element={<AuthFlow />} />
      </Routes>
    </BrowserRouter>
  )
}
