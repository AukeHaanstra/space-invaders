package nl.pancompany.spaceinvaders.laserbeam.shooter;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.*;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.Count;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.*;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.shared.EntityTags.LASER;

@RequiredArgsConstructor
public class LaserBeamShooter {

    private final EventStore eventStore;
    private final Count count = Count.times(3); // Execute command every 3 frames

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
//        if (count.finished()) {
            COMMAND_EXECUTOR.accept(() -> decide(new ShootLaserBeam()));
//        }
    }

    private void decide(ShootLaserBeam shootLaserBeam) {
        Tag laserTag = Tag.of(LASER_ENTITY);
        Tag alienTag = Tag.of(ALIEN_ENTITY);

        Types eventTypes = Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                SpriteDestroyed.class, SpriteRespawned.class);
        StateManager<LaserState> stateManager = eventStore.loadState(LaserState.class,
                Query.or(
                        QueryItem.of(laserTag, eventTypes),
                        QueryItem.of(alienTag, eventTypes)
                ));

        LaserState laserState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Bombs cannot be dropped before being created."));

        List<Event> eventsToPublish = new ArrayList<>();

        if (laserState.laserId == null) {
            return; // Player has not shot a laser beam yet.
        }

        SpriteState laser = laserState.sprites.get(laserState.laserId);
        Tag spriteTagLaser = LASER;

        if (laser != null && laser.isVisible()) {

            int shotX = laser.getX();
            int shotY = laser.getY();

            for (SpriteId alienId : laserState.alienIds) {
                SpriteState alien = laserState.sprites.get(alienId);
                Tag spriteTagAlien = Tag.of(SPRITE_ENTITY, alienId.toString());

                int alienX = alien.getX();
                int alienY = alien.getY();

                if (alien.isVisible() && laser.isVisible()) {
                    if (shotX >= (alienX)
                            && shotX <= (alienX + Constants.ALIEN_WIDTH)
                            && shotY >= (alienY)
                            && shotY <= (alienY + Constants.ALIEN_HEIGHT)) {

                        eventsToPublish.add(Event.of(new SpriteImageChanged(alien.spriteId, ALIEN_EXPLOSION_IMAGE_PATH), // make simple automation to trigger ChangeSpriteImage upon SpriteExplosionTriggered
                                Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                        eventsToPublish.add(Event.of(new SpriteExplosionTriggered(alien.spriteId),
                                Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                        eventsToPublish.add(Event.of(new OneAlienDown(alien.spriteId),
                                Tags.and(spriteTagAlien, alienTag, EntityTags.GAME)));
                        eventsToPublish.add(Event.of(new SpriteDestroyed(laser.spriteId),
                                Tags.and(spriteTagLaser, laserTag, EntityTags.GAME)));
                    }
                }
            }

            int y = laser.getY();
            int newY = y - laser.speed;

            if (newY < 0) {
                eventsToPublish.add(Event.of(new SpriteDestroyed(laser.spriteId),
                        Tags.and(spriteTagLaser, laserTag, EntityTags.GAME)));
            } else {
                eventsToPublish.add(Event.of(new SpriteMoved(laser.spriteId, laser.x, newY),
                        Tags.and(spriteTagLaser, laserTag, EntityTags.GAME)));
            }
        }

        stateManager.apply(eventsToPublish);
    }

    @Builder
    @Data
    private static class SpriteState {
        SpriteId spriteId;
        int x;
        int y;
        Direction direction;
        boolean visible;
        int speed;
    }

    private static class LaserState {

        List<SpriteId> alienIds = new ArrayList<>();
        SpriteId laserId;
        Map<SpriteId, SpriteState> sprites = new HashMap<>();

        @EventSourced // no @StateCreator, because then all but the first SpriteCreated event would be ignored!
        void evolve(SpriteCreated spriteCreated) {
            SpriteState spriteState = SpriteState.builder()
                    .spriteId(spriteCreated.id())
                    .x(spriteCreated.startX())
                    .y(spriteCreated.startY())
                    .direction(spriteCreated.direction())
                    .speed(spriteCreated.speed())
                    .visible(true)
                    .build();
            switch (spriteCreated.entityName()) {
                case ALIEN_ENTITY -> {
                    alienIds.add(spriteCreated.id());
                    sprites.put(spriteCreated.id(), spriteState);
                }
                case LASER_ENTITY -> {
                    laserId = spriteCreated.id();
                    sprites.put(spriteCreated.id(), spriteState);
                }
                default -> throw new IllegalStateException("Unexpected sprite entity: " + spriteCreated.entityName());
            }
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            sprites.get(spriteTurned.id()).setDirection(spriteTurned.direction());
        }

        @EventSourced
        void evolve(SpriteMoved spriteMoved) {
            SpriteState spriteState = sprites.get(spriteMoved.id());
            spriteState.setX(spriteMoved.newX());
            spriteState.setY(spriteMoved.newY());
        }

        @EventSourced
        void evolve(SpriteDestroyed spriteDestroyed) {
            sprites.get(spriteDestroyed.id()).setVisible(false);
        }

        @EventSourced
        void evolve(SpriteRespawned spriteRespawned) {
            SpriteState spriteState = sprites.get(spriteRespawned.id());
            spriteState.setVisible(true);
            spriteState.setX(spriteRespawned.startX());
            spriteState.setY(spriteRespawned.startY());
        }
    }
}
