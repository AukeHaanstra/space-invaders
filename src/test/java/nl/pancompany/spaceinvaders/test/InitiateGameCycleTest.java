package nl.pancompany.spaceinvaders.test;

import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameCycleInitiated;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class InitiateGameCycleTest {

    EventStore eventStore;
    CommandApi commandApi;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
    }

    @Test
    void given__whenInitiateGameCycle_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new InitiateGameCycle())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenGameCreated_whenInitiateGameCycle_thenGameCycleInitiated() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        commandApi.publish(new InitiateGameCycle());

        Query query = Query.of(EntityTags.GAME, Types.or(GameCycleInitiated.class, GameCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(2));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getLast().payload(GameCycleInitiated.class)).isEqualTo(new GameCycleInitiated());
    }
}
