package com.example.config;

import static io.r2dbc.spi.ConnectionFactoryOptions.CONNECT_TIMEOUT;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.r2dbc.ConnectionFactorySettings;
import com.typesafe.config.Config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class R2dbcConfig {

	@Autowired
	private Config config;
	
	@Bean
	public ConnectionFactorySettings connectionFactorySettings(){
		log.info("ConnectionFactorySettings init....");
		return new ConnectionFactorySettings(config);
	}
	@Bean
	public ConnectionPool connectionPool(){
		ConnectionFactorySettings settings = connectionFactorySettings();
		ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER,settings.driver)
                .option(HOST,settings.host)
                .option(PORT,settings.port)
                .option(USER,settings.user)
                .option(PASSWORD,settings.password)
                .option(DATABASE,settings.database)
                .option(CONNECT_TIMEOUT, settings.connectTimeout)
                .build());

        ConnectionPoolConfiguration connectionPoolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
                .initialSize(settings.initialSize)
                .maxSize(settings.maxSize)
                .maxAcquireTime(settings.acquireTimeout)
                .acquireRetry(settings.acquireRetry)
                .maxIdleTime(settings.maxIdleTime)
                .maxLifeTime(settings.maxLifeTime)
                .backgroundEvictionInterval(Duration.ofMinutes(10))
                .build();
        
        ConnectionPool connectionPool = new ConnectionPool(connectionPoolConfiguration);
        connectionPool.warmup().subscribe();
        return connectionPool;
	}
	
	
}
