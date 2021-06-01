package com.example;

public class HelloCJni {

    // C-function interface
    public static native String hello(String input);
    public static native String getArch();
    public static native int calculate(int x, int y);
    public static native int crash();

    // load library
    static {
        System.loadLibrary("helloc");
    }
}
