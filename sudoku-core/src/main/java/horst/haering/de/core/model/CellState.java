package horst.haering.de.core.model;

public enum CellState {
    /** Pre-filled clue cell — not editable by the player. */
    GIVEN,
    /** Editable cell with no value entered yet. */
    USER_EMPTY,
    /** Editable cell with a value entered by the player. */
    USER_FILLED
}
