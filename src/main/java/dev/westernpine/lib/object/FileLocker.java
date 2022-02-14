package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileLocker {

    public static boolean isLocked(File file) {
        FileChannel fileChannel = Try.to(() -> new RandomAccessFile(file, "rw")).getUnchecked().getChannel();
        FileLock fileLock = Try.to(() -> fileChannel.tryLock()).orElse(null);
        boolean alreadyLocked = fileLock == null;
        if(!alreadyLocked)
            unlock(fileChannel, fileLock);
        return alreadyLocked;
    }

    public static void unlock(FileChannel fileChannel, FileLock fileLock) {
        Try.to(fileLock::release);
        Try.to(fileChannel::close);
    }

    private final String filePath;
    private FileChannel channel;
    private FileLock lock;

    public FileLocker(String filePath) {
        this.filePath = filePath;
    }

    public File getFile() {
        return new File(filePath);
    }

    public FileLocker lock() {
        channel = Try.to(() -> new RandomAccessFile(getFile(), "rw")).getUnchecked().getChannel();
        lock = Try.to(() -> channel.tryLock()).orElse(null);
        return this;
    }

    public FileLocker lockBlocking() {
        channel = Try.to(() -> new RandomAccessFile(getFile(), "rw")).getUnchecked().getChannel();
        lock = Try.to(() -> channel.lock()).orElse(null);
        return this;
    }

    public boolean isLocked() {
        return FileLocker.isLocked(getFile());
    }

    public FileLocker unlock() {
        FileLocker.unlock(channel, lock);
        return this;
    }

}
