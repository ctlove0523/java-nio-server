package com.jenkov.nioserver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jjenkov on 18-10-2015.
 */
public class MessageBufferTest {

    @Test
    public void testGetMessage() {

        MessageBuffer messageBuffer = new MessageBuffer();

        Message message = messageBuffer.getMessage();

        assertNotNull(message);
        assertEquals(0       , message.getOffset());
        assertEquals(0       , message.getLength());
        assertEquals(4 * 1024, message.getCapacity());

        Message message2 = messageBuffer.getMessage();

        assertNotNull(message2);
        assertEquals(4096    , message2.getOffset());
        assertEquals(0       , message2.getLength());
        assertEquals(4 * 1024, message2.getCapacity());

        //todo test what happens if the small buffer space is depleted of messages.

    }


    @Test
    public void testExpandMessage(){
        MessageBuffer messageBuffer = new MessageBuffer();

        Message message = messageBuffer.getMessage();

        byte[] smallSharedArray = message.getSharedArray();

        assertNotNull(message);
        assertEquals(0       , message.getOffset());
        assertEquals(0       , message.getLength());
        assertEquals(4 * 1024, message.getCapacity());

        messageBuffer.expandMessage(message);
        assertEquals(0         , message.getOffset());
        assertEquals(0         , message.getLength());
        assertEquals(128 * 1024, message.getCapacity());

        byte[] mediumSharedArray = message.getSharedArray();
        assertNotSame(smallSharedArray, mediumSharedArray);

        messageBuffer.expandMessage(message);
        assertEquals(0          , message.getOffset());
        assertEquals(0          , message.getLength());
        assertEquals(1024 * 1024, message.getCapacity());

        byte[] largeSharedArray = message.getSharedArray();
        assertNotSame(smallSharedArray, largeSharedArray);
        assertNotSame(mediumSharedArray, largeSharedArray);

        //next expansion should not be possible.
        assertFalse(messageBuffer.expandMessage(message));
        assertEquals(0          , message.getOffset());
        assertEquals(0          , message.getLength());
        assertEquals(1024 * 1024, message.getCapacity());
        assertSame(message.getSharedArray(), largeSharedArray);



    }
}
