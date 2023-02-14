package com.saulpos.model.bean;

import jakarta.persistence.*;


@Entity
@Access(AccessType.PROPERTY)
@Table(name = "discount")
public class Discount {
    private int id;


    @Column(name = "description")
    private String description;

    @Column(name = "starting_date")
    private String startingDate;

    @Column(name = "ending_date")
    private String endingDate;

    @Column(name = "percentage")
    private double percentage;

    public Discount() {

    }

    @Id @GeneratedValue
    @Column(name = "id")
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

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String starting_date) {
        this.startingDate = starting_date;
    }

    public String getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(String endingDate) {
        this.endingDate = endingDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
