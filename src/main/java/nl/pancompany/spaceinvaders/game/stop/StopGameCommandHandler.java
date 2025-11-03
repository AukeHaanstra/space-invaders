package nl.pancompany.spaceinvaders.game.stop;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.game.create.CreateGame;

import java.util.Optional;

@RequiredArgsConstructor
public class StopGameCommandHandler {

    private final EventStore eventStore;

    public void decide(StopGame stopGame) {
        StateManager<GameState> stateManager = eventStore.loadState(GameState.class,
                Query.of(EntityTags.GAME, Type.of(GameCreated.class).orType(GameStopped.class)));
        GameState gameState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Game cannot be stoppped before being created."));
        if (!gameState.inGame) {
            throw new IllegalStateException("Game cannot be stoppped twice.");
        }
        stateManager.apply(new GameStopped(), EntityTags.GAME);
    }

    private static class GameState {

        boolean inGame;

        @StateCreator
        GameState(GameCreated gameCreated) {
            inGame = true;
        }

        @EventSourced
        void evolve(GameStopped gameStopped) {
            inGame = false;
        }

    }
}
