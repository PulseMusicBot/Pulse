package dev.westernpine.lib.util;

public class Debug {

    public static void printStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 2; i <= trace.length - 1; i++) {
            StackTraceElement element = trace[i];
            System.out.println("%s:%s".formatted(element.getMethodName(), element.getLineNumber()));
        }
    }

}