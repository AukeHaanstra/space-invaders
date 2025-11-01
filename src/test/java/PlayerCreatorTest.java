import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.eventstore.record.SequencedEvent;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.PlayerCreated;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PlayerCreatorTest {

    EventStore eventStore;
    CommandApi commandApi;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
    }

    @Test
    void createsPlayerWhenNoPlayerCreated() {
        commandApi.publish(new CreateGame());

        Query query = Query.of(EntityTags.PLAYER, Type.of(PlayerCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(PlayerCreated.class)).isEqualTo(new PlayerCreated(PLAYER_IMAGE_PATH,
                PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED));
    }
}
