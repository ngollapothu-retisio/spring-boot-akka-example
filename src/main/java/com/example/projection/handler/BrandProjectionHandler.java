package com.example.projection.handler;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.example.aggregate.BrandAggregate;
import com.example.command.BrandCommand;
import com.example.event.BrandEvent;
import com.example.r2dbc.StatementWrapper;
import com.example.request.BrandRequest;
import com.example.util.BrandServiceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import akka.Done;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.kafka.javadsl.SendProducer;
import akka.persistence.query.typed.EventEnvelope;
import akka.projection.r2dbc.javadsl.R2dbcHandler;
import akka.projection.r2dbc.javadsl.R2dbcSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrandProjectionHandler extends R2dbcHandler<EventEnvelope<BrandEvent>> {

    private final String topic;
    private final SendProducer<String, String> sendProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClusterSharding clusterSharding;
    public BrandProjectionHandler(ClusterSharding clusterSharding, String topic, SendProducer<String, String> sendProducer) {
        this.topic = topic;
        this.sendProducer = sendProducer;
        this.clusterSharding = clusterSharding;
        objectMapper.registerModule(new DefaultScalaModule());
    }

    @Override
    public CompletionStage<Done> process(
            R2dbcSession session, EventEnvelope<BrandEvent> envelope) {
    	BrandEvent event = envelope.event();
        return processReadSide(session, event)
                .thenCompose(done -> sendToKafkaTopic(event));
    }
    public EntityRef<BrandCommand> ref(String id) {
        return clusterSharding.entityRefFor(BrandAggregate.ENTITY_TYPE_KEY, id);
    }
    private String toJsonString(Object object){
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CompletionStage<Done> sendToKafkaTopic(BrandEvent event){
        if (event instanceof BrandEvent.Created
            || event instanceof BrandEvent.Updated
            || event instanceof BrandEvent.Deleted
        ) {
            return BrandServiceUtil.get(event.id(), ref(event.id()))
            		.toFuture()
                    .thenCompose(brand -> {
                        return sendToKafkaTopic(event.id(), brand.orElseGet(()-> null), event);
                    });
        } else {
            log.debug("Entity event {} is not sent", event);
            return CompletableFuture.completedFuture(Done.getInstance());
        }
    }
    private CompletionStage<Done> sendToKafkaTopic(String key, Object object, BrandEvent event){
        log.info("Entity topic::{}, key::{} sending", topic, key);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, toJsonString(object));
        return sendProducer
                .send(producerRecord)
                .thenApply(
                        recordMetadata -> {
                            log.info(
                                    "Published event [{}] to topic/partition {}/{}",
                                    event,
                                    topic,
                                    recordMetadata.partition());
                            return Done.getInstance();
                        });
    }
    private CompletionStage<Done> processReadSide(R2dbcSession session, BrandEvent event){
        if(event instanceof BrandEvent.Created) return createOrUpdate(session, event.id(), ((BrandEvent.Created) event).request);
        else if(event instanceof BrandEvent.Updated) return createOrUpdate(session, event.id(), ((BrandEvent.Updated) event).request);
        else if(event instanceof BrandEvent.Deleted) return delete(session, event.id());
        else return CompletableFuture.completedFuture(Done.getInstance());
    }
    private CompletionStage<Done> createOrUpdate(R2dbcSession session, String entityId, BrandRequest request) {
        log.info("Entity createOrUpdate id::{}", entityId);
        AtomicInteger index = new AtomicInteger(-1);
        StatementWrapper ps = new StatementWrapper(session.createStatement(SAVE_BRAND_QUERY));
        ps.setString(index.incrementAndGet(), entityId);
        ps.setString(index.incrementAndGet(), request.name);
        ps.setBoolean(index.incrementAndGet(), request.active);
        ps.setString(index.incrementAndGet(), request.logo);
        ps.setTimestamp(index.incrementAndGet(), Timestamp.valueOf(LocalDateTime.now()));
        return session.updateOne(ps.getStatement()).thenApply(rowsUpdated -> Done.getInstance());
    }
    private static final String SAVE_BRAND_QUERY = "INSERT INTO BRAND(" +
            "ID, " +
            "NAME, " +
            "ACTIVE, " +
            "LOGO, " +
            "LAST_MODIFIED_TMST) " +
            "VALUES ($1, $2, $3, $4, $5) " +
            "on conflict (ID) DO UPDATE " +
            "set NAME=excluded.NAME, " +
            "ACTIVE=excluded.ACTIVE, " +
            "LOGO=excluded.LOGO, " +
            "LAST_MODIFIED_TMST=now()";

    private static final String DELETE_QUERY = "DELETE FROM BRAND WHERE ID = $1";
    private CompletionStage<Done> delete(R2dbcSession session, String entityId) {
        AtomicInteger index = new AtomicInteger(-1);
        StatementWrapper ps = new StatementWrapper(session.createStatement(DELETE_QUERY));
        ps.setString(index.incrementAndGet(), entityId);
        return session.updateOne(ps.getStatement()).thenApply(rowsUpdated -> Done.getInstance());
    }
}