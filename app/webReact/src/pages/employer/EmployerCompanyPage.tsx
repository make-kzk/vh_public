import { useEffect, useState } from 'react'
import { fetchEmployerProfile, updateEmployerProfile } from '../../api/employerApi'
import type { EmployerProfileDto } from '../../api/types'
import { FormSection } from '../../components/FormSection'

export function EmployerCompanyPage() {
  const [profile, setProfile] = useState<EmployerProfileDto | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    void fetchEmployerProfile()
      .then(setProfile)
      .catch((e: Error) => setError(e.message))
  }, [])

  async function save(e: React.FormEvent) {
    e.preventDefault()
    if (profile == null) return
    setSaving(true)
    setMessage(null)
    setError(null)
    try {
      const updated = await updateEmployerProfile({
        name: profile.name,
        description: profile.description,
        website: profile.website,
        phone: profile.phone,
        emailContact: profile.emailContact,
      })
      setProfile(updated)
      setMessage('Данные компании сохранены')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ошибка сохранения')
    } finally {
      setSaving(false)
    }
  }

  if (profile == null && error == null) {
    return <p className="text-sm text-neutral-500">Загрузка…</p>
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Компания</h1>
        <p className="mt-1 text-sm text-neutral-600">Информация о вашей организации</p>
      </div>
      {message != null && <p className="text-sm text-green-700">{message}</p>}
      {error != null && <p className="text-sm text-red-600">{error}</p>}
      {profile != null && (
        <form onSubmit={(e) => void save(e)}>
          <FormSection title="О компании">
            <label className="flex flex-col gap-1">
              <span className="text-sm font-medium">Название</span>
              <input
                required
                value={profile.name}
                onChange={(e) => setProfile({ ...profile, name: e.target.value })}
                className="rounded-lg border border-neutral-300 px-3 py-2 text-sm"
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="text-sm font-medium">Описание</span>
              <textarea
                rows={4}
                value={profile.description ?? ''}
                onChange={(e) =>
                  setProfile({ ...profile, description: e.target.value || null })
                }
                className="rounded-lg border border-neutral-300 px-3 py-2 text-sm"
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="text-sm font-medium">Сайт</span>
              <input
                value={profile.website ?? ''}
                onChange={(e) => setProfile({ ...profile, website: e.target.value || null })}
                className="rounded-lg border border-neutral-300 px-3 py-2 text-sm"
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="text-sm font-medium">Телефон</span>
              <input
                value={profile.phone ?? ''}
                onChange={(e) => setProfile({ ...profile, phone: e.target.value || null })}
                className="rounded-lg border border-neutral-300 px-3 py-2 text-sm"
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="text-sm font-medium">Email для связи</span>
              <input
                type="email"
                value={profile.emailContact ?? ''}
                onChange={(e) =>
                  setProfile({ ...profile, emailContact: e.target.value || null })
                }
                className="rounded-lg border border-neutral-300 px-3 py-2 text-sm"
              />
            </label>
            <button
              type="submit"
              disabled={saving}
              className="self-start rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
            >
              Сохранить
            </button>
          </FormSection>
        </form>
      )}
    </div>
  )
}
