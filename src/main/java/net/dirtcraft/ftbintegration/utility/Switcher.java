package net.dirtcraft.ftbintegration.utility;

public class Switcher<T> {
    boolean val = false;
    final T a;
    final T b;

    public Switcher(T a, T b){
        this.a = a;
        this.b = b;
    }

    public T get(){
        return (val = !val)? a : b;
    }
}