package com.jenkov.nioserver;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by jjenkov on 21-10-2015.
 */
public class SelectorTest {

    @Test
    public void test() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        SocketAddress address = new InetSocketAddress("localhost",5230);
        server.bind(address);

        Selector selector = Selector.open();

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(address);

        socketChannel.configureBlocking(false);

        socketChannel.register(selector, SelectionKey.OP_WRITE);
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.put("test".getBytes());
        socketChannel.write(buffer);

        selector.selectedKeys().forEach(selectionKey -> Assert.assertTrue(selectionKey.isWritable()));

    }


}
