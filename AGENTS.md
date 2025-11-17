# Repository Guidelines

## Project Structure & Module Organization
Move Tasker is presently a clean slate; as functionality lands, standardize on the structure below so future changes stay predictable:
```
src/
  features/<domain>/
  shared/
tests/
  unit/
  integration/
assets/
docs/
```
Keep feature folders self-contained (API client, hooks, UI, and Move scripts together) and reserve `shared/` for cross-cutting utilities only. Docs that explain workflows, API contracts, or migration guides belong in `docs/` so agents can cite them in proposals. Binary files (icons, seed data) live under `assets/`.

## Build, Test, and Development Commands
After you scaffold `package.json`, add these npm scripts and keep them stable:
- `npm install` – installs pinned dependencies; rerun after every lockfile change.
- `npm run dev` – local watcher that transpiles TS into `dist/` and reloads the dev server.
- `npm run build` – production bundle; fails on type errors or lint violations.
- `npm run lint` – ESLint + Prettier check; fix issues before opening a PR.
- `npm test` – executes Jest suites (unit and integration); pass before pushing.

## Coding Style & Naming Conventions
Write new logic in TypeScript with 2-space indentation, semicolons, and single quotes. Use `camelCase` for variables/functions, `PascalCase` for React components/classes, and `SCREAMING_SNAKE_CASE` for environment variables stored in `.env.local`. Files within `src/features/<domain>` should follow `feature-action.ts` naming (e.g., `tasks-create.ts`). Run `npm run lint -- --fix` before committing to enforce ESLint + Prettier defaults.

## Testing Guidelines
Unit specs sit in `tests/unit` and mirror the `src` tree (`src/features/tasks/create.ts` → `tests/unit/features/tasks/create.test.ts`). Integration specs belong in `tests/integration` and can spin up the dev server via `npm run dev` in a separate terminal. Target ≥80% statement coverage; add fixtures under `tests/fixtures` instead of inlining JSON blobs. Run `npm test -- --watch` while developing and include regression steps in the PR description whenever you add or modify tests.

## Commit & Pull Request Guidelines
Use conventional commits (`feat(tasks): add batch assignment`) so changelog automation stays reliable. One logical change per commit; avoid WIP commits in shared branches. Every PR should include: a succinct summary, linked issue or ticket number, screenshots or CLI output for UX changes, mention of schema/migration impacts, and clear test instructions. Request review only after CI passes locally; note any follow-up TODOs explicitly so the next agent knows what remains.
