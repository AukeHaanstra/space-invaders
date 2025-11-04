package nl.pancompany.spaceinvaders.test.sprite;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.shared.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.test.TestUtil.withoutLogging;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SpriteCreatorTest {

    private static final int TOTAL_NUMBER_OF_SPRITES_CREATED = 1 + 24 + 24; // 1 player + 24 aliens + 24 bombs

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
    void given__whenCreateGame_thenPlayerSpriteCreated() {
        commandApi.publish(new CreateGame());

        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteCreated.class)).isEqualTo(new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_ENTITY, PLAYER_IMAGE_PATH,
                PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE));
        assertThat(events.getFirst().tags()).contains(Tag.of(PLAYER_ENTITY));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void given__whenCreateGame_thenAlienSpritesCreated() {
        commandApi.publish(new CreateGame());

        Query query = Query.of(Tags.all(), Type.of(SpriteCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(TOTAL_NUMBER_OF_SPRITES_CREATED));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.stream()
                .map(event -> event.payload(SpriteCreated.class))
                .map(SpriteCreated::id)
                .filter(ALIEN_SPRITE_IDS::contains)
                .count()
        ).isEqualTo(24);
        // check that some identifying property of aliens is present:
        assertThat(events).anyMatch(event -> event.payload(SpriteCreated.class).speed() == ALIEN_SPEED);
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void given__whenCreateGame_thenBombSpritesCreated() {
        commandApi.publish(new CreateGame());

        Query query = Query.of(Tags.all(), Type.of(SpriteCreated.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(TOTAL_NUMBER_OF_SPRITES_CREATED));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.stream()
                .map(event -> event.payload(SpriteCreated.class))
                .map(SpriteCreated::id)
                .filter(BOMB_SPRITE_IDS::contains)
                .count()
        ).isEqualTo(24);
        // check that some identifying property of bombs is present:
        assertThat(events).anyMatch(event -> event.payload(SpriteCreated.class).direction() == Direction.DOWN);
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenSpriteCreated_whenCreateGame_thenIllegalState() {
        withoutLogging(() -> {
                    SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_ENTITY, PLAYER_IMAGE_PATH, PLAYER_START_X,
                            PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
                    eventStore.append(Event.of(spriteCreated, EntityTags.PLAYER));

                    commandApi.publish(new CreateGame());

                    await().untilAsserted(() -> assertThat(eventBus.hasLoggedExceptions()).isTrue());
                    assertThat(eventBus.getLoggedExceptions().getLast().exception()).isInstanceOf(IllegalStateException.class);
                }
        );
    }
}
