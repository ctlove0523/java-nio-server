package com.jenkov.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

/**
 * @author Created by jjenkov on 19-10-2015.
 */
public class SocketAccepter implements Runnable {

    private int tcpPort;

    private Queue<Socket> socketQueue;

    SocketAccepter(int tcpPort, Queue<Socket> socketQueue) {
        this.tcpPort = tcpPort;
        this.socketQueue = socketQueue;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        ServerSocketChannel serverSocket;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(tcpPort));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                SocketChannel socketChannel = serverSocket.accept();

                System.out.println("Socket accepted: " + socketChannel);

                this.socketQueue.add(new Socket(socketChannel));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
