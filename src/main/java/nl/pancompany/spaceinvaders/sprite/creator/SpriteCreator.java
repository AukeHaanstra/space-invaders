package nl.pancompany.spaceinvaders.sprite.creator;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.shared.Direction;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.Constants.*;

@RequiredArgsConstructor
public class SpriteCreator {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCreated gameCreated) {
        // Player
        COMMAND_EXECUTOR.accept(() -> decide(new RegisterSpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE)));
    }

    private void decide(RegisterSpriteCreated registerSpriteCreated) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, registerSpriteCreated.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.of(spriteTag, Type.of(SpriteCreated.class)));
        Optional<SpriteState> spriteState = stateManager.getState();
        if (spriteState.isPresent()) {
            throw new IllegalStateException("Sprite cannot be created twice.");
        }
        stateManager.apply(new SpriteCreated(
                        registerSpriteCreated.spriteId(),
                        registerSpriteCreated.imagePath(),
                        registerSpriteCreated.startX(),
                        registerSpriteCreated.startY(),
                        registerSpriteCreated.speed(),
                        registerSpriteCreated.direction()),
                Tags.and(spriteTag, EntityTags.GAME));
    }

    private static class SpriteState {

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
        }

    }

}
