package com.jenkov.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * @author Created by jjenkov on 16-10-2015.
 */
public class Socket {
    private long socketId;
    private SocketChannel socketChannel;
    private IMessageReader messageReader;
    private MessageWriter messageWriter;

    private volatile boolean endOfStreamReached = false;

    public Socket() {
    }

    public Socket(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    /**
     * 循环从socket channel 中读取数据
     */
    public int read(ByteBuffer byteBuffer) throws IOException {
        int bytesRead = this.socketChannel.read(byteBuffer);
        int totalBytesRead = bytesRead;

        while (bytesRead > 0) {
            bytesRead = this.socketChannel.read(byteBuffer);
            totalBytesRead += bytesRead;
        }

        if (bytesRead == -1) {
            this.endOfStreamReached = true;
        }

        return totalBytesRead;
    }

    /**
     * 循环写入socket channel
     */
    int write(ByteBuffer byteBuffer) throws IOException {
        int bytesWritten = this.socketChannel.write(byteBuffer);
        int totalBytesWritten = bytesWritten;

        while (bytesWritten > 0 && byteBuffer.hasRemaining()) {
            bytesWritten = this.socketChannel.write(byteBuffer);
            totalBytesWritten += bytesWritten;
        }

        return totalBytesWritten;
    }

    void configureBlocking() throws IOException {
        socketChannel.configureBlocking(false);
    }

    long getSocketId() {
        return socketId;
    }

    void setSocketId(long socketId) {
        this.socketId = socketId;
    }

    SocketChannel getSocketChannel() {
        return socketChannel;
    }

    IMessageReader getMessageReader() {
        return messageReader;
    }

    void setMessageReader(IMessageReader messageReader) {
        this.messageReader = messageReader;
    }

    MessageWriter getMessageWriter() {
        return messageWriter;
    }

    void setMessageWriter(MessageWriter messageWriter) {
        this.messageWriter = messageWriter;
    }

    boolean isEndOfStreamReached() {
        return endOfStreamReached;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Socket)) {
            return false;
        }
        Socket socket = (Socket) o;
        return socketId == socket.socketId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(socketId);
    }
}
