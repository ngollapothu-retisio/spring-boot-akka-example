package com.example.aggregate;

import com.example.command.BrandCommand;
import com.example.event.BrandEvent;
import com.example.state.BrandState;

import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.RetentionCriteria;

public class BrandAggregate extends EventSourcedBehaviorWithEnforcedReplies<BrandCommand, BrandEvent, BrandState> {

    public static final EntityTypeKey<BrandCommand> ENTITY_TYPE_KEY =
            EntityTypeKey.create(BrandCommand.class, "BrandAggregate");

	public static void init(ClusterSharding clusterSharding) {
        clusterSharding.init(
                Entity.of(
                        ENTITY_TYPE_KEY,
                        entityContext -> {
                            return Behaviors.setup(
                                    ctx -> EventSourcedBehavior.start(new BrandAggregate(entityContext.getEntityId()), ctx));
                        }));
    }
    protected BrandAggregate(String entityId) {
        super(
                PersistenceId.of(ENTITY_TYPE_KEY.name(), entityId)
        );
    }
    protected BrandAggregate(PersistenceId persistenceId) {
        super(persistenceId);
    }

    @Override
    public BrandState emptyState() {
        return BrandState.EMPTY;
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(3,
                2).withDeleteEventsOnSnapshot();
    }

    @Override
    public CommandHandlerWithReply<BrandCommand, BrandEvent, BrandState> commandHandler() {
        CommandHandlerWithReplyBuilder<BrandCommand, BrandEvent, BrandState> builder = newCommandHandlerWithReplyBuilder();

        builder.forAnyState()
                .onCommand(BrandCommand.Create.class, (state, cmd) ->
                        Effect().persist(new BrandEvent.Created(cmd.id, cmd.request))
                                .thenReply(cmd.replyTo, __ -> state.create("command", cmd.request).brand)
                )
                .onCommand(BrandCommand.Update.class, (state, cmd) ->
                        Effect().persist(new BrandEvent.Updated(cmd.id, cmd.request))
                                .thenReply(cmd.replyTo, __ -> state.update("command", cmd.request).brand)
                )
                .onCommand(BrandCommand.Delete.class, (state, cmd) ->
                        Effect().persist(new BrandEvent.Deleted(cmd.id))
                                .thenReply(cmd.replyTo, __ -> state.delete("command", cmd.id).brand)
                )
                .onCommand(BrandCommand.Get.class, (state, cmd) ->
                        Effect().none()
                                .thenReply(cmd.replyTo, __ -> state.get("command", cmd.id).brand)
                );

        return builder.build();
    }

    @Override
    public EventHandler<BrandState, BrandEvent> eventHandler() {
        EventHandlerBuilder<BrandState, BrandEvent> builder = newEventHandlerBuilder();
        builder.forAnyState()
                .onEvent(BrandEvent.Created.class, (state, evt) -> state.create("event", evt.request))
                .onEvent(BrandEvent.Updated.class, (state, evt) -> state.update("event", evt.request))
                .onEvent(BrandEvent.Deleted.class, (state, evt) -> state.delete("event", evt.id))
        ;
        return builder.build();
    }
}
