package nl.pancompany.spaceinvaders.sprite.explode;

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
import nl.pancompany.spaceinvaders.events.SpriteExplosionTriggered;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;
import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;

@RequiredArgsConstructor
public class TriggerSpriteExplosionCommandHandler {

    private final EventStore eventStore;

    public void decide(TriggerSpriteExplosion triggerSpriteExplosion) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, triggerSpriteExplosion.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.taggedWith(spriteTag).andHavingType(Type.of(SpriteCreated.class).orType(SpriteExplosionTriggered.class)));
        SpriteState spriteState = stateManager.getState().orElseThrow(() -> new IllegalStateException("Sprite cannot explode before being created."));
        stateManager.apply(new SpriteExplosionTriggered(triggerSpriteExplosion.spriteId()), Tags.and(spriteTag, Tag.of(spriteState.entityName), GAME));
    }

    private static class SpriteState {

        boolean explosionTriggered;
        String entityName;

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
            entityName = spriteCreated.entityName();
        }

        @EventSourced
        void evolve(SpriteExplosionTriggered spriteExplosionTriggered) {
            explosionTriggered = true;
        }
    }
}
