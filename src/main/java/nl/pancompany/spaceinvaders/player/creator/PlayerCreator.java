package nl.pancompany.spaceinvaders.player.creator;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.shared.Direction;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.Constants.*;

@RequiredArgsConstructor
public class PlayerCreator {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCreated gameCreated) {
        COMMAND_EXECUTOR.accept(() -> decide(new RegisterPlayerCreated(PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE)));
    }

    private void decide(RegisterPlayerCreated registerPlayerCreated) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Type.of(PlayerCreated.class)));
        Optional<PlayerState> playerState = stateManager.getState();
        if (playerState.isPresent()) {
            throw new IllegalStateException("Player cannot be created twice.");
        }
        stateManager.apply(new PlayerCreated(
                        registerPlayerCreated.getImagePath(),
                        registerPlayerCreated.getStartX(),
                        registerPlayerCreated.getStartY(),
                        registerPlayerCreated.getSpeed(),
                        registerPlayerCreated.getDirection()),
                Tags.and(EntityTags.PLAYER, EntityTags.GAME));
    }

    private static class PlayerState {

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
        }

    }

}
