package com.example;
import java.util.PriorityQueue;

public class InMemoryPriorityQueue {
    private static class Request implements Comparable<Request> {
        int priority;
        String value;
        long timestamp;

        public Request(int priority, String value, long timestamp) {
            this.priority = priority;
            this.value = value;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(Request other) {
            if (this.priority != other.priority) {
                return Integer.compare(other.priority, this.priority); // Higher priority first
            }
            return Long.compare(this.timestamp, other.timestamp); // FCFS for equal priority
        }
    }

    private PriorityQueue<Request> queue = new PriorityQueue<>();
    private long counter = 0; // To track insertion order

    public void push(int priority, String value) {
        queue.add(new Request(priority, value, counter++));
    }

    public String poll() {
        Request request = queue.poll();
        return request == null ? null : request.value;
    }
}


