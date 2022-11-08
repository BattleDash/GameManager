package me.battledash.game.state;

import lombok.Getter;
import lombok.Setter;
import me.battledash.game.GameManager;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * The base phase for the state machine
 * Extend this if you don't need game/bukkit-specific functionality
 */
@Getter
public abstract class BasePhase {

    private final Logger logger = GameManager.getInstance().getLogger();
    private boolean started = false;
    private boolean ended = false;
    private boolean updating = false;

    @Setter
    private boolean frozen = false;

    private Instant startTime;

    protected void onStart() {}

    public void start() {
        synchronized (this) {
            if (started || ended) {
                return;
            }
            started = true;
        }

        startTime = Instant.now();

        try {
            onStart();
        } catch (Throwable e) {
            logger.severe("Exception during " + getClass().getSimpleName() + " start");
            e.printStackTrace();
        }
    }

    public void onUpdate() {}

    public void update() {
        synchronized (this) {
            if (!started || ended || updating) {
                return;
            }
            updating = true;
        }

        if (canEnd() && !frozen) {
            end();
            return;
        }

        try {
            onUpdate();
        } catch (Throwable e) {
            logger.severe("Exception during " + getClass().getSimpleName() + " start");
            e.printStackTrace();
        }

        updating = false;
    }

    protected void onEnd() {}

    /**
     * End the state
     */
    public void end() {
        synchronized (this) {
            if (!started || ended) {
                return;
            }
            ended = true;
        }

        try {
            onEnd();
        } catch (Throwable e) {
            logger.severe("Exception during " + getClass().getSimpleName() + " end");
            e.printStackTrace();
        }
    }

    /**
     * If true, the state will {@link BasePhase#end()} on the
     * next {@link BasePhase#update()}
     *
     * @return True if the phase is in a state where it can end
     */
    public boolean canEnd() {
        return ended || getRemainingDuration() == Duration.ZERO;
    }

    /**
     * The duration of the phase, this is used to  {@link BasePhase#canEnd()}
     *
     * @return The duration of the phase
     */
    public Duration getDuration() {
        return Duration.ZERO;
    }

    public Duration getRemainingDuration() {
        Duration sinceStart = Duration.between(startTime, Instant.now());
        Duration remaining = getDuration().minus(sinceStart);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

}
