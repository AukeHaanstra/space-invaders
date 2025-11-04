package nl.pancompany.spaceinvaders.game.stop;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;

@RequiredArgsConstructor
public class StopGameCommandHandler {

    private final EventStore eventStore;

    public void decide(StopGame stopGame) {
        StateManager<GameState> stateManager = eventStore.loadState(GameState.class,
                Query.of(EntityTags.GAME, Type.of(GameCreated.class).orType(GameStopped.class)));
        stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Game cannot be stoppped before being created."));
        stateManager.apply(new GameStopped(stopGame.message()), EntityTags.GAME);
    }

    private static class GameState {

        boolean inGame;
        String message;

        @StateCreator
        GameState(GameCreated gameCreated) {
            inGame = true;
        }

        @EventSourced
        void evolve(GameStopped gameStopped) {
            inGame = false;
            message = gameStopped.message();
        }

    }
}
