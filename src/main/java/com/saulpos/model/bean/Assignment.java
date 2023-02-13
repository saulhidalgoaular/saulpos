package com.saulpos.model.bean;

import java.util.Date;

public class Assignment {
    private int id;
    private String shift_id;
    private String cashier_id;
    private Date datetime;
    private int status;

    public Assignment(int id, String shift_id, String cashier_id, Date datetime, int status) {
        this.id = id;
        this.shift_id = shift_id;
        this.cashier_id = cashier_id;
        this.datetime = datetime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShift_id() {
        return shift_id;
    }

    public void setShift_id(String shift_id) {
        this.shift_id = shift_id;
    }

    public String getCashier_id() {
        return cashier_id;
    }

    public void setCashier_id(String cashier_id) {
        this.cashier_id = cashier_id;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
