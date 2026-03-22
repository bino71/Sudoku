# Sudoku

An interactive Sudoku game implemented in Java, featuring two parallel UIs — **Swing** and **JavaFX** — built from the same core engine. The project serves as a direct hands-on comparison of the two UI toolkits available in the standard Java platform.

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
└── sudoku-javafx/     ← interactive UI built with JavaFX
```

`sudoku-core` has no UI dependencies. Both UI modules depend only on `sudoku-core`.

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 26 |
| Maven | 3.9+ |

Java download: [Adoptium Temurin](https://adoptium.net/temurin/releases/) or [OpenJDK](https://jdk.java.net/)

Maven download: [maven.apache.org](https://maven.apache.org/download.cgi)

## Build & Run

```bash
# Build all modules and run tests
mvn clean install

# Run the Swing UI
mvn -pl sudoku-swing exec:java

# Run the JavaFX UI
mvn -pl sudoku-javafx javafx:run

# Run core unit tests only
mvn -pl sudoku-core test

# Run tests with HTML report
mvn -pl sudoku-core surefire-report:report
# Report: sudoku-core/target/site/surefire-report.html
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

The project follows a strict MVC separation:

- **Model** (`sudoku-core`) — `SudokuBoard`, `GameState`, `SudokuValidator`, `SudokuSolver`, `SudokuGenerator`
- **View + Controller** (`sudoku-swing`, `sudoku-javafx`) — each implements `GameEventListener` to react to state changes

`GameState` fires `GameEvent`s (cell changed, conflict detected, hint applied, puzzle complete, timer tick) to registered listeners. The two UIs wire themselves up as listeners and update their components accordingly — `SwingUtilities.invokeLater` for Swing, `Platform.runLater` for JavaFX.

The solver uses a **backtracking + MRV (Minimum Remaining Values)** heuristic: always fill the empty cell with the fewest legal candidates first. This makes puzzle generation fast enough for real-time use.

## Java UI Toolkit Comparison

This project intentionally implements the same application twice to highlight differences between the two built-in Java UI toolkits:

| | Swing | JavaFX |
|---|---|---|
| **Package** | `javax.swing` (in JDK) | `javafx.*` (separate module since JDK 11) |
| **Styling** | Per-component Java API (`setBackground`, `setFont`) | CSS stylesheets (`-fx-background-color`, etc.) |
| **Layout** | `LayoutManager` implementations (`GridLayout`, `BorderLayout`) | Scene graph nodes (`GridPane`, `BorderPane`, `VBox`) |
| **Animation** | `javax.swing.Timer` + manual color interpolation | `Timeline` + `KeyValue` / `DropShadow` effect |
| **Threading** | `SwingUtilities.invokeLater` | `Platform.runLater` |
| **Custom painting** | Override `paintComponent(Graphics)` | CSS or effect API — no painting needed |
| **FXML** | — | Declarative UI via FXML + controller |

> **Other Java UI options:** AWT (the grandfather Swing is built on — rarely used directly), and SWT (Eclipse's native-widget toolkit — not part of the JDK, separate dependency). For most desktop apps, Swing and JavaFX are the practical choices.

## Project History

Migrated from a legacy Eclipse/Java stub project (`BasicSwingComponents`, `SudokuTableModel`) with no working game logic. The salvaged piece is the `field2` puzzle from the original prototype (included as a built-in sample after fixing a duplicate-digit bug in row 9).
