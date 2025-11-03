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

@RequiredArgsConstructor
public class InitiateGameCycleCommandHandler {
    private final EventStore eventStore;

    public void decide(InitiateGameCycle initiateGameCycle) {
        StateManager<GameState> stateManager = eventStore.loadState(GameState.class,
                Query.of(EntityTags.GAME, Type.of(GameCreated.class))); // Not GameCycleInitiated.class, to save resources
        stateManager.getState().orElseThrow(() -> new IllegalStateException(
                "Game cannot be initiated before being created."));
        stateManager.apply(new GameCycleInitiated(), EntityTags.GAME);
    }

    private static class GameState {

        @StateCreator
        GameState(GameCreated gameCreated) {
        }

        // Not @EventSourced, to save resources
        void evolve(GameCycleInitiated gameCycleInitiated) {
        }

    }

}
