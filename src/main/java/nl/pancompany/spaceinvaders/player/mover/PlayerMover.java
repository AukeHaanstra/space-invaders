package nl.pancompany.spaceinvaders.player.mover;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.*;
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
                Query.of(EntityTags.PLAYER, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteStopped.class)));
        PlayerState playerState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Player cannot move before being created."));

        if (playerState.direction == NONE) {
            return;
        }

        int dx = switch (playerState.direction) {
            case LEFT -> -playerState.speed;
            case RIGHT -> playerState.speed;
            default -> throw new IllegalStateException("Illegal player move direction: " + playerState.direction);
        };

        int newX = playerState.x + dx;

        // TODO: Comment out to go beyond player boundaries (to check invasion or win game)
//        if (newX <= PLAYER_STOP_X_LEFT || newX >= PLAYER_STOP_X_RIGHT) {
//            return; // don't move off the board
//        }

        stateManager.apply(new SpriteMoved(PLAYER_SPRITE_ID, newX, playerState.y), Tags.and(EntityTags.PLAYER, Tag.of(PLAYER_ENTITY), EntityTags.GAME));
    }

    private static class PlayerState {

        int x;
        int y;
        Direction direction;
        int speed;

        @StateCreator
        PlayerState(SpriteCreated spriteCreated) {
            x = spriteCreated.startX();
            y = spriteCreated.startY();
            direction = spriteCreated.direction();
            speed = spriteCreated.speed();
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            direction = spriteTurned.direction();
        }

        @EventSourced
        void evolve(SpriteStopped spriteStopped) {
            direction = NONE;
        }

        @EventSourced
        void evolve(SpriteMoved spriteMoved) {
            x = spriteMoved.newX();
            y = spriteMoved.newY();
        }
    }
}
