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
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.test.TestUtil.withoutLogging;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PlayerCreatorTest {

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
    void given__whenCreateGame_thenPlayerCreated() {
        commandApi.publish(new CreateGame());

        Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(PlayerCreated.class)).isEqualTo(new PlayerCreated(PLAYER_IMAGE_PATH,
                PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED));
    }

    @Test
    void givenPlayerCreated_whenCreateGame_thenIllegalState() {
        withoutLogging(() -> {
                    PlayerCreated playerCreated = new PlayerCreated(PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED);
                    eventStore.append(Event.of(playerCreated, EntityTags.PLAYER));

                    commandApi.publish(new CreateGame());

                    await().untilAsserted(() -> assertThat(eventBus.hasLoggedExceptions()).isTrue());
                    assertThat(eventBus.getLoggedExceptions().getLast().exception()).isInstanceOf(IllegalStateException.class);
                }
        );
    }
}
