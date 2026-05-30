import { useCallback, useEffect, useState } from 'react'
import * as authApi from '../api/authApi'
import type { AuthState, AuthUserDto, UserRole } from '../api/types'

function userToState(user: AuthUserDto | null): AuthState {
  if (user == null) return { kind: 'unauthenticated' }
  if (user.role == null) return { kind: 'needsRegistration', user }
  return { kind: 'authenticated', user }
}

export function useAuth() {
  const [state, setState] = useState<AuthState>({ kind: 'loading' })
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isBusy, setIsBusy] = useState(false)

  const refreshSession = useCallback(async () => {
    setIsBusy(true)
    setErrorMessage(null)
    try {
      const user = await authApi.fetchMe()
      setState(userToState(user))
    } catch (e) {
      setErrorMessage(e instanceof Error ? e.message : 'Не удалось загрузить сессию')
      setState({ kind: 'unauthenticated' })
    } finally {
      setIsBusy(false)
    }
  }, [])

  useEffect(() => {
    void refreshSession()
  }, [refreshSession])

  const signInDev = useCallback(async (email: string) => {
    const normalizedEmail = email.trim().toLowerCase()
    if (normalizedEmail === '' || !normalizedEmail.includes('@')) {
      setErrorMessage('Введите корректный адрес электронной почты')
      return
    }
    setIsBusy(true)
    setErrorMessage(null)
    try {
      const user = await authApi.devLogin(normalizedEmail)
      setState(userToState(user))
    } catch (e) {
      setErrorMessage(e instanceof Error ? e.message : 'Не удалось войти')
      setState({ kind: 'unauthenticated' })
    } finally {
      setIsBusy(false)
    }
  }, [])

  const completeRegistration = useCallback(async (role: UserRole) => {
    setIsBusy(true)
    setErrorMessage(null)
    try {
      const user = await authApi.completeRegistration(role)
      setState({ kind: 'authenticated', user })
    } catch (e) {
      setErrorMessage(e instanceof Error ? e.message : 'Не удалось завершить регистрацию')
    } finally {
      setIsBusy(false)
    }
  }, [])

  const signOut = useCallback(async () => {
    setIsBusy(true)
    try {
      await authApi.logout()
    } catch {
      // ignore
    } finally {
      setState({ kind: 'unauthenticated' })
      setIsBusy(false)
    }
  }, [])

  return {
    state,
    errorMessage,
    isBusy,
    refreshSession,
    signInDev,
    completeRegistration,
    signOut,
  }
}
