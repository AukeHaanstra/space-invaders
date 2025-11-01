package nl.pancompany.spaceinvaders.game.cycler;

import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.spaceinvaders.events.GameCycleInitiated;

public class GameCycler {

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
        // TODO



    }

    private void decide(FinishGameCycle completeGameCycle) {

    }
}
