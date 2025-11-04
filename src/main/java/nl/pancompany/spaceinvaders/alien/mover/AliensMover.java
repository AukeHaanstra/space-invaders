package nl.pancompany.spaceinvaders.alien.mover;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.*;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.*;

@RequiredArgsConstructor
public class AliensMover {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCycleInitiated gameCycleInitiated) {
        COMMAND_EXECUTOR.accept(() -> decide(new MoveAliens()));
    }

    private void decide(MoveAliens moveAliens) {
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        StateManager<AliensState> stateManager = eventStore.loadState(AliensState.class,
                Query.of(alienTag, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteRestsInPeace.class)));
        AliensState aliensState = stateManager.getState().orElseThrow(
                () -> new IllegalStateException("Aliens cannot move before being created."));

        List<Event> alienMovements = new ArrayList<>();

        for (AlienState alienState : aliensState.aliens.values()) {

            if (!alienState.visible) { // i.e. R.I.P.
                return;
            }

            int dx = switch (alienState.direction) {
                case LEFT -> -alienState.speed;
                case RIGHT -> alienState.speed;
                default -> throw new IllegalStateException("Illegal alien move direction: " + alienState.direction);
            };

            int newX = alienState.x + dx;

            alienMovements.add(Event.of(new SpriteMoved(alienState.spriteId, newX, alienState.y),
                    Tags.and(Tag.of(SPRITE_ENTITY, alienState.spriteId.toString()), alienTag, EntityTags.GAME)));
        }

        stateManager.apply(alienMovements);
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
        AliensState(SpriteCreated spriteCreated) {
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
