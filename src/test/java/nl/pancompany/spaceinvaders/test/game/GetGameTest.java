package nl.pancompany.spaceinvaders.test.game;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.spaceinvaders.QueryApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.game.get.GameReadModel;
import nl.pancompany.spaceinvaders.game.get.GetGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.EntityTags.GAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetGameTest {

    EventStore eventStore;
    QueryApi queryApi;
    EventBus eventBus;
    Query gameQuery = Query.taggedWith(GAME).andHavingType(GameCreated.class, GameStopped.class);

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        queryApi = spaceInvaders.getQueryApi();
        eventBus = eventStore.getEventBus();
    }

    @Test
    void givenNoGameCreated_thenNoReadModelFound() {
        assertThat(queryApi.query(new GetGame())).isEmpty();
    }

    @Test
    void givenGameCreated_thenReadModelFound() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));
        await().untilAsserted(() -> assertThat(eventStore.read(gameQuery)).hasSize(1));

        Optional<GameReadModel> readModel = queryApi.query(new GetGame());
        assertThat(readModel).isPresent();
        assertThat(readModel.get().inGame()).isTrue();
    }

    @Test
    void givenGameCreated_whenGameStopped_thenInGameIsUpdated() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));
        await().untilAsserted(() -> assertThat(eventStore.read(gameQuery)).hasSize(1));

        GameStopped gameStopped = new GameStopped("Game Over!");
        eventStore.append(Event.of(gameStopped, GAME));

        await().untilAsserted(() -> assertThat(eventStore.read(gameQuery)).hasSize(2));
        Optional<GameReadModel> readModel = queryApi.query(new GetGame());
        assertThat(readModel).isPresent();
        assertThat(readModel.get().inGame()).isFalse();
        assertThat(readModel.get().message().get()).isEqualTo("Game Over!");
    }

}
