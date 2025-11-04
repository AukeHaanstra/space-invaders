package nl.pancompany.spaceinvaders.sprite.destroy;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteDestroyed;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;
import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;

@RequiredArgsConstructor
public class DestroySpriteCommandHandler {

    private final EventStore eventStore;

    public void decide(DestroySprite destroySprite) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, destroySprite.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.taggedWith(spriteTag).andHavingType(Type.of(SpriteCreated.class).orType(SpriteDestroyed.class)));
        SpriteState spriteState = stateManager.getState().orElseThrow(() -> new IllegalStateException("Sprite cannot be destroyed before being created."));
        stateManager.apply(new SpriteDestroyed(destroySprite.spriteId()), Tags.and(spriteTag, Tag.of(spriteState.entityName), GAME));
    }

    private static class SpriteState {

        boolean visible;
        String entityName;

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
            entityName = spriteCreated.entityName();
        }

        @EventSourced
        void evolve(SpriteDestroyed spriteDestroyed) {
            visible = false;
        }
    }
}
