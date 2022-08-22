package com.example.util;

import java.time.Duration;
import java.util.Optional;

import com.example.command.BrandCommand;
import com.example.entity.Brand;
import com.example.request.BrandRequest;

import akka.cluster.sharding.typed.javadsl.EntityRef;
import reactor.core.publisher.Mono;

public class BrandServiceUtil {

    private static final Duration askTimeout = Duration.ofSeconds(5);

    public static Mono<Optional<Brand>> get(String id, EntityRef<BrandCommand> ref) {
        return Mono.fromCompletionStage(ref.<Optional<Brand>>ask(replyTo -> new BrandCommand.Get(id,replyTo), askTimeout));
    }
    public static Mono<Optional<Brand>> create(String id, BrandRequest request, EntityRef<BrandCommand> ref) {
        return Mono.fromCompletionStage(ref.<Optional<Brand>>ask(replyTo -> new BrandCommand.Create(id,request,replyTo), askTimeout));
    }
    public static Mono<Optional<Brand>> update(String id, BrandRequest request, EntityRef<BrandCommand> ref) {
        return Mono.fromCompletionStage(ref.<Optional<Brand>>ask(replyTo -> new BrandCommand.Update(id,request,replyTo), askTimeout));
    }
    public static Mono<Optional<Brand>> delete(String id, EntityRef<BrandCommand> ref) {
        return Mono.fromCompletionStage(ref.<Optional<Brand>>ask(replyTo -> new BrandCommand.Delete(id,replyTo), askTimeout));
    }
}
