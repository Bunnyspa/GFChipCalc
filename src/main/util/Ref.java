package main.util;

public class Ref<E> {

    public E v;

    public Ref(E initial) {
        v = initial;
    }
    
    @Override
    public String toString(){
        return v.toString();
    }
}
