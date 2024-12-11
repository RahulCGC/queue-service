package com.example;


import org.junit.*;

import static org.junit.Assert.*;

public class InMemoryPriorityQueueTest {

    @Test
    public void testPriorityQueue() {
        InMemoryPriorityQueue queue = new InMemoryPriorityQueue();

        queue.push(5, "Request 1");
        queue.push(10, "Request 2");
        queue.push(5, "Request 3");

        assertEquals("Request 2", queue.poll());
        assertEquals("Request 1", queue.poll());
        assertEquals("Request 3", queue.poll());
    }

    @Test
    public void testQueueIsEmpty() {
        InMemoryPriorityQueue queue = new InMemoryPriorityQueue();
        assertNull(queue.poll());
        queue.push(1, "Request");
        assertNotNull(queue.poll());
    }


}

