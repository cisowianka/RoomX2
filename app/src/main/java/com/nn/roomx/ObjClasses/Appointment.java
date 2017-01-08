package com.nn.roomx.ObjClasses;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class Appointment {

    private static final String TAG = "RoomX_Appointment";

    private String ID;
    private String subject;
    private Date start;
    private Date end;
    private Person owner;
    private ArrayList<Person> attendees;
    private boolean isConfirmed = false;
    public static ArrayList<Appointment> appointmentsExList = new ArrayList<>();
    private boolean virtual = false;

    public Appointment(String ID, String subject, Date start, Date end, Person owner, ArrayList<Person> attendees, boolean confirmed) {
        this.ID = ID;
        this.subject = subject;
        this.start = start;
        this.end = end;
        this.owner = owner;
        this.attendees = attendees;
        setConfirmed(confirmed);
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
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        if (getID() == null) {
            return subject;
        }

        return formatter.format(start) + " - " + formatter.format(end) + " " + subject;
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

    public static Appointment getCurrentAppointment() {
        Appointment result = null;
        Log.e("getCurrentAppointment", "APPoinment siz " + appointmentsExList.size());
        if (appointmentsExList.size() > 0) {
            Date now = new Date();
            for (Appointment a : appointmentsExList) {
                if (a.isVirtual()) {
                    continue;
                }
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //  Log.d("getCurrentAppointment", "APP " + formatter.format(a.getStart()) + " " + formatter.format(a.getEnd()) + " " + now.after(a.getStart()) + " " +  a.getEnd().after(now));
                Log.d("getCurrentAppointment", "CURRENT " + a.getSubject() + " " + a.isVirtual());
                if (now.after(a.getStart()) && a.getEnd().after(now)) {
                    result = a;
                    return result;
                }
            }
            return result;
        } else {
            return null;
        }
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean isAvailableForCancel(int cancelMinuteShift) {

        Calendar appCal = Calendar.getInstance();
        appCal.setTime(getStart());
        appCal.add(Calendar.MINUTE, cancelMinuteShift);

        Log.i(TAG, getStart() + " ***** " + appCal.getTime() + " @@@@@@ " + new Date());

        return new Date().after(appCal.getTime());

    }

    public int getCancelWarningMinutes(int waringMinuteShift, int cancelMinuteShift) {
        Calendar appCalWarning = Calendar.getInstance();
        appCalWarning.setTime(getStart());
        appCalWarning.add(Calendar.MINUTE, waringMinuteShift);

        Calendar appCalCancel = Calendar.getInstance();
        appCalCancel.setTime(getStart());
        appCalCancel.add(Calendar.MINUTE, cancelMinuteShift);

        Log.i(TAG, getStart() + " ***** " + appCalWarning.getTime() + " @@@@@@ " + new Date());

        if (new Date().after(appCalWarning.getTime())) {
            return (int) ((appCalCancel.getTime().getTime() - new Date().getTime())/ (60 * 1000) % 60) + 1;
        } else {
            return -1;
        }
    }
}
