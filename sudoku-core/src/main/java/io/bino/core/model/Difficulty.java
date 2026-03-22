package io.bino.core.model;

public enum Difficulty {
    EASY(35),
    MEDIUM(28),
    HARD(22);

    /** Target number of given (pre-filled) cells in a generated puzzle. */
    private final int targetGivens;

    Difficulty(int targetGivens) {
        this.targetGivens = targetGivens;
    }

    public int getTargetGivens() {
        return targetGivens;
    }
}
