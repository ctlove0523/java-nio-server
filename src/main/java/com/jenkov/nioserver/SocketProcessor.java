package com.jenkov.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by jjenkov on 16-10-2015.
 */
public class SocketProcessor implements Runnable {

    private Queue<Socket> inboundSocketQueue;

    private MessageBuffer readMessageBuffer;
    private IMessageReaderFactory messageReaderFactory;

    private Queue<Message> outboundMessageQueue = new LinkedList<>();

    private Map<Long, Socket> socketMap = new HashMap<>();

    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private Selector readSelector;
    private Selector writeSelector;

    private IMessageProcessor messageProcessor;
    private WriteProxy writeProxy;

    /**
     * start incoming socket ids from 16K - reserve bottom ids for pre-defined sockets (servers).
     */
    private long nextSocketId = 16 * 1024;

    private Set<Socket> emptyToNonEmptySockets = new HashSet<>();
    private Set<Socket> nonEmptyToEmptySockets = new HashSet<>();


    public SocketProcessor(Queue<Socket> inboundSocketQueue, MessageBuffer readMessageBuffer, MessageBuffer writeMessageBuffer, IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor) throws IOException {
        this.inboundSocketQueue = inboundSocketQueue;

        this.readMessageBuffer = readMessageBuffer;
        this.writeProxy = new WriteProxy(writeMessageBuffer, this.outboundMessageQueue);

        this.messageReaderFactory = messageReaderFactory;

        this.messageProcessor = messageProcessor;

        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            try {
                executeCycle();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void executeCycle() throws IOException {
        takeNewSockets();
        readFromSockets();
        writeToSockets();
    }


    private void takeNewSockets() throws IOException {
        Socket newSocket = this.inboundSocketQueue.poll();

        while (newSocket != null) {
            newSocket.setSocketId(this.nextSocketId++);
            newSocket.configureBlocking();

            newSocket.setMessageReader(this.messageReaderFactory.createMessageReader());
            newSocket.getMessageReader().init(this.readMessageBuffer);

            newSocket.setMessageWriter(new MessageWriter());

            this.socketMap.put(newSocket.getSocketId(), newSocket);

            SelectionKey key = newSocket.getSocketChannel().register(this.readSelector, SelectionKey.OP_READ);
            key.attach(newSocket);

            newSocket = this.inboundSocketQueue.poll();
        }
    }

    private void readFromSockets() throws IOException {
        int readReady = this.readSelector.selectNow();

        if (readReady > 0) {
            Set<SelectionKey> selectedKeys = this.readSelector.selectedKeys();
            for (Iterator<SelectionKey> keyIterator = selectedKeys.iterator(); keyIterator.hasNext(); ) {
                SelectionKey key = keyIterator.next();
                readFromSocket(key);
                keyIterator.remove();
            }
            selectedKeys.clear();
        }
    }

    private void readFromSocket(SelectionKey key) throws IOException {
        Socket socket = (Socket) key.attachment();
        socket.getMessageReader().read(socket, this.readByteBuffer);

        List<Message> fullMessages = socket.getMessageReader().getMessages();
        if (fullMessages.size() > 0) {
            for (Message message : fullMessages) {
                message.setSocketId(socket.getSocketId());
                //the message processor will eventually push outgoing messages into an
                // IMessageWriter for this socket.
                this.messageProcessor.process(message, this.writeProxy);
            }
            fullMessages.clear();
        }

        if (socket.isEndOfStreamReached()) {
            System.out.println("Socket closed: " + socket.getSocketId());
            this.socketMap.remove(socket.getSocketId());
            key.attach(null);
            key.cancel();
            key.channel().close();
        }
    }


    private void writeToSockets() throws IOException {

        // Take all new messages from outboundMessageQueue
        takeNewOutboundMessages();

        // Cancel all sockets which have no more data to write.
        cancelEmptySockets();

        // Register all sockets that *have* data and which are not yet registered.
        registerNonEmptySockets();

        // Select from the Selector.
        int writeReady = this.writeSelector.selectNow();

        if (writeReady > 0) {
            Set<SelectionKey> selectionKeys = this.writeSelector.selectedKeys();
            for (Iterator<SelectionKey> keyIterator = selectionKeys.iterator();keyIterator.hasNext();) {
                SelectionKey key = keyIterator.next();
                Socket socket = (Socket) key.attachment();
                socket.getMessageWriter().write(socket, this.writeByteBuffer);
                if (socket.getMessageWriter().isEmpty()) {
                    this.nonEmptyToEmptySockets.add(socket);
                }
                keyIterator.remove();
            }
            selectionKeys.clear();
        }
    }

    private void registerNonEmptySockets() throws ClosedChannelException {
        for (Socket socket : emptyToNonEmptySockets) {
            socket.getSocketChannel().register(this.writeSelector, SelectionKey.OP_WRITE, socket);
        }
        emptyToNonEmptySockets.clear();
    }

    private void cancelEmptySockets() {
        for (Socket socket : nonEmptyToEmptySockets) {
            SelectionKey key = socket.getSocketChannel().keyFor(this.writeSelector);

            key.cancel();
        }
        nonEmptyToEmptySockets.clear();
    }

    private void takeNewOutboundMessages() {
        Message outMessage = this.outboundMessageQueue.poll();
        while (outMessage != null) {
            Socket socket = this.socketMap.get(outMessage.getSocketId());

            if (socket != null) {
                MessageWriter messageWriter = socket.getMessageWriter();
                if (messageWriter.isEmpty()) {
                    messageWriter.enqueue(outMessage);
                    nonEmptyToEmptySockets.remove(socket);
                    //not necessary if removed from nonEmptyToEmptySockets in prev. statement.
                    emptyToNonEmptySockets.add(socket);
                } else {
                    messageWriter.enqueue(outMessage);
                }
            }

            outMessage = this.outboundMessageQueue.poll();
        }
    }

}
