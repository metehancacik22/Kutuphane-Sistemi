package logic;

import java.util.ArrayList;
import java.util.List;

public class NotificationSystem {
    private List<Observer> observers = new ArrayList<>();
    public void addObserver(Observer o) { observers.add(o); }
    public void notifyAll(String msg) { for(Observer o : observers) o.update(msg); }
}