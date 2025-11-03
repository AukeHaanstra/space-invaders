package nl.pancompany.spaceinvaders.game.stopper;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.events.SpriteMoved;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.Constants.ALIEN_HEIGHT;
import static nl.pancompany.spaceinvaders.Constants.GROUND_Y;
import static nl.pancompany.spaceinvaders.shared.IdUtil.isAlien;

@RequiredArgsConstructor
public class GameStopper {

    private final EventStore eventStore;

    @EventHandler
    private void react(SpriteMoved spriteMoved) {
        if (isAlien(spriteMoved.id()) && spriteMoved.newY() + ALIEN_HEIGHT > GROUND_Y) { // If the bottom of the alien comes below the ground
            COMMAND_EXECUTOR.accept(() -> decide(new StopGame("Invasion!")));
        }
    }

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
