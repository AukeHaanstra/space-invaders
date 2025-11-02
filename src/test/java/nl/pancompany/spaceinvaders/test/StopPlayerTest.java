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
import nl.pancompany.spaceinvaders.events.PlayerStopped;
import nl.pancompany.spaceinvaders.player.stop.StopPlayer;
import nl.pancompany.spaceinvaders.shared.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class StopPlayerTest {

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
    void givenPlayerCreated_whenTurnPlayer_thenPlayerTurned() {
        PlayerCreated playerCreated = new PlayerCreated(PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(playerCreated, EntityTags.PLAYER));

        commandApi.publish(new StopPlayer());

        Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerStopped.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(PlayerStopped.class)).isEqualTo(new PlayerStopped());
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void given__whenTurnPlayer_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new StopPlayer())).isInstanceOf(IllegalStateException.class);
    }
}
