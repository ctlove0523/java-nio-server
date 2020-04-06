package com.jenkov.nioserver;

import java.nio.ByteBuffer;

/**
 * Created by jjenkov on 16-10-2015.
 */
public class Message {

    private MessageBuffer messageBuffer;

    /**
     * the id of source socket or destination socket, depending on whether is going in or out.
     */
    private long socketId = 0L;

    private byte[] sharedArray;

    /**
     * offset into sharedArray where this message data starts.
     */
    private int offset = 0;

    /**
     * the size of the section in the sharedArray allocated to this message.
     */
    private int capacity = 0;

    /**
     * the number of bytes used of the allocated section.
     */
    private int length = 0;

    private Object metaData ;

    public Message(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    /**
     * Writes data from the ByteBuffer into this message - meaning into the buffer backing this message.
     *
     * @param byteBuffer The ByteBuffer containing the message data to write.
     * @return
     */
    public int writeToMessage(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();

        while (this.length + remaining > capacity) {
            if (!this.messageBuffer.expandMessage(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(remaining, this.capacity - this.length);
        byteBuffer.get(this.sharedArray, this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;

        return bytesToCopy;
    }


    /**
     * Writes data from the byte array into this message - meaning into the buffer backing this message.
     *
     * @param byteArray The byte array containing the message data to write.
     * @return
     */
    public int writeToMessage(byte[] byteArray) {
        return writeToMessage(byteArray, 0, byteArray.length);
    }


    /**
     * Writes data from the byte array into this message - meaning into the buffer backing this message.
     *
     * @param byteArray The byte array containing the message data to write.
     * @return
     */
    public int writeToMessage(byte[] byteArray, int offset, int length) {

        while (this.length + length > capacity) {
            if (!this.messageBuffer.expandMessage(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(length, this.capacity - this.length);
        System.arraycopy(byteArray, offset, this.sharedArray, this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;
        return bytesToCopy;
    }


    /**
     * In case the buffer backing the nextMessage contains more than one HTTP message, move all data after the first
     * message to a new Message object.
     *
     * @param message  The message containing the partial message (after the first message).
     * @param endIndex The end index of the first message in the buffer of the message given as parameter.
     */
    public void writePartialMessageToMessage(Message message, int endIndex) {
        int startIndexOfPartialMessage = message.offset + endIndex;
        int lengthOfPartialMessage = (message.offset + message.length) - endIndex;

        System.arraycopy(message.sharedArray, startIndexOfPartialMessage, this.sharedArray, this.offset, lengthOfPartialMessage);
    }


    public long getSocketId() {
        return socketId;
    }

    public void setSocketId(long socketId) {
        this.socketId = socketId;
    }

    public byte[] getSharedArray() {
        return sharedArray;
    }

    void setSharedArray(byte[] sharedArray) {
        this.sharedArray = sharedArray;
    }

    public int getOffset() {
        return offset;
    }

    void setOffset(int offset) {
        this.offset = offset;
    }

    int getCapacity() {
        return capacity;
    }

    void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getLength() {
        return length;
    }

    void setLength(int length) {
        this.length = length;
    }

    public Object getMetaData() {
        return metaData;
    }

    public void setMetaData(Object metaData) {
        this.metaData = metaData;
    }
}
