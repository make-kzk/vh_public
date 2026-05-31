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
      <FormSection title="Аккаунт">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-neutral-700">Электронная почта</span>
          <input
            type="email"
            value={user.email}
            readOnly
            className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-600"
          />
        </label>
      </FormSection>
      <FormSection title="Оплата" description="Раздел в разработке">
        <button
          type="button"
          disabled
          className="self-start rounded-lg bg-neutral-200 px-4 py-2 text-sm text-neutral-500"
        >
          Управление подпиской (скоро)
        </button>
      </FormSection>
      <FormSection title="Отчёты" description="Раздел в разработке">
        <button
          type="button"
          disabled
          className="self-start rounded-lg bg-neutral-200 px-4 py-2 text-sm text-neutral-500"
        >
          Скачать отчёты (скоро)
        </button>
      </FormSection>
    </div>
  )
}
