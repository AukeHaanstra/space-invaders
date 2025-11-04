package nl.pancompany.spaceinvaders.alien.dropper;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.alien.mover.MoveAliens;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Constants.ALIEN_SPRITE_IDS;

@RequiredArgsConstructor
public class AliensDropper {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
        COMMAND_EXECUTOR.accept(() -> decide(new DropAliens()));
    }

    private void decide(DropAliens dropAliens) {
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        StateManager<AliensState> stateManager = eventStore.loadState(AliensState.class,
                Query.of(alienTag, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteRestsInPeace.class)));
        AliensState aliensState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Aliens cannot be dropped before being created."));

        List<Event> alienDrops = new ArrayList<>();

        for (AlienState alienState : aliensState.aliens.values()) {

            if (!alienState.visible) { // i.e. R.I.P.
                return;
            }

            if (alienState.x + Constants.ALIEN_BORDER_RIGHT >= Constants.BOARD_WIDTH && alienState.direction != Direction.LEFT) {
                // If one alien gets too close to the right border, turn it left and lower it.
                for (SpriteId alienSpriteId : ALIEN_SPRITE_IDS) {

                    Tag spriteTagAlien = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
                    int newY = alienState.y + Constants.ALIEN_STEP_DOWN;
                    Direction newDirection = Direction.LEFT;

                    alienDrops.add(Event.of(new SpriteTurned(alienSpriteId, newDirection),
                            Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                    alienDrops.add(Event.of(new SpriteMoved(alienSpriteId, alienState.x, newY),
                            Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                }
            }
            if (alienState.x <= Constants.ALIEN_BORDER_LEFT && alienState.direction != Direction.RIGHT) {
                // If one alien gets too close to the left border, turn it right and lower it.
                for (SpriteId alienSpriteId : ALIEN_SPRITE_IDS) {

                    Tag spriteTagAlien = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
                    int newY = alienState.y + Constants.ALIEN_STEP_DOWN;
                    Direction newDirection = Direction.RIGHT;

                    alienDrops.add(Event.of(new SpriteTurned(alienSpriteId, newDirection),
                            Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                    alienDrops.add(Event.of(new SpriteMoved(alienSpriteId, alienState.x, newY),
                            Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                }
            }
        }

        stateManager.apply(alienDrops);
    }

    @Builder
    @Data
    private static class AlienState {
        SpriteId spriteId;
        int x;
        int y;
        Direction direction;
        boolean visible;
        int speed;
    }

    private static class AliensState {

        Map<SpriteId, AlienState> aliens = new HashMap<>();

        @EventSourced // no @StateCreator, because then all but the first SpriteCreated event would be ignored!
        void evolve(SpriteCreated spriteCreated) {
            AlienState alienState = AlienState.builder()
                    .spriteId(spriteCreated.id())
                    .x(spriteCreated.startX())
                    .y(spriteCreated.startY())
                    .direction(spriteCreated.direction())
                    .speed(spriteCreated.speed())
                    .visible(true)
                    .build();
            aliens.put(spriteCreated.id(), alienState);
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            aliens.get(spriteTurned.id()).setDirection(spriteTurned.direction());
        }

        @EventSourced
        void evolve(SpriteMoved spriteMoved) {
            aliens.get(spriteMoved.id()).setX(spriteMoved.newX());
            aliens.get(spriteMoved.id()).setY(spriteMoved.newY());
        }

        @EventSourced
        void evolve(SpriteRestsInPeace spriteRestsInPeace) {
            aliens.get(spriteRestsInPeace.id()).setVisible(false);
        }
    }
}
