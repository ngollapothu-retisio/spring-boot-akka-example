package com.example.event;


import com.example.request.BrandRequest;
import com.example.serializer.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

public interface BrandEvent extends JsonSerializable {

    public String id();

	@Value
    @JsonDeserialize
    final class Created implements BrandEvent {
        public final String id;
        public final BrandRequest request;
        @JsonCreator
        public Created(String id, BrandRequest request) {
            this.id = id;
            this.request = request;
        }
        @Override
        public String id(){
            return this.id;
        }
    }
	@Value
    @JsonDeserialize
    final class Updated implements BrandEvent {
        public final String id;
        public final BrandRequest request;
        @JsonCreator
        public Updated(String id, BrandRequest request) {
            this.id = id;
            this.request = request;
        }
        @Override
        public String id(){
            return this.id;
        }
    }
	@Value
    @JsonDeserialize
    final class Deleted implements BrandEvent {
        public final String id;
        @JsonCreator
        public Deleted(String id) {
            this.id = id;
        }
        @Override
        public String id(){
            return this.id;
        }
    }
}
