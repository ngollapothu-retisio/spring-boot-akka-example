package com.example.projection;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.common.serialization.StringSerializer;

import com.example.aggregate.BrandAggregate;
import com.example.event.BrandEvent;
import com.example.projection.handler.BrandProjectionHandler;

import akka.actor.CoordinatedShutdown;
import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.japi.Pair;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.SendProducer;
import akka.persistence.query.Offset;
import akka.persistence.query.typed.EventEnvelope;
import akka.persistence.r2dbc.query.javadsl.R2dbcReadJournal;
import akka.projection.Projection;
import akka.projection.ProjectionBehavior;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.SourceProvider;
import akka.projection.r2dbc.R2dbcProjectionSettings;
import akka.projection.r2dbc.javadsl.R2dbcProjection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrandProjection {

    public static void init(ActorSystem<?> system, ClusterSharding clusterSharding) {
        // Split the slices into 4 ranges
        int numberOfSliceRanges = 4;
        List<Pair<Integer, Integer>> sliceRanges =
                EventSourcedProvider.sliceRanges(
                        system, R2dbcReadJournal.Identifier(), numberOfSliceRanges);

        SendProducer<String, String> sendProducer = createProducer(system);
        String topic = system.settings().config().getString("application.projection.topic.brand");

        ShardedDaemonProcess.get(system)
                .init(
                        ProjectionBehavior.Command.class,
                        system.settings().config().getString("application.projection.name.brand"),
                        sliceRanges.size(),
                        i -> ProjectionBehavior.create(createProjection(system, clusterSharding, sliceRanges.get(i), topic, sendProducer)),
                        ProjectionBehavior.stopMessage());
    }

    private static SendProducer<String, String> createProducer(ActorSystem<?> system) {
        String bootstrapServers = system.settings().config().getString("lagom.broker.kafka.brokers");
        ProducerSettings<String, String> producerSettings =
                ProducerSettings.create(system, new StringSerializer(), new StringSerializer())
                        .withBootstrapServers(bootstrapServers);;
        SendProducer<String, String> sendProducer = new SendProducer<>(producerSettings, system);
        CoordinatedShutdown.get(system)
                .addTask(
                        CoordinatedShutdown.PhaseActorSystemTerminate(),
                        "close-sendProducer",
                        () -> sendProducer.close());
        return sendProducer;
    }

    private static Projection<EventEnvelope<BrandEvent>> createProjection(ActorSystem<?> system, ClusterSharding clusterSharding,
                                                                     Pair<Integer, Integer> sliceRange, String topic, SendProducer<String, String> sendProducer) {
        int minSlice = sliceRange.first();
        int maxSlice = sliceRange.second();

        String entityType = BrandAggregate.ENTITY_TYPE_KEY.name();

        SourceProvider<Offset, EventEnvelope<BrandEvent>> sourceProvider =
                EventSourcedProvider.eventsBySlices(
                        system, R2dbcReadJournal.Identifier(), entityType, minSlice, maxSlice);

        ProjectionId projectionId =
                ProjectionId.of("Brands", "brands-" + minSlice + "-" + maxSlice);
        Optional<R2dbcProjectionSettings> settings = Optional.empty();

        int saveOffsetAfterEnvelopes = 100;
        Duration saveOffsetAfterDuration = Duration.ofMillis(500);
        log.info("BrandProjection init()..................");
        return R2dbcProjection.atLeastOnce(
                        projectionId, settings, sourceProvider, () -> new BrandProjectionHandler(clusterSharding, topic, sendProducer), system)
                        .withSaveOffset(saveOffsetAfterEnvelopes, saveOffsetAfterDuration);
    }
}
