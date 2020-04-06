package com.jenkov.nioserver;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Created by jjenkov on 24-10-2015.
 */
public class Server {
    private ExecutorService acceptorThread = Executors.newSingleThreadExecutor();
    private ExecutorService workerThread = Executors.newSingleThreadExecutor();

    private int tcpPort;
    private IMessageReaderFactory messageReaderFactory;
    private IMessageProcessor messageProcessor;

    public Server(int tcpPort, IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor) {
        this.tcpPort = tcpPort;
        this.messageReaderFactory = messageReaderFactory;
        this.messageProcessor = messageProcessor;
    }

    public void start() throws IOException {
        Queue<Socket> socketQueue = new ArrayBlockingQueue<>(1024);

        SocketAccepter socketAccepter = new SocketAccepter(tcpPort, socketQueue);

        MessageBuffer readBuffer = new MessageBuffer();
        MessageBuffer writeBuffer = new MessageBuffer();

        SocketProcessor socketProcessor = new SocketProcessor(socketQueue, readBuffer, writeBuffer, this.messageReaderFactory, this.messageProcessor);

        acceptorThread.submit(socketAccepter);
        workerThread.submit(socketProcessor);
    }

}
