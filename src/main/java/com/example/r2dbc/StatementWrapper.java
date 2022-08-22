package com.example.r2dbc;

import io.r2dbc.spi.Statement;

import java.util.Objects;

public class StatementWrapper {
    private Statement stmt;
    public StatementWrapper(Statement stmt){
        this.stmt = stmt;
    }
    private void bindNullOrValue(int index, Object object, Class<?> type){
        if (Objects.nonNull(object)) {
            this.stmt.bind(index, object);
        } else {
            this.stmt.bindNull(index, type);
        }
    }
    public void setString(int index, Object object){
        bindNullOrValue(index, object, String.class);
    }
    public void setBoolean(int index, Object object){
        bindNullOrValue(index, object, Boolean.class);
    }
    public void setInt(int index, Object object){
        bindNullOrValue(index, object, Integer.class);
    }
    public void setDouble(int index, Object object){
        bindNullOrValue(index, object, Integer.class);
    }
    public void setTimestamp(int index, Object object){
        bindNullOrValue(index, object, java.sql.Timestamp.class);
    }
    public Statement getStatement(){
        return this.stmt;
    }
}
//$1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20