package io.bino.core.model;

import io.bino.core.event.GameEvent;
import io.bino.core.event.GameEventListener;
import io.bino.core.logic.SudokuSolver;
import io.bino.core.logic.SudokuValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central game state. Owns the board, timer, mistake counter, and completion status.
 * Fires {@link GameEvent}s to registered listeners on all state changes.
 *
 * <p>Events are fired from the calling thread (EDT / FX thread) for cell changes,
 * but TIMER_TICK events originate from a background scheduler thread.
 * UI listeners must dispatch timer updates to their UI thread accordingly.
 */
public class GameState {

    private final SudokuBoard board;
    private final Difficulty difficulty;
    private final SudokuValidator validator = new SudokuValidator();
    private final SudokuSolver solver = new SudokuSolver();

    private final List<GameEventListener> listeners = new ArrayList<>();

    private int mistakeCount = 0;
    private boolean complete = false;
    private boolean hadConflicts = false;

    /** Lazily computed solution as flat array [row*9+col] — used for hints and mistake detection. */
    private int[] cachedSolution = null;

    // Timer state
    private final AtomicLong elapsedSeconds = new AtomicLong(0);
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sudoku-timer");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> timerTask;

    public GameState(SudokuBoard puzzle, Difficulty difficulty) {
        this.board = puzzle;
        this.difficulty = difficulty;
        // Forward board-level CELL_CHANGED events to GameState listeners
        this.board.addGameEventListener(this::fireEvent);
    }

    public SudokuBoard getBoard() {
        return board;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Records a player's cell entry, validates conflicts, updates mistake count,
     * and fires appropriate events. Pass 0 to clear a cell.
     */
    public void enterValue(int row, int col, int value) {
        board.setValue(row, col, value);  // fires CELL_CHANGED via board

        Set<String> conflicts = validator.findConflicts(board);
        boolean nowHasConflicts = !conflicts.isEmpty();

        if (value != 0 && nowHasConflicts) {
            // Check if this specific entry conflicts with the solution
            int[] solution = getSolution();
            if (solution != null && solution[row * 9 + col] != value) {
                mistakeCount++;
                fireEvent(new GameEvent(GameEvent.Type.MISTAKE_INCREMENTED, row, col));
            }
            if (!hadConflicts) {
                fireEvent(new GameEvent(GameEvent.Type.CONFLICT_DETECTED, row, col));
            }
        } else if (hadConflicts && !nowHasConflicts) {
            fireEvent(new GameEvent(GameEvent.Type.CONFLICT_RESOLVED, row, col));
        }
        hadConflicts = nowHasConflicts;

        if (!complete && board.isFilled() && conflicts.isEmpty()) {
            complete = true;
            pauseTimer();
            fireEvent(new GameEvent(GameEvent.Type.PUZZLE_COMPLETE));
        }
    }

    /** Applies a single hint cell using the solver's naked/hidden single logic. */
    public void applyHint() {
        SudokuBoard workCopy = board.copy();
        Optional<SudokuSolver.HintCell> hint = solver.findHint(workCopy);
        if (hint.isPresent()) {
            SudokuSolver.HintCell h = hint.get();
            enterValue(h.row(), h.col(), h.value());
            fireEvent(new GameEvent(GameEvent.Type.HINT_APPLIED, h.row(), h.col()));
        }
    }

    public int getMistakeCount() {
        return mistakeCount;
    }

    public boolean isComplete() {
        return complete;
    }

    // --- Timer ---

    public void startTimer() {
        if (timerTask != null && !timerTask.isDone()) return;
        timerTask = scheduler.scheduleAtFixedRate(() -> {
            elapsedSeconds.incrementAndGet();
            fireEvent(new GameEvent(GameEvent.Type.TIMER_TICK));
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void pauseTimer() {
        if (timerTask != null) {
            timerTask.cancel(false);
        }
    }

    public long getElapsedSeconds() {
        return elapsedSeconds.get();
    }

    public void resetTimer() {
        pauseTimer();
        elapsedSeconds.set(0);
    }

    // --- Listeners ---

    public void addGameEventListener(GameEventListener l) {
        listeners.add(l);
    }

    public void removeGameEventListener(GameEventListener l) {
        listeners.remove(l);
    }

    public void fireEvent(GameEvent event) {
        for (GameEventListener l : new ArrayList<>(listeners)) {
            l.onGameEvent(event);
        }
    }

    // --- Private helpers ---

    /**
     * Lazily solves the puzzle to get the canonical solution array.
     * Returns null if puzzle is unsolvable (shouldn't happen with generated puzzles).
     */
    private int[] getSolution() {
        if (cachedSolution == null) {
            SudokuBoard copy = board.copy();
            copy.clearUserCells();
            if (solver.solve(copy)) {
                int[][] arr = copy.toArray();
                cachedSolution = new int[81];
                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        cachedSolution[r * 9 + c] = arr[r][c];
                    }
                }
            }
        }
        return cachedSolution;
    }
}
