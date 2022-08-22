package com.example.request;

import com.example.serializer.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Value;

@Value
@JsonDeserialize
public class BrandRequest implements JsonSerializable {
	public String id;
	public String name;
	public Boolean active;
	public String logo;

    @JsonCreator
	public BrandRequest(String id, String name, Boolean active, String logo) {
		super();
		this.id = id;
		this.name = name;
		this.active = active;
		this.logo = logo;
	}
}
