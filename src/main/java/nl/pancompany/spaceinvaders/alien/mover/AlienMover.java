package nl.pancompany.spaceinvaders.alien.mover;

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
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.*;

@RequiredArgsConstructor
public class AlienMover {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
        for (SpriteId alienSpriteId : ALIEN_SPRITE_IDS) {
            COMMAND_EXECUTOR.accept(() -> decide(new MoveAlien(alienSpriteId)));
        }
    }

    private void decide(MoveAlien moveAlien) {
        Tag spriteTagAlien = Tag.of(SPRITE_ENTITY, moveAlien.spriteId().toString());
        StateManager<AlienState> stateManager = eventStore.loadState(AlienState.class,
                Query.of(spriteTagAlien, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteRestsInPeace.class)));
        AlienState alienState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Alien cannot move before being created."));

        if (!alienState.visible) { // i.e. R.I.P.
            return;
        }

        int dx = switch (alienState.direction) {
            case LEFT -> -alienState.speed;
            case RIGHT -> alienState.speed;
            default -> throw new IllegalStateException("Illegal alien move direction: " + alienState.direction);
        };

        int newX = alienState.x + dx;

        stateManager.apply(new SpriteMoved(moveAlien.spriteId(), newX, alienState.y), Tags.and(spriteTagAlien, EntityTags.GAME));
    }

    private static class AlienState {

        int x;
        int y;
        Direction direction;
        boolean visible;
        int speed;

        @StateCreator
        AlienState(SpriteCreated spriteCreated) {
            x = spriteCreated.startX();
            y = spriteCreated.startY();
            direction = spriteCreated.direction();
            visible = true;
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            direction = spriteTurned.direction();
        }

        @EventSourced
        void evolve(SpriteMoved spriteMoved) {
            x = spriteMoved.newX();
            y = spriteMoved.newY();
        }

        @EventSourced
        void evolve(SpriteRestsInPeace spriteRestsInPeace) {
            visible = false;
        }
    }
}
