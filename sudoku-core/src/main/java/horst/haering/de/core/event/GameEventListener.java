package horst.haering.de.core.event;

@FunctionalInterface
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}
