package com.gikk.clock.types;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Gikkman
 */
public final class Observable<T> {
    private T value = null;
    private final T emptyValue;
    private final Set<ChangeListener<T>> changeListeners = new HashSet<>();

    public Observable(T emptyValue){
        this.emptyValue = emptyValue;
    }

    /**
     * Adds a new listener and, if this Observable is set, will update the
     * listener with the value. If the value isn't set, the listener will be
     * updated as soon as the value is set.
     *
     * @param listener the listener to add
     */
    public synchronized void addListener(ChangeListener<T> listener){
        if( value != null ) {
            listener.onChange(value);
        }

        changeListeners.add(listener);
    }

    /**
     * Removes a listener from this Observable.
     *
     * @param listener
     */
    public synchronized void removeListener(ChangeListener<T> listener){
        changeListeners.remove(listener);
    }

    /**
     * Updates the value of this observable, and notifies all listeners of the
     * change.
     *
     * @param listener the listener to add
     */
    public synchronized void update(T value){
        if(value == null){
            delete();
            return;
        }

        this.value = value;
        for( ChangeListener<T> cl : changeListeners ){
            cl.onChange(value);
        }
    }

    /**
     * Clears the value from this observable, and notifies all listeners of this
     * removal. If delete is called when no data is set, nothing will occur.
     */
    public synchronized void delete(){
        if(this.value == null) {
            return;
        }

        this.value = null;
        for(ChangeListener<T> cl : changeListeners){
            cl.onChange(emptyValue);
        }
    }

    /**
     * Reads the value from this observable.
     *
     * @return an Optional of the value
     */
    public synchronized T read(){
        if(value == null) {
            return emptyValue;
        }

        return value;
    }
}
