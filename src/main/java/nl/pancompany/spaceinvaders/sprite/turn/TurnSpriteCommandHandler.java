package nl.pancompany.spaceinvaders.sprite.turn;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.shared.Constants.PLAYER_SPRITE_ID;
import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;

@RequiredArgsConstructor
public class TurnSpriteCommandHandler {

    private final EventStore eventStore;

    public void decide(TurnSprite turnSprite) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, turnSprite.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.of(spriteTag, Types.or(SpriteCreated.class, SpriteTurned.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Sprite cannot turn before being created."));
        if (turnSprite.direction() == null || turnSprite.direction() == Direction.NONE) {
            throw new IllegalArgumentException("Turn direction can only be left or right.");
        }
        stateManager.apply(new SpriteTurned(PLAYER_SPRITE_ID, turnSprite.direction()), Tags.and(spriteTag, EntityTags.GAME));
    }

    private static class SpriteState {

        Direction direction;

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            direction = spriteTurned.direction();
        }
    }
}
