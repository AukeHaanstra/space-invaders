package nl.pancompany.spaceinvaders.test.game;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.events.OneAlienDown;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.ALIEN_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
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
    void givenGameCreated_whenOneAlienDown_thenGameNotStopped() throws InterruptedException {
        GameCreated gameCreated = new GameCreated();
        SpriteId alienSpriteId = SpriteId.random();
        Tag alienSpriteTag = Tag.of(ALIEN_ENTITY, alienSpriteId.toString());
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        OneAlienDown oneAlienDown = new OneAlienDown(alienSpriteId);

        eventStore.append(
                Event.of(gameCreated, EntityTags.GAME),
                Event.of(oneAlienDown, Tags.and(alienSpriteTag, alienTag))
        );

        Thread.sleep(500);
        Query query = Query.of(EntityTags.GAME, Type.of(GameStopped.class));
        assertThat(eventStore.read(query)).hasSize(0);
    }

    @Test
    void givenGameCreated_when23AliensDown_thenGameNotStopped() throws InterruptedException {
        List<Event> events = new ArrayList<>();
        GameCreated gameCreated = new GameCreated();
        events.add(Event.of(gameCreated, EntityTags.GAME));
        for (int i = 0; i < 23; i++) {
            SpriteId alienSpriteId = SpriteId.random();
            Tag alienSpriteTag = Tag.of(ALIEN_ENTITY, alienSpriteId.toString());
            Tag alienTag = Tag.of(ALIEN_ENTITY);
            OneAlienDown oneAlienDown = new OneAlienDown(alienSpriteId);
            events.add(Event.of(oneAlienDown, Tags.and(alienSpriteTag, alienTag)));
        }

        eventStore.append(events);

        Thread.sleep(500);
        Query query = Query.of(EntityTags.GAME, Type.of(GameStopped.class));
        assertThat(eventStore.read(query)).hasSize(0);
    }

    @Test
    void givenGameCreated_when24AliensDown_thenGameStopped() {
        List<Event> events = new ArrayList<>();
        GameCreated gameCreated = new GameCreated();
        events.add(Event.of(gameCreated, EntityTags.GAME));
        for (int i = 0; i < 24; i++) {
            SpriteId alienSpriteId = SpriteId.random();
            Tag alienSpriteTag = Tag.of(ALIEN_ENTITY, alienSpriteId.toString());
            Tag alienTag = Tag.of(ALIEN_ENTITY);
            OneAlienDown oneAlienDown = new OneAlienDown(alienSpriteId);
            events.add(Event.of(oneAlienDown, Tags.and(alienSpriteTag, alienTag)));
        }

        eventStore.append(events);

        Query query = Query.of(EntityTags.GAME, Type.of(GameStopped.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
    }

}
