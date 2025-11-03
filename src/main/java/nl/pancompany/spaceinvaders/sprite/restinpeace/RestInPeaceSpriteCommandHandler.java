package nl.pancompany.spaceinvaders.sprite.restinpeace;

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
import nl.pancompany.spaceinvaders.events.SpriteRestsInPeace;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;
import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;

@RequiredArgsConstructor
public class RestInPeaceSpriteCommandHandler {

    private final EventStore eventStore;

    public void decide(RestInPeaceSprite restInPeaceSprite) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, restInPeaceSprite.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.taggedWith(spriteTag).andHavingType(Type.of(SpriteCreated.class).orType(SpriteRestsInPeace.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Sprite cannot rest in peace before being created."));
        stateManager.apply(new SpriteRestsInPeace(restInPeaceSprite.spriteId()), Tags.and(spriteTag, GAME));
    }

    private static class SpriteState {

        boolean visible;

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
        }

        @EventSourced
        void evolve(SpriteRestsInPeace spriteRestsInPeace) {
            visible = false;
        }
    }
}
