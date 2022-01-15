package dev.westernpine.lib.object;

import java.io.*;

public class LoggingPrintStream extends PrintStream {

    public static LoggingPrintStream initialize(File logFile) throws FileNotFoundException {
        FileOutputStream fileOutput = new FileOutputStream(logFile);
        LoggingPrintStream printer = new LoggingPrintStream(fileOutput, System.out);
        System.setOut(printer);
        System.setErr(printer);
        return printer;
    }
	
    private final PrintStream second;

    public LoggingPrintStream(OutputStream main, PrintStream second) {
        super(main);
        this.second = second;
    }

    /**
     * Closes the main stream. 
     * The second stream is just flushed but <b>not</b> closed.
     * @see PrintStream#close()
     */
    @Override
    public void close() {
        // just for documentation
        super.close();
    }

    @Override
    public void flush() {
        super.flush();
        second.flush();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        second.write(buf, off, len);
    }

    @Override
    public void write(int b) {
        super.write(b);
        second.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        second.write(b);
    }
}