package model;

public class AvailableState implements BookState {
    public String getStatusName() { return "MEVCUT"; }
    public boolean canBorrow() { return true; }
}