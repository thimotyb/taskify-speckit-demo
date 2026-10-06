# Taskify Frontend

React 18 + TypeScript + Material UI single-page app served on port **3000** in development.

## What it does (so far)

- Start screen: choose one of the five predefined users (no login). The choice is kept in
  `localStorage` and survives reloads.
- Project list and a four-column Kanban board (To Do, In Progress, In Review, Done) with assignee and
  comment count on each card. All text renders as plain text.
- Data is fetched on open, on window focus, and with the Refresh button. There is no live push.

If the server rejects the selected identity (401), the app returns to the start screen.

## Run and test

```bash
npm install
npm run dev        # http://localhost:3000, proxies /api to the gateway (TASKIFY_GATEWAY_URL, default :8080)
npm test           # Vitest component tests
npm run lint       # ESLint, including the TSDoc requirement and the dangerouslySetInnerHTML ban
npm run e2e        # Playwright; needs the whole stack running (see ../README or quickstart.md)
npm run build      # type check + production build
```

## Layout

`src/pages` (screens), `src/components` (board, column, card, shell), `src/services` (typed API
clients), `src/state` (acting-user context). Contracts live in
`../specs/001-taskify-kanban/contracts/`.
