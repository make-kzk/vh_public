import type { AuthUserDto } from '../../api/types'
import { FormSection } from '../../components/FormSection'

interface SettingsPageProps {
  user: AuthUserDto
}

export function SettingsPage({ user }: SettingsPageProps) {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Настройки</h1>
        <p className="mt-1 text-sm text-neutral-600">Управление аккаунтом</p>
      </div>
      <FormSection title="Электронная почта" description="Раздел в разработке">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-neutral-700">Текущий адрес</span>
          <input
            type="email"
            value={user.email}
            readOnly
            className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-600"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-neutral-700">Новый адрес</span>
          <input
            type="email"
            disabled
            placeholder="example@mail.ru"
            className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-500 disabled:cursor-not-allowed"
          />
        </label>
        <button
          type="button"
          disabled
          className="self-start rounded-lg bg-neutral-200 px-4 py-2 text-sm text-neutral-500"
        >
          Сохранить (скоро)
        </button>
      </FormSection>
      <FormSection title="Пароль" description="Раздел в разработке">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-neutral-700">Текущий пароль</span>
          <input
            type="password"
            disabled
            className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-500 disabled:cursor-not-allowed"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-neutral-700">Новый пароль</span>
          <input
            type="password"
            disabled
            className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-500 disabled:cursor-not-allowed"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-neutral-700">Подтверждение пароля</span>
          <input
            type="password"
            disabled
            className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-500 disabled:cursor-not-allowed"
          />
        </label>
        <button
          type="button"
          disabled
          className="self-start rounded-lg bg-neutral-200 px-4 py-2 text-sm text-neutral-500"
        >
          Изменить пароль (скоро)
        </button>
      </FormSection>
      <FormSection title="Удаление аккаунта" description="Раздел в разработке">
        <p className="text-sm text-neutral-600">
          Удаление аккаунта необратимо. Все ваши данные будут удалены без возможности восстановления.
        </p>
        <button
          type="button"
          disabled
          className="self-start rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-400"
        >
          Удалить аккаунт (скоро)
        </button>
      </FormSection>
    </div>
  )
}
