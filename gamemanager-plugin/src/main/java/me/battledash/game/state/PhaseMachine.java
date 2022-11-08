package me.battledash.game.state;

import me.battledash.game.game.Game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhaseMachine extends GamePhase {

    private final List<BasePhase> phases = new ArrayList<>();
    private int current = 0;
    private boolean skipping = false;
    private final long interval;

    public PhaseMachine(Game game, long interval, List<BasePhase> phases) {
        super(game);
        this.interval = interval;
        this.phases.addAll(phases);
    }

    public PhaseMachine(Game game, long interval, BasePhase... phases) {
        this(game, interval, List.of(phases));
    }

    public PhaseMachine(Game game, BasePhase... phases) {
        this(game, 1, phases);
    }

    public void add(BasePhase state) {
        phases.add(state);
    }

    public void addNext(BasePhase state) {
        phases.add(current + 1, state);
    }

    public void skip() {
        skipping = true;
    }

    @Override
    public void onStart() {
        if (phases.isEmpty()) {
            end();
            return;
        }

        phases.get(current).start();

        scheduleRepeating(this::update, 0L, interval);
    }

    @Override
    public void onUpdate() {
        BasePhase state = phases.get(current);

        state.update();
        if ((state.canEnd() && !state.isFrozen()) || skipping) {
            if (skipping) {
                skipping = false;
            }

            state.end();
            ++current;

            if (current >= phases.size()) {
                end();
                return;
            }

            phases.get(current).start();
        }
    }

    @Override
    public boolean canEnd() {
        return current == phases.size() - 1 && phases.get(current).canEnd();
    }

    @Override
    public void onEnd() {
        if (current < phases.size()) {
            phases.get(current).end();
        }
    }

    @Override
    public Duration getDuration() {
        return phases.stream().map(BasePhase::getDuration).reduce(Duration.ZERO, Duration::plus);
    }
}
