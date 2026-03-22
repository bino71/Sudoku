# Sudoku

An interactive Sudoku game implemented in Java, featuring four parallel UI implementations — **Swing**, **JavaFX**, **Spring Boot/Thymeleaf** (server-side rendered), and **Angular 21** (SPA) — all built from the same core engine. The project serves as a hands-on comparison of different UI approaches available on the Java platform and beyond.

## Features

- **Puzzle generator** — generates fresh puzzles at three difficulty levels (Easy / Medium / Hard) with a guaranteed unique solution
- **Backtracking solver** — fully solves any valid puzzle; also powers the hint system
- **Hint system** — reveals a single cell using naked-single and hidden-single constraint propagation (shows *why* the value is correct)
- **Real-time conflict highlighting** — instantly flags duplicate values in rows, columns, and 3×3 boxes
- **Mistake counter** — tracks entries that conflict with the correct solution
- **Timer** — records elapsed time per game; pauses on completion

## Module Layout

```
sudoku/
├── pom.xml            ← parent POM (Java 26, Maven 3.9+)
├── sudoku-core/       ← game logic: model, validator, solver, generator
├── sudoku-swing/      ← interactive UI built with Java Swing
├── sudoku-javafx/     ← interactive UI built with JavaFX
├── sudoku-web/        ← Spring Boot 3.4 + Thymeleaf SSR + REST API
└── sudoku-angular/    ← Angular 21 SPA (consumes sudoku-web REST API)
```

`sudoku-core` has no UI dependencies. All UI modules depend only on `sudoku-core`.

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 26 |
| Maven | 3.9+ |
| Node.js | 22.15+ (for Angular dev server) |

Java download: [Adoptium Temurin](https://adoptium.net/temurin/releases/) or [OpenJDK](https://jdk.java.net/)

Maven download: [maven.apache.org](https://maven.apache.org/download.cgi)

## Build & Run

```bash
# Build all modules (includes Angular build + tests)
mvn clean install

# Run core unit tests only
mvn -pl sudoku-core test

# Run tests with HTML report
mvn -pl sudoku-core surefire-report:report
# Report: sudoku-core/target/site/surefire-report.html
```

### Desktop UIs

```bash
# Swing
mvn -pl sudoku-swing exec:java

# JavaFX
mvn -pl sudoku-javafx javafx:run
```

### Web UI

```bash
# Start the Spring Boot server (serves Thymeleaf + Angular SPA)
mvn -pl sudoku-web spring-boot:run

# Thymeleaf SSR:  http://localhost:8080/thymeleaf
# Angular SPA:    http://localhost:8080/
```

### Angular Development Server (live-reload)

```bash
# Requires sudoku-web running on :8080 for the REST API
cd sudoku-angular
npm install     # first time only
npm start       # dev server on http://localhost:4200
```

## Gameplay

1. Select a difficulty and click **New Game**
2. Click any empty cell and type a digit (1–9)
3. Use **arrow keys** to move between cells
4. Conflicting cells are highlighted in red immediately
5. Use **Hint** to reveal one logically deducible cell
6. Use **Solve** to auto-complete the puzzle
7. Use **Reset** to clear all your entries and start over
8. The game ends when all cells are filled correctly — your time and mistake count are shown

## Architecture

The project follows a strict separation of concerns:

- **Model** (`sudoku-core`) — `SudokuBoard`, `GameState`, `SudokuValidator`, `SudokuSolver`, `SudokuGenerator`
- **Desktop UIs** (`sudoku-swing`, `sudoku-javafx`) — implement `GameEventListener` to react to state changes
- **Web backend** (`sudoku-web`) — Spring Boot REST API + Thymeleaf; `GameState` is stored in HTTP session scope
- **Web frontend** (`sudoku-angular`) — Angular 21 SPA calling the REST API

`GameState` fires `GameEvent`s to registered listeners. Desktop UIs dispatch updates to their UI threads (`SwingUtilities.invokeLater` / `Platform.runLater`). The web UI manages its own client-side timer via `setInterval`.

The solver uses a **backtracking + MRV (Minimum Remaining Values)** heuristic: always fill the empty cell with the fewest legal candidates first.

## REST API (sudoku-web)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/game/new?difficulty=EASY` | Start new game |
| `GET` | `/api/game/state` | Get current board state |
| `POST` | `/api/game/enter` | Enter a value `{row, col, value}` |
| `POST` | `/api/game/hint` | Apply one hint cell |
| `POST` | `/api/game/solve` | Auto-solve the puzzle |
| `POST` | `/api/game/reset` | Clear all user entries |

All endpoints return a `BoardDto` with `values[][]`, `given[][]`, `conflicts[][]`, `complete`, `mistakeCount`, `difficulty`.

## UI Comparison

| | Swing | JavaFX | Thymeleaf | Angular |
|---|---|---|---|---|
| **Paradigm** | OOP component tree | Scene graph | Server-side rendering | SPA (client-side) |
| **Styling** | Java API (`setBackground`) | CSS (`-fx-background-color`) | CSS | SCSS component styles |
| **Layout** | `LayoutManager` | `GridPane`, `VBox` | HTML/CSS grid | Angular `GridPane`, flex |
| **State** | Objects in JVM | Objects in JVM | HTTP session | Component/service state |
| **Updates** | `invokeLater` | `Platform.runLater` | Full page reload / fetch | Reactive bindings |
| **Animation** | `javax.swing.Timer` | `Timeline` + effects | CSS animations | CSS `@keyframes` |
| **Routing** | — | — | Spring MVC | None (single page) |

> **Other Java UI options:** AWT (rarely used directly), SWT (Eclipse's native-widget toolkit). For most modern Java desktop apps, JavaFX is preferred over Swing. For web, Spring Boot + a JS frontend is the dominant pattern.

## Project History

Migrated from a legacy Eclipse/Java stub project (`BasicSwingComponents`, `SudokuTableModel`) with no working game logic. The salvaged piece is the `field2` puzzle from the original prototype (included as a built-in sample after fixing a duplicate-digit bug in row 9).
