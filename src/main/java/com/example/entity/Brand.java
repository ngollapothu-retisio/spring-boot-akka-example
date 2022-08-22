package com.example.entity;

import com.example.serializer.JsonSerializable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Value;

@Value
@JsonDeserialize
public class Brand implements JsonSerializable {
	public String id;
	public String name;
	public Boolean active;
	public String logo;
}
