package nl.pancompany.spaceinvaders.player.mover;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.Constants;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCycleInitiated;
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.events.PlayerMoved;
import nl.pancompany.spaceinvaders.events.PlayerTurned;
import nl.pancompany.spaceinvaders.events.SpriteTurned.TurnDirection;

import static nl.pancompany.spaceinvaders.Constants.*;

@RequiredArgsConstructor
public class PlayerMover {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
        decide(new MovePlayer());
    }

    private void decide(MovePlayer movePlayer) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Types.or(PlayerCreated.class, PlayerTurned.class, PlayerMoved.class)));
        PlayerState playerState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Player must be created before moving."));

        int dx = 0;

        if (playerState.turnDirection == TurnDirection.LEFT) {
            dx = -PLAYER_SPEED;
        } else if (playerState.turnDirection == TurnDirection.RIGHT) {
            dx = PLAYER_SPEED;
        } else {
            return;
        }

        int newX = playerState.x + dx;

        if (newX <= PLAYER_STOP_X_LEFT || newX >= PLAYER_STOP_X_RIGHT) {
            return; // don't move off the board
        }

        stateManager.apply(new PlayerMoved(newX, playerState.y), Tags.and(EntityTags.PLAYER, EntityTags.GAME));
    }

    private static class PlayerState {

        int x;
        int y;
        TurnDirection turnDirection;

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
            x = playerCreated.getStartX();
            y = playerCreated.getStartY();
        }

        @EventSourced
        void evolve(PlayerTurned playerTurned) {
            turnDirection = playerTurned.getTurnDirection();
        }

        @EventSourced
        void evolve(PlayerMoved playerMoved) {
            x = playerMoved.getNewX();
            y = playerMoved.getNewY();
        }
    }
}
