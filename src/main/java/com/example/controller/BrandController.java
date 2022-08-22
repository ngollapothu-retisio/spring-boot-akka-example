package com.example.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aggregate.BrandAggregate;
import com.example.command.BrandCommand;
import com.example.repository.BrandRepository;
import com.example.request.BrandRequest;
import com.example.response.BrandResponse;
import com.example.util.BrandServiceUtil;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/brands")
@Slf4j
public class BrandController {
	
	@Autowired
	private ClusterSharding clusterSharding;
	
	@Autowired
	private BrandRepository brandRepository;
	
	@GetMapping("ping")
	public Mono<String> ping() {
		log.info("ping");
		return Mono.just(LocalDateTime.now()+" - Ok");
	}
	
	public EntityRef<BrandCommand> ref(String id) {
		return clusterSharding.entityRefFor(BrandAggregate.ENTITY_TYPE_KEY,id);
    }

	@PostMapping
	public Mono<BrandResponse> create(@RequestBody BrandRequest request) {

		String entityId = Optional.ofNullable(request.id).orElseGet(() -> UUID.randomUUID().toString());

		return BrandServiceUtil.create(entityId, request, ref(entityId))
				.map(brand -> {
					if (brand.isPresent()) {
						return BrandResponse.builder()
								.id(brand.get().id)
								.name(brand.get().name)
								.active(brand.get().active)
								.logo(brand.get().logo)
								.build();
					}
					return BrandResponse.builder()
							.build();
				});

	}

	@PutMapping
    public Mono<BrandResponse> update(@RequestBody BrandRequest request) {
    	String entityId = Optional.ofNullable(request.id).orElseGet(() -> UUID.randomUUID().toString());

		return BrandServiceUtil.update(entityId, request, ref(entityId))
				.map(brand -> {
					if (brand.isPresent()) {
						return BrandResponse.builder()
								.id(brand.get().id)
								.name(brand.get().name)
								.active(brand.get().active)
								.logo(brand.get().logo)
								.build();
					}
					return BrandResponse.builder()
							.build();
				});
        
    }

	@DeleteMapping("{id}")
    public Mono<BrandResponse> delete(@PathVariable String id) {
        return BrandServiceUtil.delete(id, ref(id))
                .map(
                        brand -> {
                            if(brand.isPresent()){
                            	return BrandResponse.builder()
        								.id(brand.get().id)
        								.name(brand.get().name)
        								.active(brand.get().active)
        								.logo(brand.get().logo)
        								.build();
                            }
                            return BrandResponse.builder()
                                    .build();
                        }
                );
    }
	
	@GetMapping("{id}")
    public Mono<BrandResponse> getById(@PathVariable String id) {
        return BrandServiceUtil.get(id, ref(id))
                .flatMap(
                        brand -> {
                            if(brand.isPresent()){
                            	return Mono.just(BrandResponse.builder()
        								.id(brand.get().id)
        								.name(brand.get().name)
        								.active(brand.get().active)
        								.logo(brand.get().logo)
        								.build());
                            }
                            return brandRepository.getBrandById(id)
                                	.map(b -> BrandResponse.builder()
            								.id(b.id)
            								.name(b.name)
            								.active(b.active)
            								.logo(b.logo)
            								.build())
                                	.switchIfEmpty(Mono.just(BrandResponse.builder()
                                            .build()));
                        }
                );
    }
	@GetMapping
    public Mono<List<String>> getBrandNames() {
		return brandRepository.getBrandNames();
    }
}
