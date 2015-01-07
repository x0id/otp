package com.ericsson.otp.erlang.example;

public class Counter {

    int count = 0;

    public void inc() {
        count++;
    }

    public int get() {
        return count;
    }

}
