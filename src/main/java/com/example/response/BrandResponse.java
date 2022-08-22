package com.example.response;

import lombok.Builder;

@Builder
public class BrandResponse {
	public String id;
	public String name;
	public Boolean active;
	public String logo;
}
