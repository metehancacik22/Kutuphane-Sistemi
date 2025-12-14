package model;

//Template tasarım deseni kod tekrarını azalttım

public abstract class BaseEntity {
    protected int id;
    public int getId() {return id; }
    public void setId(int id) { this.id = id; }
}