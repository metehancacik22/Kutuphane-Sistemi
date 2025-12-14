package model;

public class BorrowedState implements BookState {
    public String getStatusName() { return "TÜKENDİ"; }
    public boolean canBorrow() { return false; }
}