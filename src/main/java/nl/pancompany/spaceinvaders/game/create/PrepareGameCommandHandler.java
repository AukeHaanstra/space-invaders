package nl.pancompany.spaceinvaders.game.create;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;

import java.util.Optional;

@RequiredArgsConstructor
public class PrepareGameCommandHandler {

    private final EventStore eventStore;

    public void handle(CreateGame createGame) {
        StateManager<GameState> stateManager = eventStore.loadState(GameState.class,
                Query.of(EntityTags.GAME, Type.of(GameCreated.class)));
        Optional<GameState> gameState = stateManager.getState();
        if (gameState.isPresent()) {
            throw new IllegalStateException("Game cannot be created twice.");
        }
        stateManager.apply(new GameCreated(), EntityTags.GAME);
    }

    private static class GameState {

        @StateCreator
        GameState(GameCreated gameCreated) {
        }

    }
}
