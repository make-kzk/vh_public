import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { fetchSurveyGroups } from '../../api/seekerApi'
import type { SurveyGroupsResponseDto, SurveyStatus } from '../../api/types'
import { FormSection } from '../../components/FormSection'

const STATUS_LABELS: Record<SurveyStatus, string> = {
  NOT_STARTED: 'Не начат',
  IN_PROGRESS: 'В процессе',
  COMPLETED: 'Пройден',
}

function StatusBadge({ status }: { status: SurveyStatus }) {
  const colors =
    status === 'COMPLETED'
      ? 'bg-green-100 text-green-800'
      : status === 'IN_PROGRESS'
        ? 'bg-blue-100 text-blue-800'
        : 'bg-neutral-100 text-neutral-700'
  return <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${colors}`}>{STATUS_LABELS[status]}</span>
}

export function SeekerTestsListPage() {
  const [data, setData] = useState<SurveyGroupsResponseDto | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void fetchSurveyGroups()
      .then(setData)
      .catch((e: Error) => setError(e.message))
  }, [])

  if (error != null) return <p className="text-sm text-red-600">{error}</p>
  if (data == null) return <p className="text-sm text-neutral-500">Загрузка…</p>

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/seeker/personality" className="text-sm text-neutral-600 underline">
          ← Личностные характеристики
        </Link>
        <h1 className="mt-2 text-2xl font-semibold">Личностные тесты</h1>
        <p className="mt-1 text-sm text-neutral-600">
          Пройдено групп: {data.testsCompleted} / {data.testsTotal}
        </p>
      </div>

      {data.groups.map((group) => (
        <FormSection
          key={group.code}
          title={group.name}
          description={`${group.completedCount} из ${group.totalCount} методик пройдено`}
        >
          <div className="flex flex-col gap-3">
            {group.surveys.map((survey) => (
              <div
                key={survey.id}
                className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-neutral-200 p-4"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-medium text-sm">{survey.name}</h3>
                    <StatusBadge status={survey.status} />
                  </div>
                  <p className="mt-1 text-sm text-neutral-600">{survey.description}</p>
                </div>
                {survey.status === 'COMPLETED' ? (
                  <span className="text-sm text-green-700">Пройдено</span>
                ) : (
                  <Link
                    to={`/seeker/personality/tests/${survey.id}`}
                    className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white"
                  >
                    {survey.status === 'IN_PROGRESS' ? 'Продолжить' : 'Начать'}
                  </Link>
                )}
              </div>
            ))}
          </div>
        </FormSection>
      ))}
    </div>
  )
}
