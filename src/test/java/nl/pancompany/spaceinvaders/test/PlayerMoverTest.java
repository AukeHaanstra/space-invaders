package nl.pancompany.spaceinvaders.test;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PlayerMoverTest {

    EventStore eventStore;
    EventBus eventBus;
    CommandApi commandApi;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        eventBus = eventStore.getEventBus();
        commandApi = spaceInvaders.getCommandApi();
    }

    @Test
    void givenGameCreated_whenInitiateGameCycle_thenPlayerNotMoved() throws InterruptedException {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        commandApi.publish(new InitiateGameCycle());

        Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerMoved.class));
        Thread.sleep(500);
        assertThat(eventStore.read(query)).isEmpty();
    }

    @Test
    void givenGameCreatedAndPlayerTurned_whenInitiateGameCycle_thenPlayerMoved() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        PlayerTurned playerTurned = new PlayerTurned(SpriteTurned.TurnDirection.RIGHT);
        eventStore.append(Event.of(playerTurned, EntityTags.PLAYER));

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerMoved.class));
        Thread.sleep(500);
        List<SequencedEvent> sequencedEvents = eventStore.read(query);
        assertThat(sequencedEvents).hasSize(1);
        assertThat(sequencedEvents.getFirst().payload(PlayerMoved.class)).isEqualTo(new PlayerMoved(PLAYER_START_X + PLAYER_SPEED, PLAYER_START_Y));
    }

}
