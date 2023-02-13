package com.saulpos.model.bean;

public class Configuration {
    private int id;
    private String key;
    private String value;
    private String name;

    public Configuration(int id, String key, String value, String name) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
