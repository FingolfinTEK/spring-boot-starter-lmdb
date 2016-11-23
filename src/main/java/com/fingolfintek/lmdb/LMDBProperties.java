package com.fingolfintek.lmdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties("lmdb")
public class LMDBProperties {

    private String dbPath;

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public File getDbPathAsFile() {
        return new File(dbPath);
    }

}
