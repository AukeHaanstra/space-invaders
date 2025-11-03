package nl.pancompany.spaceinvaders.test.game;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.game.stopper.StopGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class GameStopperTest {

    EventStore eventStore;
    CommandApi commandApi;
    EventBus eventBus;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
        eventBus = eventStore.getEventBus();
    }

    @Test
    void given__whenStopGame_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new StopGame("message"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenGameCreated__whenStopGame_thenGameStopped() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        commandApi.publish(new StopGame("message"));

        Query query = Query.of(EntityTags.GAME, Type.of(GameStopped.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(GameStopped.class)).isEqualTo(new GameStopped("message"));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    /*
            It is allowed to try to stop the game twice, because a new gamecycle might have started while the previous
            StopGame command still needs to be processed.
     */
    @Test
    void givenGameStopped_whenStopGame_thenGameStopped() {
        GameCreated gameCreated = new GameCreated();
        GameStopped gameStopped = new GameStopped("message");
        eventStore.append(Event.of(gameCreated, EntityTags.GAME), Event.of(gameStopped, EntityTags.GAME));

        commandApi.publish(new StopGame("message2"));

        Query query = Query.of(EntityTags.GAME, Type.of(GameStopped.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(2));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(GameStopped.class)).isEqualTo(new GameStopped("message"));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

}
