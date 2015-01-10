package de.smart_sense.tracker.app.bluetoothLogger;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.Observer;

/**
 * Created by phil on 11/19/14.
 */
public class ObservableLinkedList<E> extends LinkedList<E> {

    private final LinkedList<Observer> observers = new LinkedList<Observer>();

    public void register(ObservableLinkedList.Observer o) {
        observers.add(o);
    }
    public void unregister(ObservableLinkedList.Observer o)
    {
        observers.remove(o);
    }

    @Override
    public void clear() {
        super.clear();
        fireEvent();
    }

    @Override
    public boolean add(E object) {
        boolean b = super.add(object);
        fireEvent();
        return b;
    }

    @Override
    public boolean remove(Object object) {
        boolean b = super.remove(object);
        fireEvent();
        return b;
    }

    public void fireEvent() {
        for (Observer o : observers)
            o.listChanged();
    }

    public interface Observer {
        public void listChanged();
    }
}