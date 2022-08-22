package com.example.state;

import com.example.request.BrandRequest;
import com.example.serializer.JsonSerializable;
import com.example.entity.Brand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@JsonDeserialize
@Slf4j
public class BrandState implements JsonSerializable {
    public static final BrandState EMPTY = new BrandState(Optional.<Brand>empty());

    public final Optional<Brand> brand;

    @JsonCreator
    public BrandState(Optional<Brand> brand) {
        this.brand = brand;
    }

    public BrandState create(String placeholder, BrandRequest request) {
    	log.info("placeholder:: {}, state create::{}", placeholder, request.id);
        return new BrandState(
            Optional.of(
                    new Brand(request.id, request.name, request.active, request.logo)
            )
        );
    }
    public BrandState update(String placeholder, BrandRequest request) {
    	log.info("placeholder:: {}, state update::{}", placeholder, request.id);
        return new BrandState(
            Optional.of(
            		new Brand(request.id, request.name, request.active, request.logo)
            )
        );
    }
    public BrandState delete(String placeholder, String id) {
    	log.info("placeholder:: {}, state update::{}", placeholder, id);
        return EMPTY;
    }
    public BrandState get(String placeholder, String id) {
    	log.info("placeholder:: {}, state get::{}", placeholder, id);
        return this;
    }
}
