# Vedic Panchang (Kotlin)

Kotlin/Android codebase for Vedic Panchang calculations and related UI. The project is organized as a Gradle multi-module workspace.

## Getting started

This repo uses a Gradle toolchain configured for **JDK 21**.

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

```bash
# Run unit tests
.\gradlew.bat test
```

## Specification-driven development

Work in this repo is driven by written specifications. Every meaningful change starts with a spec that defines the goal, constraints, and acceptance criteria. Implementation should not begin until the spec is approved.

Specs live under **/specs** (create the directory if it does not exist). For the full process and agent guidance, see **AGENTS.md**.

**Spec template (short form):**

```md
# Spec: <short title>
Status: Draft | Approved | Implemented

## Goal

## Non-goals

## Context

## Design

## Acceptance criteria

## Test plan

## Rollout / compatibility
```

## Repository layout

| Path | Purpose |
| --- | --- |
| app/ | Android application module |
| astronomy/ | Core astronomy and calculation logic |
| images/ | Project images and assets |
| gradle/ | Gradle version catalog and wrapper config |

## Contributing

Follow the specification-driven workflow in **AGENTS.md**. Link each change back to its spec, keep acceptance criteria up to date, and update docs when behavior changes.
