package org.pitest.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

// from http://www.cs.princeton.edu/introcs/43stack/RingBuffer.java.html
// licence status uncertain

public class RingBuffer<Item> implements Iterable<Item> {
  private final Item[] a;        // queue elements
  private int          N     = 0; // number of elements on queue
  private int          first = 0; // index of first element of queue
  private int          last  = 0; // index of next available slot

  // cast needed since no generic array creation in Java
  @SuppressWarnings("unchecked")
  public RingBuffer(final int capacity) {
    this.a = (Item[]) new Object[capacity];
  }

  public boolean isEmpty() {
    return this.N == 0;
  }

  public int size() {
    return this.N;
  }

  public void enqueue(final Item item) {
    if (this.N == this.a.length) {
      throw new RuntimeException("Ring buffer overflow");
    }
    this.a[this.last] = item;
    this.last = (this.last + 1) % this.a.length; // wrap-around
    this.N++;
  }

  // remove the least recently added item - doesn't check for underflow
  public Item dequeue() {
    if (isEmpty()) {
      throw new RuntimeException("Ring buffer underflow");
    }
    final Item item = this.a[this.first];
    this.a[this.first] = null; // to help with garbage collection
    this.N--;
    this.first = (this.first + 1) % this.a.length; // wrap-around
    return item;
  }

  public Iterator<Item> iterator() {
    return new RingBufferIterator();
  }

  // an iterator, doesn't implement remove() since it's optional
  private class RingBufferIterator implements Iterator<Item> {
    private int i = 0;

    public boolean hasNext() {
      return this.i < RingBuffer.this.N;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public Item next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return RingBuffer.this.a[this.i++];
    }
  }
}
