package nl.pancompany.spaceinvaders.player.stop;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteStopped;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.shared.Constants.PLAYER_ENTITY;
import static nl.pancompany.spaceinvaders.shared.Constants.PLAYER_SPRITE_ID;

@RequiredArgsConstructor
public class StopPlayerCommandHandler {

    private final EventStore eventStore;

    public void decide(StopPlayer stopPlayer) {
        StateManager<PlayerState> stateManager = eventStore.loadState(PlayerState.class,
                Query.of(EntityTags.PLAYER, Types.or(SpriteCreated.class, SpriteStopped.class)));
        stateManager.getState().orElseThrow(() -> new IllegalStateException("Player cannot stop before being created."));
        stateManager.apply(new SpriteStopped(PLAYER_SPRITE_ID), Tags.and(EntityTags.PLAYER, Tag.of(PLAYER_ENTITY), EntityTags.GAME));
    }

    private static class PlayerState {

        Direction direction;

        @StateCreator
        PlayerState(SpriteCreated spriteCreated) {
        }

        @EventSourced
        void evolve(SpriteStopped spriteStopped) {
            direction = Direction.NONE;
        }
    }
}
