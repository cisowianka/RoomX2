package com.nn.roomx.ObjClasses;

public class Person {

    private String ID;
    private String fullNname;

    public Person(String ID) {
        this.ID = ID;
    }

    public Person(String ID, String name) {
        this.ID = ID;
        this.fullNname = name;
    }

    public Person() {

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return fullNname;
    }

    public void setName(String name) {
        this.fullNname = name;


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (!ID.equals(person.ID)) return false;
        return fullNname.equals(person.fullNname);

    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 31 * result + fullNname.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
                "ID='" + ID + '\'' +
                ", fullNname='" + fullNname + '\'' +
                '}';
    }
}
