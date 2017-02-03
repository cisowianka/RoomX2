package com.nn.roomx.ObjClasses;

import java.util.List;

/**
 * Created by user on 2017-01-02.
 */

public class ServiceResponse<V> {

    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }

    private enum Status {
        OK,
        FAIL
    }

    private V responseObject;
    private List<Event> events;
    private List<SystemProperty> properties;
    private Status status;
    private String message;


    public V getResponseObject() {

        return responseObject;
    }

    public void setResponseObject(V responseObject) {
        this.responseObject = responseObject;
    }


    public Status getStatus() {
        return status;
    }

    public void ok() {
        this.status = Status.OK;
    }

    public void fail() {
        this.status = Status.FAIL;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOK() {
        return this.status.equals(Status.OK);
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<SystemProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<SystemProperty> properties) {
        this.properties = properties;
    }
}
