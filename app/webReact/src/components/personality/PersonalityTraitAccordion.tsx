import type { PersonalityTraitDto } from '../../api/types'
import {
  LABEL_BAD_DAY,
  LABEL_GOOD_DAY,
  LABEL_SUCCEED_THROUGH,
  LABEL_TOP_STRENGTH,
} from './personalityLabels'
import { PersonalityTraitScale } from './PersonalityTraitScale'

interface PersonalityTraitAccordionProps {
  trait: PersonalityTraitDto
}

export function PersonalityTraitAccordion({ trait }: PersonalityTraitAccordionProps) {
  return (
    <details className="group rounded-lg border border-neutral-200 bg-neutral-50">
      <summary className="flex cursor-pointer list-none items-start justify-between gap-3 p-4 marker:content-none [&::-webkit-details-marker]:hidden">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          <span className="text-sm font-semibold text-neutral-900">{trait.label}</span>
          {trait.isTopStrength === true && (
            <span className="shrink-0 rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-900">
              {LABEL_TOP_STRENGTH}
            </span>
          )}
        </div>
        <span
          className="mt-0.5 shrink-0 text-neutral-400 transition-transform group-open:rotate-180"
          aria-hidden
        >
          ▾
        </span>
      </summary>
      <div className="border-t border-neutral-200 px-4 pb-4 pt-3">
        <p className="text-sm text-neutral-700">{trait.description}</p>
        <PersonalityTraitScale
          leftPole={trait.leftPole}
          rightPole={trait.rightPole}
          scalePosition={trait.scalePosition}
        />
        <div className="mt-4 flex flex-col gap-3 text-sm">
          {trait.goodDay.length > 0 && (
            <div>
              <p className="font-medium text-neutral-800">{LABEL_GOOD_DAY}</p>
              <p className="mt-0.5 text-neutral-600">{trait.goodDay}</p>
            </div>
          )}
          {trait.badDay.length > 0 && (
            <div>
              <p className="font-medium text-neutral-800">{LABEL_BAD_DAY}</p>
              <p className="mt-0.5 text-neutral-600">{trait.badDay}</p>
            </div>
          )}
          {trait.succeedThrough.length > 0 && (
            <div>
              <p className="font-medium text-neutral-800">{LABEL_SUCCEED_THROUGH}</p>
              <ul className="mt-1 list-disc space-y-0.5 pl-5 text-neutral-600">
                {trait.succeedThrough.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>
    </details>
  )
}
