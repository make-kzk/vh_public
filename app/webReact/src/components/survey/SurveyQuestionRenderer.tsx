import type { SurveyQuestionsDefinition } from '../../api/types'

type Answers = Record<string, unknown>

interface Props {
  definition: SurveyQuestionsDefinition
  answers: Answers
  onChange: (answers: Answers) => void
  pageIndex?: number
  pageSize?: number
}

function parseSelected(value: unknown): number[] {
  if (!Array.isArray(value)) return []
  return value.filter((v): v is number => typeof v === 'number')
}

function toggleSelection(current: number[], id: number, max: number): number[] {
  if (current.includes(id)) return current.filter((x) => x !== id)
  if (current.length >= max) return current
  return [...current, id]
}

function getBlockAnswers(answers: Answers, key: string): Record<string, number> {
  const block = answers[key]
  if (block == null || typeof block !== 'object' || Array.isArray(block)) return {}
  return block as Record<string, number>
}

function setBlockPoint(
  answers: Answers,
  key: string,
  optionId: number,
  value: number,
  onChange: (a: Answers) => void,
) {
  const block = { ...getBlockAnswers(answers, key), [String(optionId)]: value }
  onChange({ ...answers, [key]: block })
}

export function SurveyQuestionRenderer({ definition, answers, onChange, pageIndex = 0, pageSize }: Props) {
  const { type, instruction } = definition

  if (type === 'open_questions') {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-sm text-neutral-600">{instruction}</p>
        {definition.questions?.map((q) => (
          <label key={q.id} className="flex flex-col gap-1">
            <span className="text-sm font-medium">{q.text}</span>
            <textarea
              rows={3}
              value={String(answers[String(q.id)] ?? '')}
              onChange={(e) => onChange({ ...answers, [String(q.id)]: e.target.value })}
              className="rounded-lg border border-neutral-300 px-3 py-2 text-sm"
            />
          </label>
        ))}
      </div>
    )
  }

  if (type === 'multi_select') {
    const key = definition.answerKey ?? 'selected'
    const selected = parseSelected(answers[key])
    const max = definition.maxSelections ?? selected.length
    const options = definition.options ?? []
    return (
      <div className="flex flex-col gap-4">
        <p className="text-sm text-neutral-600">{instruction}</p>
        <p className="text-xs text-neutral-500">
          Выбрано: {selected.length} / {max}
        </p>
        <div className="grid gap-2 sm:grid-cols-2">
          {options.map((opt) => (
            <label key={opt.id} className="flex items-center gap-2 rounded-lg border border-neutral-200 p-2 text-sm">
              <input
                type="checkbox"
                checked={selected.includes(opt.id)}
                onChange={() => onChange({ ...answers, [key]: toggleSelection(selected, opt.id, max) })}
              />
              {opt.label}
            </label>
          ))}
        </div>
      </div>
    )
  }

  if (type === 'binary_choice') {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-sm text-neutral-600">{instruction}</p>
        {definition.questions?.map((q) => (
          <fieldset key={q.id} className="rounded-lg border border-neutral-200 p-3">
            <legend className="px-1 text-sm font-medium">Вопрос {q.id}</legend>
            <div className="mt-2 flex flex-col gap-2">
              {[1, 2].map((choice) => (
                <label key={choice} className="flex items-center gap-2 text-sm">
                  <input
                    type="radio"
                    name={`q-${q.id}`}
                    checked={answers[String(q.id)] === choice}
                    onChange={() => onChange({ ...answers, [String(q.id)]: choice })}
                  />
                  {choice === 1 ? q.option1 : q.option2}
                </label>
              ))}
            </div>
          </fieldset>
        ))}
      </div>
    )
  }

  if (type === 'scale_0_4') {
    const all = definition.questions ?? []
    const start = pageSize != null ? pageIndex * pageSize : 0
    const items = pageSize != null ? all.slice(start, start + pageSize) : all
    return (
      <div className="flex flex-col gap-4">
        <p className="text-sm text-neutral-600">{instruction}</p>
        {items.map((q) => (
          <fieldset key={q.id} className="rounded-lg border border-neutral-200 p-3">
            <legend className="px-1 text-sm">{q.text}</legend>
            <div className="mt-2 flex flex-wrap gap-3">
              {[0, 1, 2, 3, 4].map((v) => (
                <label key={v} className="flex items-center gap-1 text-sm">
                  <input
                    type="radio"
                    name={`scale-${q.id}`}
                    checked={answers[String(q.id)] === v}
                    onChange={() => onChange({ ...answers, [String(q.id)]: v })}
                  />
                  {v}
                </label>
              ))}
            </div>
          </fieldset>
        ))}
      </div>
    )
  }

  if (type === 'allocate_points' || type === 'belbin_matrix') {
    const total = definition.totalPoints ?? 10
    const maxPer = definition.maxPerOption ?? 5
    const prefix = type === 'belbin_matrix' ? 'section_' : 'q'
    return (
      <div className="flex flex-col gap-6">
        <p className="text-sm text-neutral-600">{instruction}</p>
        {(definition.questions ?? []).map((q) => {
          const key = `${prefix}${q.id}`
          const block = getBlockAnswers(answers, key)
          const sum = Object.values(block).reduce((a, b) => a + (b ?? 0), 0)
          return (
            <fieldset key={q.id} className="rounded-lg border border-neutral-200 p-3">
              <legend className="px-1 text-sm font-medium">{q.text}</legend>
              <p className="mt-1 text-xs text-neutral-500">
                Сумма: {sum} / {total}
              </p>
              <div className="mt-3 flex flex-col gap-2">
                {(q.options ?? []).map((opt) => (
                  <label key={opt.id} className="flex items-center justify-between gap-3 text-sm">
                    <span className="flex-1">{opt.label}</span>
                    <input
                      type="number"
                      min={0}
                      max={maxPer}
                      value={block[String(opt.id)] ?? 0}
                      onChange={(e) => {
                        const v = Math.min(maxPer, Math.max(0, Number(e.target.value) || 0))
                        setBlockPoint(answers, key, opt.id, v, onChange)
                      }}
                      className="w-16 rounded border border-neutral-300 px-2 py-1"
                    />
                  </label>
                ))}
              </div>
            </fieldset>
          )
        })}
      </div>
    )
  }

  return <p className="text-sm text-red-600">Неподдерживаемый тип опроса: {type}</p>
}

export function isSurveyComplete(definition: SurveyQuestionsDefinition, answers: Answers): boolean {
  try {
    switch (definition.type) {
      case 'open_questions':
        return (definition.questions ?? []).every((q) => String(answers[String(q.id)] ?? '').trim().length > 0)
      case 'multi_select': {
        const key = definition.answerKey ?? 'selected'
        const n = parseSelected(answers[key]).length
        return n === (definition.minSelections ?? n)
      }
      case 'binary_choice':
        return (definition.questions ?? []).every((q) => answers[String(q.id)] === 1 || answers[String(q.id)] === 2)
      case 'scale_0_4':
        return (definition.questions ?? []).every(
          (q) => typeof answers[String(q.id)] === 'number' && (answers[String(q.id)] as number) >= 0,
        )
      case 'allocate_points':
      case 'belbin_matrix': {
        const total = definition.totalPoints ?? 10
        const prefix = definition.type === 'belbin_matrix' ? 'section_' : 'q'
        return (definition.questions ?? []).every((q) => {
          const block = getBlockAnswers(answers, `${prefix}${q.id}`)
          const sum = Object.values(block).reduce((a, b) => a + (b ?? 0), 0)
          return sum === total
        })
      }
      default:
        return false
    }
  } catch {
    return false
  }
}

export function parseSurveyDefinition(json: string): SurveyQuestionsDefinition {
  return JSON.parse(json) as SurveyQuestionsDefinition
}

export function parseAnswersJson(json: string | null): Answers {
  if (json == null || json === '') return {}
  try {
    return JSON.parse(json) as Answers
  } catch {
    return {}
  }
}
