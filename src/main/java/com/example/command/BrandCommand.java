package com.example.command;

import java.util.Optional;

import com.example.entity.Brand;
import com.example.request.BrandRequest;
import com.example.serializer.JsonSerializable;

import akka.actor.typed.ActorRef;
import lombok.Value;

public interface BrandCommand extends JsonSerializable {
	@Value
    final class Get implements BrandCommand {
		public final String id;
        public final ActorRef<Optional<Brand>> replyTo;
    }

	@Value
    final class Create implements BrandCommand {
        public final String id;
        public final BrandRequest request;
        public final ActorRef<Optional<Brand>> replyTo;
    }
	@Value
    final class Update implements BrandCommand {
        public final String id;
        public final BrandRequest request;
        public final ActorRef<Optional<Brand>> replyTo;
    }
	@Value
    final class Delete implements BrandCommand {
        public final String id;
        public final ActorRef<Optional<Brand>> replyTo;
    }
}
