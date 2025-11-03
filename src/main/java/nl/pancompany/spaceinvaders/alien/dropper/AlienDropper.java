package nl.pancompany.spaceinvaders.alien.dropper;

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
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.ALIEN_SPRITE_IDS;
import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;
import static nl.pancompany.spaceinvaders.shared.IdUtil.isAlien;

@RequiredArgsConstructor
public class AlienDropper {

    private final EventStore eventStore;

    @EventHandler
    private void react(SpriteMoved spriteMoved) {
        if (!isAlien(spriteMoved.id())) {
            return;
        }

        Tag spriteTag = Tag.of(SPRITE_ENTITY, spriteMoved.id().toString());
        StateManager<AlienState> stateManager = eventStore.loadState(AlienState.class, // Live read model for this automation
                Query.of(spriteTag, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteRestsInPeace.class)));
        AlienState alienState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Alien cannot be lowered before being created."));

        if (!alienState.visible) { // i.e. R.I.P.
            return;
        }

        if (alienState.x + Constants.ALIEN_BORDER_RIGHT >= Constants.BOARD_WIDTH && alienState.direction != Direction.LEFT) {
            // If one alien gets too close to the right border, turn it left and lower it.
            for (SpriteId alienSpriteId : ALIEN_SPRITE_IDS) {
                COMMAND_EXECUTOR.accept(() -> decide(new DropAlien(alienSpriteId, Direction.LEFT)));
            }
        }
        if (alienState.x <= Constants.ALIEN_BORDER_LEFT && alienState.direction != Direction.RIGHT) {
            // If one alien gets too close to the left border, turn it right and lower it.
            for (SpriteId alienSpriteId : ALIEN_SPRITE_IDS) {
                COMMAND_EXECUTOR.accept(() -> decide(new DropAlien(alienSpriteId, Direction.RIGHT)));
            }
        }
    }

    private void decide(DropAlien dropAlien) {
        Tag spriteTagAlien = Tag.of(SPRITE_ENTITY, dropAlien.spriteId().toString());
        StateManager<AlienState> stateManager = eventStore.loadState(AlienState.class,
                Query.of(spriteTagAlien, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteRestsInPeace.class)));
        AlienState alienState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Alien cannot be lowered before being created."));

        if (!alienState.visible) { // i.e. R.I.P.
            return;
        }

        int newY = alienState.y + Constants.ALIEN_STEP_DOWN;

        stateManager.apply(new SpriteTurned(dropAlien.spriteId(), dropAlien.direction()),
                Tags.and(spriteTagAlien, EntityTags.GAME));
        stateManager.apply(new SpriteMoved(dropAlien.spriteId(), alienState.x, newY),
                Tags.and(spriteTagAlien, EntityTags.GAME));
    }

    private static class AlienState {

        int x;
        int y;
        Direction direction;
        boolean visible;

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
