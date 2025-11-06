package nl.pancompany.spaceinvaders.game.stopper;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.*;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.events.OneAlienDown;
import nl.pancompany.spaceinvaders.shared.EntityTags;

import java.util.concurrent.atomic.AtomicInteger;

import static nl.pancompany.spaceinvaders.CommandApi.COMMAND_EXECUTOR;
import static nl.pancompany.spaceinvaders.shared.Constants.ALIEN_ENTITY;
import static nl.pancompany.spaceinvaders.shared.Constants.GAME_ENTITY;

@RequiredArgsConstructor
public class GameStopper {

    private final EventStore eventStore;
    private final AtomicInteger alienDownCount = new AtomicInteger(0); // repo

    @EventHandler // projector
    private void update(OneAlienDown oneAlienDown) {
    }

    private void decide(StopGame stopGame) {
        StateManager<AlienState> stateManager = eventStore.loadState(AlienState.class,
                Query.fromItems(QueryItems.or(
                        QueryItem.of(EntityTags.GAME, Types.or(GameCreated.class)),
                        QueryItem.of(Tag.of(ALIEN_ENTITY), Types.or(OneAlienDown.class))
                ))
        );
        AlienState alienState = stateManager.getState().orElseThrow(() -> new IllegalStateException("Aliens cannot go down before game being created."));
        if (alienState.alienDownCount == 24) {
            stateManager.apply(new GameStopped("Game won!"), Tags.and(EntityTags.GAME, Tag.of(GAME_ENTITY)));
        }
    }

    // static!
    private static class AlienState {

        int alienDownCount = 0;

        @StateCreator
        AlienState(GameCreated gameCreated) {
        }

        @EventSourced
        void evolve(GameStopped gameStopped) {
        }

    }


}
