import type { AuthUserDto } from '../api/types'
import { AdaptiveLayout } from '../components/AdaptiveLayout'

interface RoleSelectionPageProps {
  user: AuthUserDto
  isBusy: boolean
  errorMessage: string | null
  onSelectSeeker: () => void
  onSelectEmployer: () => void
}

export function RoleSelectionPage({
  user,
  isBusy,
  errorMessage,
  onSelectSeeker,
  onSelectEmployer,
}: RoleSelectionPageProps) {
  const welcomeName = user.displayName != null ? `, ${user.displayName}` : ''

  return (
    <AdaptiveLayout>
      <div className="flex flex-col items-center gap-4">
        <h1 className="text-center text-xl font-semibold">Выберите тип аккаунта</h1>
        <p className="text-center text-sm text-neutral-600">
          Добро пожаловать{welcomeName}. Этот выбор нельзя изменить для {user.email}.
        </p>
        {errorMessage != null && (
          <p className="w-full text-sm text-red-600">{errorMessage}</p>
        )}
        {isBusy ? (
          <div className="flex justify-center py-2">
            <div
              className="h-8 w-8 animate-spin rounded-full border-4 border-neutral-300 border-t-neutral-900"
              role="status"
              aria-label="Загрузка"
            />
          </div>
        ) : (
          <>
            <button
              type="button"
              onClick={onSelectSeeker}
              className="w-full rounded-lg bg-neutral-900 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-neutral-800"
            >
              Ищу работу
            </button>
            <button
              type="button"
              onClick={onSelectEmployer}
              className="w-full rounded-lg border border-neutral-300 bg-white px-4 py-2.5 text-sm font-medium text-neutral-900 transition hover:bg-neutral-50"
            >
              Нанимаю сотрудников
            </button>
          </>
        )}
      </div>
    </AdaptiveLayout>
  )
}
