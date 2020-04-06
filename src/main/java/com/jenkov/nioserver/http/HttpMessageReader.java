package com.jenkov.nioserver.http;

import com.jenkov.nioserver.IMessageReader;
import com.jenkov.nioserver.Message;
import com.jenkov.nioserver.MessageBuffer;
import com.jenkov.nioserver.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by jjenkov on 18-10-2015.
 */
public class HttpMessageReader implements IMessageReader {

    private MessageBuffer messageBuffer = null;

    private List<Message> completeMessages = new ArrayList<>();
    private Message nextMessage = null;

    @Override
    public void init(MessageBuffer readMessageBuffer) {
        this.messageBuffer = readMessageBuffer;
        this.nextMessage = messageBuffer.getMessage();
        this.nextMessage.setMetaData(new HttpHeaders());
    }

    @Override
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        socket.read(byteBuffer);
        byteBuffer.flip();

        if (byteBuffer.remaining() == 0) {
            byteBuffer.clear();
            return;
        }

        this.nextMessage.writeToMessage(byteBuffer);

        int endIndex = HttpUtil.parseHttpRequest(this.nextMessage.getSharedArray(), this.nextMessage.getOffset(), this.nextMessage.getOffset() + this.nextMessage.getLength(), (HttpHeaders) this.nextMessage.getMetaData());
        if (endIndex != -1) {
            Message message = this.messageBuffer.getMessage();
            message.setMetaData(new HttpHeaders());

            message.writePartialMessageToMessage(nextMessage, endIndex);

            completeMessages.add(nextMessage);
            nextMessage = message;
        }
        byteBuffer.clear();
    }


    @Override
    public List<Message> getMessages() {
        return this.completeMessages;
    }

}
