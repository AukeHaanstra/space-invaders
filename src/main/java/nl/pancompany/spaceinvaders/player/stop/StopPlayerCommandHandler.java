package nl.pancompany.spaceinvaders.player.stop;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.events.PlayerStopped;
import nl.pancompany.spaceinvaders.shared.Direction;

@RequiredArgsConstructor
public class StopPlayerCommandHandler {

    private final EventStore eventStore;

    public void decide(StopPlayer stopPlayer) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Types.or(PlayerCreated.class, PlayerStopped.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Player stopped before being created."));
        stateManager.apply(new PlayerStopped(), Tags.and(EntityTags.PLAYER, EntityTags.GAME));
    }

    private static class PlayerState {

        Direction direction;

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
        }

        @EventSourced
        void evolve(PlayerStopped playerStopped) {
            direction = Direction.NONE;
        }
    }
}
