package io.bino.core.event;

@FunctionalInterface
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}
