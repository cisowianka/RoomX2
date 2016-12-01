package com.nn.roomx.ObjClasses;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class Appointment {

    private String ID;
    private String subject;
    private Date start;
    private Date end;
    private Person owner;
    private ArrayList<Person> attendees;
    public static ArrayList<Appointment> appointmentsExList = new ArrayList<>();

    public Appointment(String ID, String subject, Date start, Date end, Person owner, ArrayList<Person> attendees) {
        this.ID = ID;
        this.subject = subject;
        this.start = start;
        this.end = end;
        this.owner = owner;
        this.attendees = attendees;
        appointmentsExList.add(this);
    }

    public Appointment() {
        appointmentsExList.add(this);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public ArrayList<Person> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<Person> attendees) {
        this.attendees = attendees;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "ID='" + ID.substring(0,5) + '\'' +
                ", subject='" + subject + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", owner=" + owner +
                ", attendees=" + attendees +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Appointment that = (Appointment) o;

        if (!ID.equals(that.ID)) return false;
        if (!subject.equals(that.subject)) return false;
        if (!start.equals(that.start)) return false;
        if (!end.equals(that.end)) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        return attendees != null ? attendees.equals(that.attendees) : that.attendees == null;

    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (attendees != null ? attendees.hashCode() : 0);
        return result;
    }
}
