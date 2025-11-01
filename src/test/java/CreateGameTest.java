import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.eventstore.record.Event;
import nl.pancompany.eventstore.record.SequencedEvent;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class CreateGameTest {

    EventStore eventStore;
    CommandApi commandApi;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
    }

    @Test
    void given__whenCreateGame_thenGameCreated() {
        commandApi.publish(new CreateGame());

        Query query = Query.of(EntityTags.GAME, Type.of(GameCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(GameCreated.class)).isEqualTo(new GameCreated());
    }

    @Test
    void givenGameCreated_whenCreateGame_thenIllegalState() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(EntityTags.GAME), Event.of(gameCreated, EntityTags.GAME));

        Assertions.assertThatThrownBy(() -> commandApi.publish(new CreateGame())).isInstanceOf(IllegalStateException.class);
    }
}
