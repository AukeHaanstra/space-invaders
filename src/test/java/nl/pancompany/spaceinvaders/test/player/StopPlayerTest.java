package nl.pancompany.spaceinvaders.test.player;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.SpriteStopped;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.player.stop.StopPlayer;
import nl.pancompany.spaceinvaders.shared.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.*;
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
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, EntityTags.PLAYER));

        commandApi.publish(new StopPlayer());

        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteStopped.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteStopped.class)).isEqualTo(new SpriteStopped(PLAYER_SPRITE_ID));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void given__whenTurnPlayer_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new StopPlayer())).isInstanceOf(IllegalStateException.class);
    }
}
