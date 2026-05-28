# AGENTS.md

This repository uses **specification-driven development**. Specs are the source of truth for scope, behavior, and acceptance criteria. Agents and contributors must keep specs and implementation in sync.

## Quick start (for any change)

1. Create or update a spec in **/specs** and set **Status: Draft**.
2. Get approval before coding (Status: Approved).
3. Implement exactly what the spec defines.
4. Validate against the acceptance criteria and update Status to Implemented.

## Spec location and naming

Specs live under **/specs** with a stable, ordered name:

```
specs/NNNN-short-title.md
```

Use a numeric prefix for ordering (0001, 0002, ...). Update the same file for revisions and log changes in the spec.

## Spec status lifecycle

Allowed statuses:

- Draft
- In Review
- Approved
- Implemented
- Deprecated (optional, for superseded specs)

Only Approved specs may be implemented. If scope changes, return to Draft or In Review.

## Required spec sections

```md
# Spec: <short title>
Status: Draft | In Review | Approved | Implemented | Deprecated
Owners: <names or handles>

## Goal
What outcome are we trying to achieve?

## Non-goals
What is explicitly out of scope?

## Context
Relevant background, constraints, and dependencies.

## Design
Architecture, data flow, APIs, UI/UX notes, and key decisions.

## Acceptance criteria
Numbered, verifiable statements.

## Test plan
What to run and what to verify.

## Rollout / compatibility
Migrations, backward compatibility, feature flags.

## Decision log
Date-stamped decisions and changes.
```

## Acceptance criteria guidelines

- Write criteria as **verifiable outcomes**, not implementation details.
- Use numbering for traceability.
- Prefer precise language: "When X, then Y" or "Given/When/Then."
- Ensure every criterion has a corresponding test or manual verification step.

## Test plan expectations

- Specify the exact Gradle tasks to run.
- Include any manual checks (UI, device behavior, migration validation).
- If a criterion cannot be tested automatically, document the manual steps.

## Traceability requirements

Every change must reference its spec:

- Commit message footer, PR description, or issue link must include the spec path.
- If the change updates behavior, update the spec and README (if user-facing).

## Workflow for agents

1. **Draft the spec.** Fill all sections and set Status to Draft.
2. **Seek approval.** Do not implement until Status is Approved.
3. **Implement.** Keep code aligned to the spec and update it when decisions change.
4. **Validate.** Ensure acceptance criteria are met and set Status to Implemented.

## Kotlin/Gradle conventions

- Use the Gradle wrapper for all tasks.
- Prefer unit tests in the module where behavior is implemented.
- Avoid introducing new modules without an approved spec.
- Keep dependencies minimal and aligned with the version catalog.

## Handling bug fixes

Create a spec even for small fixes. Include:

- Repro steps
- Root cause
- Acceptance criteria that prevent regressions

## When a spec is missing

If a request does not include a spec, create one and request approval before coding. Only skip the spec step if explicitly instructed.
