package com.example.repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.entity.Brand;
import com.example.r2dbc.R2dbcConnectionPool;
import com.example.r2dbc.StatementWrapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class BrandRepository {

	@Autowired
    private R2dbcConnectionPool connectionPool;

    public Mono<List<String>> getBrandNames(){
        return Mono.usingWhen(connectionPool.getConnection(),
                connection -> {
                    log.info("Connection:::{}", connection);
                    return Flux.from(connection.createStatement("SELECT name FROM brand").execute())
                            .flatMap(result -> result.map(row -> row.get("name", String.class)))
                            .collectList();
                },
                connection -> connection.close());
    }
    
    public Mono<Brand> getBrandById(String entityId){
        return Mono.usingWhen(connectionPool.getConnection(),
                connection -> {
                    log.info("Connection:::{}", connection);
                    AtomicInteger index = new AtomicInteger(-1);
                    StatementWrapper ps = new StatementWrapper(connection.createStatement("SELECT id, name, active, logo FROM brand where id = $1 limit 1"));
                    ps.setString(index.incrementAndGet(), entityId);
                    return Flux.from(ps.getStatement().execute())
                            .flatMap(result -> result.map(row -> {
                            	return new Brand(
                            			row.get("id", String.class), 
                            			row.get("name", String.class), 
                            			row.get("active", Boolean.class), 
                            			row.get("logo", String.class)
                            			);
                            }))
                            .last();
                },
                connection -> connection.close());
    }

}
