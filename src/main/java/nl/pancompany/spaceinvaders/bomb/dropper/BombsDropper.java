package nl.pancompany.spaceinvaders.bomb.dropper;

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
import static nl.pancompany.spaceinvaders.shared.EntityTags.PLAYER;
import static nl.pancompany.spaceinvaders.shared.IdUtil.getBombId;

@RequiredArgsConstructor
public class BombsDropper {

    private final EventStore eventStore;
    private final Count count = Count.times(3); // Execute command every 3 frames

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
//        if (count.finished()) {
            COMMAND_EXECUTOR.accept(() -> decide(new DropBombs()));
//        }
    }

    private void decide(DropBombs dropBombs) {

        Tag bombTag = Tag.of(BOMB_ENTITY);
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        Tag playerTag = Tag.of(PLAYER_ENTITY);

        Types eventTypes = Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                SpriteDestroyed.class, SpriteRespawned.class);
        StateManager<BombState> stateManager = eventStore.loadState(BombState.class,
                Query.or(
                        QueryItem.of(bombTag, eventTypes),
                        QueryItem.of(alienTag, eventTypes),
                        QueryItem.of(playerTag, eventTypes)
                ));

        BombState bombState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Bombs cannot be dropped before being created."));

        List<Event> eventsToPublish = new ArrayList<>();

        var generator = new Random();

        SpriteState player = bombState.sprites.get(bombState.playerId);
        Tag spriteTagPlayer = PLAYER;

        for (SpriteId alienId : bombState.alienIds) {

            SpriteState alien = bombState.sprites.get(alienId);

            SpriteId bombId = getBombId(alienId);
            SpriteState bomb = bombState.sprites.get(bombId);
            Tag spriteTagBomb = Tag.of(SPRITE_ENTITY, bombId.toString());

            int shot = generator.nextInt(15);

            if (shot == Constants.CHANCE && alien.isVisible() && !bomb.isVisible()) {

                eventsToPublish.add(Event.of(new SpriteRespawned(bomb.spriteId, alien.getX(), alien.getY()),
                        Tags.and(spriteTagBomb, bombTag, EntityTags.GAME)));
            }

            int bombX = bomb.getX();
            int bombY = bomb.getY();
            int playerX = player.x;
            int playerY = player.y;

            if (player.visible && bomb.visible) {

                if (bombX >= (playerX)
                        && bombX <= (playerX + PLAYER_WIDTH)
                        && bombY >= (playerY)
                        && bombY <= (playerY + PLAYER_HEIGHT)) {

                    eventsToPublish.add(Event.of(new SpriteExplosionTriggered(player.spriteId),
                            Tags.and(spriteTagPlayer, playerTag, EntityTags.GAME)));
                    eventsToPublish.add(Event.of(new SpriteImageChanged(player.spriteId, BOMB_EXPLOSION_IMAGE_PATH), // make simple automation to trigger ChangeSpriteImage upon SpriteExplosionTriggered
                            Tags.and(spriteTagPlayer, playerTag, EntityTags.GAME)));
                    eventsToPublish.add(Event.of(new SpriteDestroyed(bomb.spriteId),
                            Tags.and(spriteTagBomb, bombTag, EntityTags.GAME)));
                }
            }

            if (bomb.visible) {

                int newY = bomb.getY() + bomb.getSpeed();
                eventsToPublish.add(Event.of(new SpriteMoved(bomb.spriteId, bomb.x, newY),
                        Tags.and(spriteTagBomb, bombTag, EntityTags.GAME)));

                if (bomb.getY() >= Constants.GROUND_Y - Constants.BOMB_HEIGHT) {

                    eventsToPublish.add(Event.of(new SpriteDestroyed(bomb.spriteId),
                            Tags.and(spriteTagBomb, bombTag, EntityTags.GAME)));
                }
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

    private static class BombState {

        List<SpriteId> alienIds = new ArrayList<>();
        List<SpriteId> bombIds = new ArrayList<>();
        SpriteId playerId;
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
                case BOMB_ENTITY -> {
                    bombIds.add(spriteCreated.id());
                    sprites.put(spriteCreated.id(), spriteState);
                }
                case PLAYER_ENTITY -> {
                    playerId = spriteCreated.id();
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
