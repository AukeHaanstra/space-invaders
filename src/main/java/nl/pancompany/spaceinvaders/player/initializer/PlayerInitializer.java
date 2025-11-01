package nl.pancompany.spaceinvaders.player.initializer;

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
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.shared.SpriteState;

import javax.swing.*;
import java.util.Optional;

@RequiredArgsConstructor
public class PlayerInitializer {

    private final EventStore eventStore;

    @EventHandler
    private void handle(GameCreated gameCreated) {
        handle(new RegisterPlayerCreated("/images/player.png", 270, 280));
    }

    private void handle(RegisterPlayerCreated registerPlayerCreated) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Type.of(PlayerCreated.class)));
        Optional<PlayerState> playerState = stateManager.getState();
        if (playerState.isEmpty()) { // never prepare game anew
            stateManager.apply(new PlayerCreated(), EntityTags.PLAYER);
            stateManager.apply(new SpriteCreated(), EntityTags.PLAYER);
        }
    }

    private static class PlayerState extends SpriteState {

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
        }

    }

}
