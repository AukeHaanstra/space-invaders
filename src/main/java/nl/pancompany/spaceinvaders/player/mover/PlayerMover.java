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
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Direction.NONE;

@RequiredArgsConstructor
public class PlayerMover {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
        COMMAND_EXECUTOR.accept(() -> decide(new MovePlayer()));
    }

    private void decide(MovePlayer movePlayer) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Types.or(PlayerCreated.class, PlayerTurned.class, PlayerMoved.class,
                        PlayerStopped.class)));
        PlayerState playerState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Player must be created before moving."));

        if (playerState.direction == NONE) {
            return;
        }

        int dx = switch (playerState.direction) {
            case LEFT -> -PLAYER_SPEED;
            case RIGHT -> PLAYER_SPEED;
            default -> throw new IllegalStateException("Unknown move direction: " + playerState.direction);
        };

        int newX = playerState.x + dx;

        if (newX <= PLAYER_STOP_X_LEFT || newX >= PLAYER_STOP_X_RIGHT) {
            return; // don't move off the board
        }

        stateManager.apply(new PlayerMoved(newX, playerState.y), Tags.and(EntityTags.PLAYER, EntityTags.GAME));
    }

    private static class PlayerState {

        int x;
        int y;
        Direction direction;

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
            x = playerCreated.getStartX();
            y = playerCreated.getStartY();
            direction = playerCreated.getDirection();
        }

        @EventSourced
        void evolve(PlayerTurned playerTurned) {
            direction = playerTurned.getDirection();
        }

        @EventSourced
        void evolve(PlayerStopped playerStopped) {
            direction = NONE;
        }

        @EventSourced
        void evolve(PlayerMoved playerMoved) {
            x = playerMoved.getNewX();
            y = playerMoved.getNewY();
        }
    }
}
