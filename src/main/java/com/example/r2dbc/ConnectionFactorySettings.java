package com.example.r2dbc;

import java.time.Duration;

import com.typesafe.config.Config;

public class ConnectionFactorySettings {

    public final String driver;
    public final String host;
    public final Integer port;
    public final String user;
    public final String password;
    public final String database;
    public final Integer initialSize;
    public final Integer maxSize;
    public final Duration maxIdleTime;
    public final Duration maxLifeTime;
    public final Duration connectTimeout;
    public final Duration acquireTimeout;
    public final Integer acquireRetry;

    public ConnectionFactorySettings(Config configuration){

        Config config = configuration.getConfig("akka.projection.r2dbc.connection-factory");

        driver  = config.getString("driver");
        host = config.getString("host");
        port = config.getInt("port");
        user = config.getString("user");
        password = config.getString("password");
        database = config.getString("database");

        initialSize = config.getInt("initial-size");
        maxSize = config.getInt("max-size");
        maxIdleTime = config.getDuration("max-idle-time");
        maxLifeTime = config.getDuration("max-life-time");

        connectTimeout = config.getDuration("connect-timeout");
        acquireTimeout = config.getDuration("acquire-timeout");
        acquireRetry = config.getInt("acquire-retry");

    }


}
