package nl.pancompany.spaceinvaders.test.alien;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class AlienDropperTest {

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
    void givenAlienMoved_whenGameCycleInitiated_thenAlienMovedDown() {
        SpriteId alienSpriteId = ALIEN_SPRITE_IDS.getFirst();
        Tag alienSpriteTag = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));
        await().untilAsserted(() -> { // wait until alien is created
            Query query = Query.of(alienSpriteTag, Type.of(SpriteCreated.class));
            assertThat(eventStore.read(query)).hasSize(1);
        });
        // direction: right
        SpriteTurned spriteTurned = new SpriteTurned(alienSpriteId, Direction.RIGHT);
        eventStore.append(Event.of(spriteTurned, alienSpriteTag));
        // position: right border
        SpriteMoved alienSpriteMoved = new SpriteMoved(alienSpriteId, BOARD_WIDTH - ALIEN_BORDER_RIGHT, ALIEN_START_Y);
        eventStore.append(Event.of(alienSpriteMoved, alienSpriteTag));

        GameCycleInitiated gameCycleInitiated = new GameCycleInitiated();
        eventStore.append(Event.of(gameCycleInitiated, GAME));
//        await().untilAsserted(() -> { // wait until alien is moved by AlienMover
//            Query query = Query.of(alienSpriteTag, Type.of(SpriteMoved.class));
//            List<SequencedEvent> events = eventStore.read(query);
//            assertThat(events).hasSize(1);
//            SpriteMoved spriteMoved = events.getLast().payload(SpriteMoved.class);
//            assertThat(spriteMoved.newX()).isEqualTo(ALIEN_START_X - ALIEN_SPEED);
//        });

        await().untilAsserted(() -> {
            Query query = Query.of(alienSpriteTag, Type.of(SpriteMoved.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events.size()).isGreaterThanOrEqualTo(2);
            SpriteMoved spriteMoved = events.getLast().payload(SpriteMoved.class);
            assertThat(spriteMoved.newY()).isEqualTo(ALIEN_START_Y + ALIEN_STEP_DOWN);

            query = Query.of(alienSpriteTag, Type.of(SpriteTurned.class));
            events = eventStore.read(query);
            assertThat(events).hasSize(2);
            SpriteTurned secondSpriteTurned = events.getLast().payload(SpriteTurned.class);
            assertThat(secondSpriteTurned.direction()).isEqualTo(Direction.LEFT);
        });
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    // TODO: Add more tests



}
