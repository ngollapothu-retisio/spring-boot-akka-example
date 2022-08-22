package com.example.r2dbc;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;

@Component
public class R2dbcConnectionPool {
	
	@Autowired
    private ConnectionPool connectionPool;

    public Publisher<Connection> getConnection(){
        return connectionPool.create();
    }
}
