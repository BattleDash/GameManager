package me.battledash.game.game.phases;

import me.battledash.game.game.Game;
import me.battledash.game.state.GamePhase;
import me.battledash.game.state.PhaseMachine;

public class PregamePhase extends GamePhase {

    private final PhaseMachine machine;

    public PregamePhase(Game game, PhaseMachine machine) {
        super(game);
        this.machine = machine;
    }

    @Override
    protected void onEnd() {
    }

    @Override
    public boolean canEnd() {
        return !getGame().isAcceptingPlayers();
    }
}
