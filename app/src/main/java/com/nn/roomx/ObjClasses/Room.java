package com.nn.roomx.ObjClasses;

/**
 * Created by user on 2017-01-07.
 */

public class Room {

    private String mailboxId;

    public Room(String address) {
        this.mailboxId = address;
    }

    public String getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
    }

}
