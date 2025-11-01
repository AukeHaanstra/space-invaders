package nl.pancompany.spaceinvaders.game.initiatecycle;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameCycleInitiated;

import java.time.LocalTime;

@RequiredArgsConstructor
public class InitiateGameCycleCommandHandler {
    private final EventStore eventStore;

    public void decide(InitiateGameCycle initiateGameCycle) {
        StateManager<GameState> stateManager = eventStore.loadState(GameState.class,
                Query.of(EntityTags.GAME, Type.of(GameCreated.class)));
        GameState gameState = stateManager.getState().orElseThrow(() -> new IllegalStateException(
                "Game must be created before initiating a game cycle."));
        stateManager.apply(new GameCycleInitiated(), EntityTags.GAME);
    }

    private static class GameState {

        @StateCreator
        GameState(GameCreated gameCreated) {
        }

        @EventSourced
        void evolve(GameCycleInitiated gameCycleInitiated) {
        }

    }

}
