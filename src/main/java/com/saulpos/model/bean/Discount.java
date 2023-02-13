package com.saulpos.model.bean;


public class Discount {
    private int id;
    private String description;
    private String starting_date;
    private String ending_date;
    private double percentage;

    public Discount(int id, String description, String starting_date, String ending_date, double percentage) {
        this.id = id;
        this.description = description;
        this.starting_date = starting_date;
        this.ending_date = ending_date;
        this.percentage = percentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStarting_date() {
        return starting_date;
    }

    public void setStarting_date(String starting_date) {
        this.starting_date = starting_date;
    }

    public String getEnding_date() {
        return ending_date;
    }

    public void setEnding_date(String ending_date) {
        this.ending_date = ending_date;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
