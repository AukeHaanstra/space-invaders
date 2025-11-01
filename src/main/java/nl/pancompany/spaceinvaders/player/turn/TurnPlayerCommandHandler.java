package nl.pancompany.spaceinvaders.player.turn;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.events.PlayerTurned;
import nl.pancompany.spaceinvaders.events.SpriteTurned.TurnDirection;

@RequiredArgsConstructor
public class TurnPlayerCommandHandler {

    private final EventStore eventStore;

    public void decide(TurnPlayer turnPlayer) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Types.or(PlayerCreated.class, PlayerTurned.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Player turned before being created."));
        stateManager.apply(new PlayerTurned(turnPlayer.getTurnDirection()), EntityTags.PLAYER);
    }

    private static class PlayerState {

        TurnDirection turnDirection;

        @StateCreator
        PlayerState(PlayerCreated playerCreated) {
        }

        @EventSourced
        void evolve(PlayerTurned playerTurned) {
            turnDirection = playerTurned.getTurnDirection();
        }
    }
}
