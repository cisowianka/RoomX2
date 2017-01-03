package com.nn.roomx.ObjClasses;

/**
 * Created by user on 2017-01-02.
 */

public class ServiceResponse<V> {

    private enum Status {
        OK,
        FAIL
    }

    private V responseObject;
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
}
