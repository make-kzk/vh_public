import { useEffect, useState } from 'react'
import { fetchPersonalityPreview } from '../../api/seekerApi'
import type { PersonalityPreviewDto } from '../../api/types'
import { FormSection } from '../../components/FormSection'

const AXIS_LABELS: Record<string, string> = {
  axisDominance: 'Доминантность',
  axisInfluence: 'Влияние',
  axisStability: 'Стабильность',
  axisIntegrity: 'Добросовестность',
  axisAutonomy: 'Автономность',
  axisPace: 'Темп',
}

export function SeekerPersonalityPage() {
  const [data, setData] = useState<PersonalityPreviewDto | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void fetchPersonalityPreview()
      .then(setData)
      .catch((e: Error) => setError(e.message))
  }, [])

  if (error != null) return <p className="text-sm text-red-600">{error}</p>
  if (data == null) return <p className="text-sm text-neutral-500">Загрузка…</p>

  const axes = [
    data.axisDominance,
    data.axisInfluence,
    data.axisStability,
    data.axisIntegrity,
    data.axisAutonomy,
    data.axisPace,
  ]
  const axisKeys = Object.keys(AXIS_LABELS)

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Личностные характеристики</h1>
        <p className="mt-1 text-sm text-neutral-600">Предварительный профиль (демо-данные)</p>
      </div>
      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
        Пройдите 3 теста для точного мэтчинга. Сейчас отображается пример профиля (
        {data.testsCompleted}/{data.testsTotal} тестов пройдено).
      </div>
      <FormSection title={data.title} description={data.description}>
        <p className="text-sm text-neutral-700">{data.profile}</p>
      </FormSection>
      <FormSection title="Профиль DISC">
        <div className="flex flex-col gap-3">
          {axes.map((value, index) => (
            <div key={axisKeys[index]}>
              <div className="mb-1 flex justify-between text-sm">
                <span>{AXIS_LABELS[axisKeys[index]]}</span>
                <span>{Math.round(value * 100)}%</span>
              </div>
              <div className="h-2 rounded-full bg-neutral-100">
                <div
                  className="h-2 rounded-full bg-neutral-900"
                  style={{ width: `${value * 100}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </FormSection>
      {data.categories.map((category) => (
        <FormSection key={category.key} title={category.key} description={category.description}>
          <div className="flex flex-col gap-4">
            {category.traits.map((trait) => (
              <div key={trait.key} className="rounded-lg bg-neutral-50 p-3">
                <p className="font-medium text-sm">{trait.label}</p>
                <p className="mt-1 text-sm text-neutral-600">{trait.description}</p>
              </div>
            ))}
          </div>
        </FormSection>
      ))}
      <FormSection title={data.energySources.title}>
        <ul className="flex flex-col gap-3">
          {data.energySources.items.map((item) => (
            <li key={item.title}>
              <p className="font-medium text-sm">{item.title}</p>
              <p className="text-sm text-neutral-600">{item.description}</p>
            </li>
          ))}
        </ul>
      </FormSection>
      <FormSection title={data.stopFactors.title}>
        <ul className="flex flex-col gap-3">
          {data.stopFactors.items.map((item) => (
            <li key={item.title}>
              <p className="font-medium text-sm">{item.title}</p>
              <p className="text-sm text-neutral-600">{item.description}</p>
            </li>
          ))}
        </ul>
      </FormSection>
    </div>
  )
}
