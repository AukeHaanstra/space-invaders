package nl.pancompany.spaceinvaders.sprite.changeimage;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.*;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteImageChanged;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;
import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;

@RequiredArgsConstructor
public class ChangeSpriteImageCommandHandler {

    private final EventStore eventStore;

    public void decide(ChangeSpriteImage changeSpriteImage) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, changeSpriteImage.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.taggedWith(spriteTag).andHavingType(Type.of(SpriteCreated.class).orType(SpriteImageChanged.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Sprite cannot change image before being created."));
        stateManager.apply(new SpriteImageChanged(changeSpriteImage.spriteId(), changeSpriteImage.imagePath()),
                Tags.and(spriteTag, GAME) );
    }

    private static class SpriteState {

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
        }

        @EventSourced
        void evolve(SpriteImageChanged spriteImageChanged) {
        }
    }
}
