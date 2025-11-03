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
import nl.pancompany.spaceinvaders.Constants;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.sprite.Alien;

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

        // 24 Aliens and 24 Bombs
        // The alien image size is 12x12px. We put 6px space among the aliens.
        int n = 0;
        for (int i = 0; i < 4; i++) { // 4 rows
            for (int j = 0; j < 6; j++, n++) { // 6 columns
                final int row = i, col = j, alienNo = n;
                COMMAND_EXECUTOR.accept(() -> decide(new RegisterSpriteCreated(ALIEN_SPRITE_IDS.get(alienNo), ALIEN_IMAGE_PATH,
                        ALIEN_START_X + 18 * col, ALIEN_START_Y + 18 * row, ALIEN_SPEED, Direction.LEFT)));
                COMMAND_EXECUTOR.accept(() -> decide(new RegisterSpriteCreated(BOMB_SPRITE_IDS.get(alienNo), BOMB_IMAGE_PATH,
                        ALIEN_START_X + 18 * col, ALIEN_START_Y + 18 * row, BOMB_SPEED, Direction.DOWN)));
            }
        }
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
