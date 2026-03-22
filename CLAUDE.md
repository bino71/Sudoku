# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An interactive Sudoku game in Java 26, structured as a Maven multi-module project with two desktop UIs (Swing and JavaFX), a Spring Boot web backend with Thymeleaf SSR, and an Angular 21 SPA — all sharing the same core engine.

## Build & Run

```bash
# Prerequisites: Java 26, Maven 3.9+
# Java 26 is at: C:\Users\Horst\.jdks\openjdk-26
# Maven 3.9 is at: D:\tools\maven\apache-maven-3.9.9\bin\mvn.cmd
# Set env: JAVA_HOME=C:\Users\Horst\.jdks\openjdk-26

# Build all modules + run tests
mvn clean install

# Run tests only (fast — <10s)
mvn -pl sudoku-core test

# Launch Swing UI
mvn -pl sudoku-swing exec:java

# Launch JavaFX UI
mvn -pl sudoku-javafx javafx:run

# Launch web UI (Spring Boot — serves Thymeleaf + Angular SPA at :8080)
mvn -pl sudoku-web spring-boot:run
# then open http://localhost:8080  (Thymeleaf) or http://localhost:8080/index.html (Angular)

# Angular dev server with live-reload (proxies /api → :8080)
cd sudoku-angular && npm start
# open http://localhost:4200
```

## Module Layout

```
sudoku/
├── pom.xml             ← parent POM: Java 26, junit 5.12.1, javafx 24.0.1
├── sudoku-core/        ← game logic (no UI deps)
├── sudoku-swing/       ← Swing UI, depends on sudoku-core
├── sudoku-javafx/      ← JavaFX UI, depends on sudoku-core
├── sudoku-web/         ← Spring Boot 3.4 + Thymeleaf SSR + REST API
└── sudoku-angular/     ← Angular 21 SPA (calls sudoku-web REST API)
```

## Architecture

### sudoku-core

All logic is in `src/main/java/io/bino/core/`:

- **`model/SudokuBoard`** — 9×9 grid, given vs. user cells, observable via `GameEventListener`
- **`model/GameState`** — central hub: board + timer + mistakes + completion; fires `GameEvent`s to listeners; call `fireEvent()` to force a UI refresh
- **`logic/SudokuValidator`** — stateless; `findConflicts()` returns `Set<String>` of `"row,col"` keys
- **`logic/SudokuSolver`** — backtracking + MRV heuristic; `solve(board)` mutates in-place; `findHint()` returns naked/hidden singles
- **`logic/SudokuGenerator`** — fill board → remove cells with uniqueness check; seeded `Random` for deterministic tests
- **`event/GameEvent.Type`** — `CELL_CHANGED, CONFLICT_DETECTED, CONFLICT_RESOLVED, HINT_APPLIED, PUZZLE_COMPLETE, TIMER_TICK, MISTAKE_INCREMENTED`

`GameState` forwards board-level `CELL_CHANGED` events to its own listener list. Timer fires `TIMER_TICK` from a background thread — UI listeners must dispatch via `SwingUtilities.invokeLater` / `Platform.runLater`.

### sudoku-swing

`src/main/java/io/bino/swing/`:
- `MainFrame` — JFrame, wires `BoardPanel` + `ControlPanel` to `GameState`
- `board/BoardPanel` — 9×9 `GridLayout` of `SudokuCellPanel`; `paintComponent` draws thick 3×3 box borders
- `board/SudokuCellPanel` — JTextField with `DocumentFilter` (accepts only digits 1-9); color states via `setBackground()`
- `control/ControlPanel` + `control/TimerLabel` — buttons, difficulty combo, timer, mistake counter

### sudoku-javafx

`src/main/java/io/bino/fx/`:
- `SudokuFxApp` — `extends Application`; creates `GameState`, wires `BoardGrid` + `ControlBar`
- `component/BoardGrid` — `GridPane` 9×9; CSS classes `cell-given/cell-user/cell-conflict/cell-selected/cell-hint`; box borders via `box-border-right/bottom/corner` CSS classes
- `component/ControlBar` — `VBox` with buttons, difficulty `ComboBox`, timer, mistake label

CSS: `src/main/resources/io/bino/fx/sudoku.css`

No `module-info.java` — `javafx-maven-plugin` handles module path automatically.

### sudoku-web

`src/main/java/io/bino/web/`:
- `SudokuWebApp` — `@SpringBootApplication` entry point
- `session/GameSession` — `@Component @SessionScope`; one `GameState` per HTTP session; **does NOT call `startTimer()`** (timer is client-side JS)
- `dto/BoardDto` — record mapping `GameState` → JSON (`values[][]`, `given[][]`, `conflicts[][]`, `complete`, `mistakeCount`, `difficulty`)
- `controller/GameRestController` — REST endpoints at `/api/game/{new,state,enter,hint,solve,reset}`
- `controller/GameViewController` — Thymeleaf route at `GET /thymeleaf`
- `config/WebConfig` — CORS for `:4200`, SPA fallback (`PathResourceResolver` → `index.html`)
- `templates/game.html` — full Thymeleaf board with JS fetch calls

### sudoku-angular

Angular 21 (Node 22.15+, TypeScript 5.9) standalone-components SPA in `src/app/`:
- `services/game.service.ts` — `HttpClient` calls to `/api/game/*`
- `components/board/` — `BoardComponent` renders 9×9 grid using `@for` control flow
- `components/cell/` — `CellComponent` handles keyboard input (digits 1-9, Backspace/Delete)
- `components/control-bar/` — `ControlBarComponent` with difficulty select, buttons, client-side timer
- `app.component.ts` — root, manages state: `board`, `selectedCell`, `hintedCell`
- `proxy.conf.json` — dev-server proxy routes `/api` → `http://localhost:8080`

Maven build: `frontend-maven-plugin` downloads Node, runs `npm install` + `ng build`, then `maven-resources-plugin` copies `dist/sudoku-angular/browser/` → `sudoku-web/src/main/resources/static/`.

## Key Pitfalls

- **Timer thread-safety**: `TIMER_TICK` fires from a `ScheduledExecutorService` thread. Always wrap UI updates in `SwingUtilities.invokeLater` / `Platform.runLater`.
- **`SudokuBoard.copy()`**: Solver/generator always work on copies; never pass the live board.
- **Conflict detection** tracks duplicates in row/col/box — NOT wrong-vs-solution. `GameState` has a lazily-computed `cachedSolution` for mistake counting.
- **Generator test speed**: Tests use seeded `Random(42)` and EASY difficulty only — MEDIUM/HARD uniqueness checks are too slow for CI.
- **Solver test speed**: `detectsUnsolvableBoard` must use a near-complete board (only 1 empty cell with 0 candidates) — a sparse invalid board causes exhaustive search and hangs. `countSolutions` tests likewise use near-complete boards or boards with known quick ambiguity.
- **`TimerLabel.gameState`** is non-final (needed for `rebind()`) — the `final` modifier would cause a compile error.
- **Angular timer**: `GameSession` never calls `startTimer()` — the web UI manages its own `setInterval` timer. Calling it would leak daemon threads per session.
- **Angular build output**: `ng build` emits to `dist/sudoku-angular/browser/` (not `dist/sudoku-angular/`). The pom copies from `browser/`.
- **Angular Node version**: Angular 21 requires Node ≥ 22.x. Use Node 22.15.0 via nvm. Also requires TypeScript ≥ 5.9 (published as 5.9.3 — do not use TS 5.7/5.8 with Angular 21).
- **CORS**: `WebConfig` allows `http://localhost:4200` with credentials. In production the SPA is served from the Spring Boot static resources, so no CORS needed.

## Basic principles
- When working on a new feature/fix
  - create a new branch from `main`
  - always compile and test after an implementation task
  - when done commit with descriptive commit message and push and open a PR
- tools
  - try using latest stable libs and tools (LTS)
  - update tools when needed in D:\tools\ and make them available in the PATH
  - use WSL with Ubuntu 22.04 LTS for Windows development with intelliJ for better integration
  - VS Code is awful, use IntelliJ. Nevertheless, give me insights on advantages of VS Code
- always give feedback, advice on best practices and ask questions
- always update the README.md and CLAUDE.md
