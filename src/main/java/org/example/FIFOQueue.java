package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple FIFO (First-In-First-Out) queue implementation.
 * This class is used to manage tickets in the queue system.
 */
public class FIFOQueue<T> {
    private final List<T> elements;

    /**
     * Constructs an empty queue.
     */
    public FIFOQueue() {
        this.elements = new ArrayList<>();
    }

    /**
     * Adds an element to the end of the queue.
     *
     * @param element the element to add
     */
    public synchronized void enqueue(T element) {
        elements.add(element);
    }

    /**
     * Removes and returns the element at the front of the queue.
     *
     * @return the element at the front of the queue
     * @throws IllegalStateException if the queue is empty
     */
    public synchronized T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return elements.remove(0);
    }

    /**
     * Returns, but does not remove, the element at the front of the queue.
     *
     * @return the element at the front of the queue
     * @throws IllegalStateException if the queue is empty
     */
    public synchronized T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return elements.get(0);
    }

    /**
     * Returns true if the queue contains no elements.
     *
     * @return true if the queue contains no elements
     */
    public synchronized boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Returns the number of elements in the queue.
     *
     * @return the number of elements in the queue
     */
    public synchronized int size() {
        return elements.size();
    }

    /**
     * Returns all elements in the queue without removing them.
     *
     * @return a list of all elements in the queue
     */
    public synchronized List<T> getAll() {
        return new ArrayList<>(elements);
    }
}