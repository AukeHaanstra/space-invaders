import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.eventstore.record.Event;
import nl.pancompany.eventstore.record.SequencedEvent;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.events.PlayerTurned;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.events.SpriteTurned.TurnDirection.RIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class TurnPlayerTest {

    EventStore eventStore;
    CommandApi commandApi;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
    }

    @Test
    void givenGameAndPlayerCreated_whenTurnPlayer_thenPlayerTurned() {
        PlayerCreated playerCreated = new PlayerCreated(PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED);
        eventStore.append(Event.of(EntityTags.PLAYER), Event.of(playerCreated, EntityTags.PLAYER));

        commandApi.publish(new TurnPlayer(RIGHT));

        Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerTurned.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(PlayerTurned.class)).isEqualTo(new PlayerTurned(RIGHT));
    }
}
