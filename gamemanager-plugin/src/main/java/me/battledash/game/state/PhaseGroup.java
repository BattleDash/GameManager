package me.battledash.game.state;

import me.battledash.game.game.Game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhaseGroup extends GamePhase {

    private final List<BasePhase> phases = new ArrayList<>();

    public PhaseGroup(Game game, BasePhase... phases) {
        super(game);
        this.phases.addAll(List.of(phases));
    }

    @Override
    protected void onStart() {
        for (BasePhase phase : phases) {
            phase.start();
        }
    }

    @Override
    public void onUpdate() {
        for (BasePhase phase : phases) {
            phase.update();
        }

        if (phases.stream().allMatch(BasePhase::isEnded)) {
            end();
        }
    }

    @Override
    public boolean canEnd() {
        return phases.stream().allMatch(BasePhase::canEnd);
    }

    @Override
    public Duration getDuration() {
        Duration maxDuration = Duration.ZERO;
        for (BasePhase phase : phases) {
            if (phase.getDuration().toMillis() > maxDuration.toMillis()) {
                maxDuration = phase.getDuration();
            }
        }
        return maxDuration;
    }

}
