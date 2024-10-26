package com.proxtextchat.util;


public class Tuple<X, Y> {
    public final X one;
    public final Y two;

    public Tuple(X first, Y second) {
        this.one = first;
        this.two = second;
    }

    @Override
    public String toString() {
        return "(" + one + ", " + two + ")";
    }
}

