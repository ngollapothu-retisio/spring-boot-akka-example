package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.aggregate.BrandAggregate;
import com.example.projection.BrandProjection;
import com.typesafe.config.Config;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AkkaConfig {

	@Value("${akka.actor.system.name:ApplicationSystem}")
	private String actorSystemName;
	
	@Bean
	public ActorSystem<Void> system(){
		log.info("ActorSystem init...., actorSystemName::{}", actorSystemName);
		return ActorSystem.create(Behaviors.empty(), actorSystemName);
	}
	
	@Bean
	public ClusterSharding clusterSharding(){
		log.info("clusterSharding init...", actorSystemName);
		return ClusterSharding.get(system());
	}
	
	@Bean
	public Config config(){
		return system().settings().config();
	}	
	
	@Bean
	public String akkaManagementInitiazer(){
		AkkaManagement.get(system()).start();
		ClusterBootstrap.get(system()).start();
		
		BrandAggregate.init(clusterSharding());
		BrandProjection.init(system(), clusterSharding());
		return "init";
	}
	
	
}
