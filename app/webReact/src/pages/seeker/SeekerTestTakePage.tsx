import { Link, useNavigate, useParams } from 'react-router-dom'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { completeSurvey, fetchSurvey, saveSurveyAnswers, startSurvey } from '../../api/seekerApi'
import type { SurveyDetailDto } from '../../api/types'
import {
  SurveyQuestionRenderer,
  isSurveyComplete,
  parseAnswersJson,
  parseSurveyDefinition,
} from '../../components/survey/SurveyQuestionRenderer'

const SCALE_PAGE_SIZE = 8

export function SeekerTestTakePage() {
  const { surveyId } = useParams()
  const navigate = useNavigate()
  const id = Number(surveyId)
  const [survey, setSurvey] = useState<SurveyDetailDto | null>(null)
  const [answers, setAnswers] = useState<Record<string, unknown>>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [scalePage, setScalePage] = useState(0)

  const load = useCallback(async () => {
    if (!Number.isFinite(id)) {
      setError('Некорректный идентификатор теста')
      return
    }
    let detail = await fetchSurvey(id)
    if (detail.status === 'COMPLETED') {
      navigate('/seeker/personality/tests', { replace: true })
      return
    }
    if (detail.status === 'NOT_STARTED') {
      detail = await startSurvey(id)
    }
    setSurvey(detail)
    setAnswers(parseAnswersJson(detail.answersJson))
  }, [id, navigate])

  useEffect(() => {
    void load().catch((e: Error) => setError(e.message))
  }, [load])

  const definition = useMemo(
    () => (survey != null ? parseSurveyDefinition(survey.questionsJson) : null),
    [survey],
  )

  const scalePageCount =
    definition?.type === 'scale_0_4'
      ? Math.ceil((definition.questions?.length ?? 0) / SCALE_PAGE_SIZE)
      : 1

  async function persistAnswers(next: Record<string, unknown>) {
    setAnswers(next)
    if (survey == null) return
    try {
      await saveSurveyAnswers(survey.id, next)
    } catch {
      // autosave is best-effort
    }
  }

  async function handleComplete() {
    if (survey == null || definition == null) return
    if (!isSurveyComplete(definition, answers)) {
      setError('Ответьте на все вопросы, прежде чем завершить тест')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await completeSurvey(survey.id, answers)
      navigate('/seeker/personality/tests')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не удалось завершить тест')
    } finally {
      setSubmitting(false)
    }
  }

  if (error != null && survey == null) return <p className="text-sm text-red-600">{error}</p>
  if (survey == null || definition == null) return <p className="text-sm text-neutral-500">Загрузка…</p>

  const complete = isSurveyComplete(definition, answers)

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/seeker/personality/tests" className="text-sm text-neutral-600 underline">
          ← К списку тестов
        </Link>
        <h1 className="mt-2 text-2xl font-semibold">{survey.name}</h1>
        <p className="mt-1 text-sm text-neutral-600">{survey.description}</p>
      </div>

      {error != null && <p className="text-sm text-red-600">{error}</p>}

      {definition.type === 'scale_0_4' && scalePageCount > 1 && (
        <div className="flex items-center justify-between text-sm text-neutral-600">
          <span>
            Страница {scalePage + 1} / {scalePageCount}
          </span>
          <div className="flex gap-2">
            <button
              type="button"
              disabled={scalePage === 0}
              onClick={() => setScalePage((p) => p - 1)}
              className="rounded border border-neutral-300 px-3 py-1 disabled:opacity-40"
            >
              Назад
            </button>
            <button
              type="button"
              disabled={scalePage >= scalePageCount - 1}
              onClick={() => setScalePage((p) => p + 1)}
              className="rounded border border-neutral-300 px-3 py-1 disabled:opacity-40"
            >
              Далее
            </button>
          </div>
        </div>
      )}

      <SurveyQuestionRenderer
        definition={definition}
        answers={answers}
        onChange={(next) => void persistAnswers(next)}
        pageIndex={definition.type === 'scale_0_4' ? scalePage : undefined}
        pageSize={definition.type === 'scale_0_4' ? SCALE_PAGE_SIZE : undefined}
      />

      {(definition.type !== 'scale_0_4' || scalePage >= scalePageCount - 1) && (
        <button
          type="button"
          disabled={!complete || submitting}
          onClick={() => void handleComplete()}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {submitting ? 'Сохранение…' : 'Завершить тест'}
        </button>
      )}
    </div>
  )
}
