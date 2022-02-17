package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class SessionLocker {

    private static final InetAddress localhost = Try.to(() -> InetAddress.getByAddress(new byte[]{127, 0, 0, 1})).getUnchecked();

    private final int port;
    private ServerSocket serverSocket;

    public SessionLocker(int port) {
        this.port = port;
    }

    public boolean lockExists() {
        return serverSocket != null || Try.to(() -> new Socket(localhost, port).close()).map(success -> true).orElse(false);
    }

    public boolean holdsLock() {
        return serverSocket != null;
    }

    public boolean lock() {
        Try<ServerSocket> serverSocketTry = Try.to(() -> new ServerSocket(port, 0, localhost));
        if (serverSocketTry.isSuccessful()) {
            this.serverSocket = serverSocketTry.getUnchecked();
            return true;
        }

        if (serverSocketTry.getFailureCause() instanceof BindException) {
            return false;
        }

        throw new RuntimeException(serverSocketTry.getFailureCause());
    }

    public void lockBlocking(long checkInterval, TimeUnit timeUnit) {
        long millis = TimeUnit.MILLISECONDS.convert(checkInterval, timeUnit);
        while (lockExists()) {
            Try.to(() -> Thread.sleep(millis));
        }
        if (!Try.to(this::lock).onFailure(throwable -> {
            throw new RuntimeException(throwable);
        }).orElse(false)) {
            Try.to(() -> Thread.sleep(millis));
            lockBlocking(checkInterval, timeUnit);
        }

    }

    public boolean unlock() {
        if (serverSocket == null)
            return false;

        return Try.to(() -> serverSocket.close()).map(success -> true).getUnchecked();
    }

}
