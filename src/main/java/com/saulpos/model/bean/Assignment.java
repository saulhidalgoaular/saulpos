package com.saulpos.model.bean;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name="assignment")
public class Assignment {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "shift_id")
    private String shiftId;

    @NotNull
    @Column(name = "cashier_id")
    private String cashierId;

    @NotNull
    @Column(name = "datetime")
    private Date dateTime;

    //todo check the data type
    @NotNull
    @Column(name = "status")
    private int status;

    public Assignment(int id, String shiftId, String cashierId, Date dateTime, int status) {
        this.id = id;
        this.shiftId = shiftId;
        this.cashierId = cashierId;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
