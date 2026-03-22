package horst.haering.de.core.event;

public final class GameEvent {

    public enum Type {
        CELL_CHANGED,
        CONFLICT_DETECTED,
        CONFLICT_RESOLVED,
        HINT_APPLIED,
        PUZZLE_COMPLETE,
        TIMER_TICK,
        MISTAKE_INCREMENTED
    }

    private final Type type;
    private final int row;
    private final int col;

    /** General event not tied to a specific cell. */
    public GameEvent(Type type) {
        this(type, -1, -1);
    }

    /** Cell-specific event. */
    public GameEvent(Type type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public Type getType() { return type; }

    /** Row of the affected cell, or -1 if not cell-specific. */
    public int getRow() { return row; }

    /** Column of the affected cell, or -1 if not cell-specific. */
    public int getCol() { return col; }

    @Override
    public String toString() {
        return "GameEvent{" + type + (row >= 0 ? " [" + row + "," + col + "]" : "") + "}";
    }
}
