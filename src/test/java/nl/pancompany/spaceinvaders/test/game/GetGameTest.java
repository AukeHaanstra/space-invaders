package nl.pancompany.spaceinvaders.test.game;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.spaceinvaders.QueryApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameResumed;
import nl.pancompany.spaceinvaders.events.GameStopped;
import nl.pancompany.spaceinvaders.game.get.GameReadModel;
import nl.pancompany.spaceinvaders.game.get.GetGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetGameTest {

    EventStore eventStore;
    QueryApi queryApi;
    EventBus eventBus;

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
    void givenNoGameCreated_whenGameCreated_thenReadModelFound() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));

        await().untilAsserted(() -> {
            Optional<GameReadModel> readModel = queryApi.query(new GetGame());
            assertThat(readModel).isPresent();
            assertThat(readModel.get().inGame()).isTrue();
            assertThat(readModel.get().replay()).isFalse();
            assertThat(readModel.get().message()).isEmpty();
        });
    }

    @Test
    void givenGameCreated_whenGameStopped_thenInGameIsUpdated() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));

        GameStopped gameStopped = new GameStopped("Game Over!");
        eventStore.append(Event.of(gameStopped, GAME));

        await().untilAsserted(() -> {
            Optional<GameReadModel> readModel = queryApi.query(new GetGame());
            assertThat(readModel).isPresent();
            assertThat(readModel.get().inGame()).isFalse();
            assertThat(readModel.get().replay()).isFalse();
            assertThat(readModel.get().message().get()).isEqualTo("Game Over!");
        });
    }

    @Test
    void givenGameCreated_whenReplay_thenInGameFalseReplayTrue() {
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));

        eventBus.replay();

        await().untilAsserted(() -> {
            Optional<GameReadModel> readModel = queryApi.query(new GetGame());
            assertThat(readModel).isPresent();
            assertThat(readModel.get().inGame()).isFalse();
            assertThat(readModel.get().replay()).isTrue();
            assertThat(readModel.get().message()).isEmpty();
        });
    }

    @Test
    void givenReplayed_whenGameResumed_thenInGameTrueReplayFalse() {
        eventBus.replay();
        await().untilAsserted(() -> {
            Optional<GameReadModel> readModel = queryApi.query(new GetGame());
            assertThat(readModel).isPresent();
            assertThat(readModel.get().inGame()).isFalse();
            assertThat(readModel.get().replay()).isTrue();
            assertThat(readModel.get().message()).isEmpty();
        });

        GameResumed gameResumed = new GameResumed();
        eventStore.append(Event.of(gameResumed, GAME));

        await().untilAsserted(() -> {
            Optional<GameReadModel> readModel = queryApi.query(new GetGame());
            assertThat(readModel).isPresent();
            assertThat(readModel.get().inGame()).isTrue();
            assertThat(readModel.get().replay()).isFalse();
            assertThat(readModel.get().message()).isEmpty();
        });
    }

}
