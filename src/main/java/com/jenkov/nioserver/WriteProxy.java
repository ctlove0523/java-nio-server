package com.jenkov.nioserver;

import java.util.Queue;

/**
 * @author Created by jjenkov on 22-10-2015.
 */
public class WriteProxy {

    private MessageBuffer messageBuffer;
    private Queue<Message> writeQueue;

    WriteProxy(MessageBuffer messageBuffer, Queue<Message> writeQueue) {
        this.messageBuffer = messageBuffer;
        this.writeQueue = writeQueue;
    }

    public Message getMessage() {
        return this.messageBuffer.getMessage();
    }

    public void enqueue(Message message) {
        this.writeQueue.offer(message);
    }

}
