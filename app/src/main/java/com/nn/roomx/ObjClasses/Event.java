package com.nn.roomx.ObjClasses;

/**
 * Created by user on 2017-01-07.
 */

public class Event {

    private Integer id;
    private String roomId;
    private String name;
    private String value;
    private Boolean applied;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getApplied() {
        return applied;
    }

    public void setApplied(Boolean applied) {
        this.applied = applied;
    }

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", roomId='" + roomId + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", applied=" + applied +
                '}';
    }
}
