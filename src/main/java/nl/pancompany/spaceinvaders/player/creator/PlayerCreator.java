package nl.pancompany.spaceinvaders.player.creator;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.PlayerCreated;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.Constants.*;

@RequiredArgsConstructor
public class PlayerCreator {

    private final EventStore eventStore;

    @EventHandler
    private void react(GameCreated gameCreated) {
        decide(new RegisterPlayerCreated(PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED));
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
                        registerPlayerCreated.getSpeed()),
                EntityTags.PLAYER);
    }

    private static class PlayerState {

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
        }

    }

}
