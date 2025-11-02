package nl.pancompany.spaceinvaders.player.turn;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.Constants.PLAYER_SPRITE_ID;

@RequiredArgsConstructor
public class TurnPlayerCommandHandler {

    private final EventStore eventStore;

    public void decide(TurnPlayer turnPlayer) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Types.or(SpriteCreated.class, SpriteTurned.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Player cannot turn before being created."));
        stateManager.apply(new SpriteTurned(PLAYER_SPRITE_ID, turnPlayer.getDirection()), Tags.and(EntityTags.PLAYER, EntityTags.GAME));
    }

    private static class PlayerState {

        Direction direction;

        @StateCreator
        PlayerState(SpriteCreated spriteCreated) {
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            direction = spriteTurned.direction();
        }
    }
}
