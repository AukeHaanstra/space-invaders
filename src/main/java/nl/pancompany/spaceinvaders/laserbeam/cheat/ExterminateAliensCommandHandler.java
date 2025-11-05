package nl.pancompany.spaceinvaders.laserbeam.cheat;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.*;
import nl.pancompany.spaceinvaders.alien.mover.AliensMover;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.pancompany.spaceinvaders.shared.Constants.*;

@RequiredArgsConstructor
public class ExterminateAliensCommandHandler {

    private final EventStore eventStore;

    public void decide(ExterminateAliens exterminateAliens) {
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        StateManager<AliensState> stateManager = eventStore.loadState(
                AliensState.class,
                Query.fromItems(QueryItems.or(
                        QueryItem.of(EntityTags.GAME, Type.of(GameCreated.class)),
                        QueryItem.of(alienTag, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                                SpriteDestroyed.class))
                )));
        AliensState aliensState = stateManager.getState().orElseThrow(() -> new IllegalStateException("Cannot cheat before game being created."));

        List<Event> eventsToPublish = new ArrayList<>();

        for (AlienState alienState : aliensState.aliens.values()) {
            if (alienState.visible) {
                Tag spriteTagAlien = Tag.of(SPRITE_ENTITY, alienState.spriteId.toString());
                eventsToPublish.add(Event.of(new SpriteImageChanged(alienState.spriteId, ALIEN_EXPLOSION_IMAGE_PATH), // make simple automation to trigger ChangeSpriteImage upon SpriteExplosionTriggered
                        Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                eventsToPublish.add(Event.of(new SpriteExplosionTriggered(alienState.spriteId),
                        Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                eventsToPublish.add(Event.of(new OneAlienDown(alienState.spriteId),
                        Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
            }
        }

        stateManager.apply(eventsToPublish);
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

        @StateCreator
        AliensState(GameCreated gameCreated) {
        }

        @EventSourced
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
        void evolve(SpriteDestroyed spriteDestroyed) {
            aliens.get(spriteDestroyed.id()).setVisible(false);
        }

    }




}
