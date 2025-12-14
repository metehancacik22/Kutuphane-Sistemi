package model;

//STATE tasarım deseni

public interface BookState {
    String getStatusName();
    boolean canBorrow();
}