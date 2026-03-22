# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An interactive Sudoku game in Java 26, structured as a Maven multi-module project with two parallel UIs (Swing and JavaFX) sharing a common core engine.

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
```

## Module Layout

```
sudoku/
├── pom.xml             ← parent POM: Java 26, junit 5.12.1, javafx 24.0.1
├── sudoku-core/        ← game logic (no UI deps)
├── sudoku-swing/       ← Swing UI, depends on sudoku-core
└── sudoku-javafx/      ← JavaFX UI, depends on sudoku-core
```

## Architecture

### sudoku-core

All logic is in `src/main/java/horst/haering/de/core/`:

- **`model/SudokuBoard`** — 9×9 grid, given vs. user cells, observable via `GameEventListener`
- **`model/GameState`** — central hub: board + timer + mistakes + completion; fires `GameEvent`s to listeners; call `fireEvent()` to force a UI refresh
- **`logic/SudokuValidator`** — stateless; `findConflicts()` returns `Set<String>` of `"row,col"` keys
- **`logic/SudokuSolver`** — backtracking + MRV heuristic; `solve(board)` mutates in-place; `findHint()` returns naked/hidden singles
- **`logic/SudokuGenerator`** — fill board → remove cells with uniqueness check; seeded `Random` for deterministic tests
- **`event/GameEvent.Type`** — `CELL_CHANGED, CONFLICT_DETECTED, CONFLICT_RESOLVED, HINT_APPLIED, PUZZLE_COMPLETE, TIMER_TICK, MISTAKE_INCREMENTED`

`GameState` forwards board-level `CELL_CHANGED` events to its own listener list. Timer fires `TIMER_TICK` from a background thread — UI listeners must dispatch via `SwingUtilities.invokeLater` / `Platform.runLater`.

### sudoku-swing

`src/main/java/horst/haering/de/swing/`:
- `MainFrame` — JFrame, wires `BoardPanel` + `ControlPanel` to `GameState`
- `board/BoardPanel` — 9×9 `GridLayout` of `SudokuCellPanel`; `paintComponent` draws thick 3×3 box borders
- `board/SudokuCellPanel` — JTextField with `DocumentFilter` (accepts only digits 1-9); color states via `setBackground()`
- `control/ControlPanel` + `control/TimerLabel` — buttons, difficulty combo, timer, mistake counter

### sudoku-javafx

`src/main/java/horst/haering/de/fx/`:
- `SudokuFxApp` — `extends Application`; creates `GameState`, wires `BoardGrid` + `ControlBar`
- `component/BoardGrid` — `GridPane` 9×9; CSS classes `cell-given/cell-user/cell-conflict/cell-selected/cell-hint`; box borders via `box-border-right/bottom/corner` CSS classes
- `component/ControlBar` — `VBox` with buttons, difficulty `ComboBox`, timer, mistake label

CSS: `src/main/resources/horst/haering/de/fx/sudoku.css`

No `module-info.java` — `javafx-maven-plugin` handles module path automatically.

## Key Pitfalls

- **Timer thread-safety**: `TIMER_TICK` fires from a `ScheduledExecutorService` thread. Always wrap UI updates in `SwingUtilities.invokeLater` / `Platform.runLater`.
- **`SudokuBoard.copy()`**: Solver/generator always work on copies; never pass the live board.
- **Conflict detection** tracks duplicates in row/col/box — NOT wrong-vs-solution. `GameState` has a lazily-computed `cachedSolution` for mistake counting.
- **Generator test speed**: Tests use seeded `Random(42)` and EASY difficulty only — MEDIUM/HARD uniqueness checks are too slow for CI.
- **Solver test speed**: `detectsUnsolvableBoard` must use a near-complete board (only 1 empty cell with 0 candidates) — a sparse invalid board causes exhaustive search and hangs. `countSolutions` tests likewise use near-complete boards or boards with known quick ambiguity.
- **`TimerLabel.gameState`** is non-final (needed for `rebind()`) — the `final` modifier would cause a compile error.
