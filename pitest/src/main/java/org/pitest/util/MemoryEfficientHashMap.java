// Code taken from google. Minor changes made to remove warnings
// plus auto reformatting applied

/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.pitest.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A memory-efficient hash map.
 * 
 * @param <K>
 *          the key type
 * @param <V>
 *          the value type
 */
public class MemoryEfficientHashMap<K, V> implements Map<K, V>, Serializable {

  private static final long serialVersionUID   = 1L;

  /**
   * In the interest of memory-savings, we start with the smallest feasible
   * power-of-two table size that can hold three items without rehashing. If we
   * started with a size of 2, we'd have to expand as soon as the second item
   * was added.
   */
  private static final int  INITIAL_TABLE_SIZE = 4;

  private class EntryIterator implements Iterator<Entry<K, V>> {
    private int index = 0;
    private int last  = -1;

    {
      advanceToItem();
    }

    public boolean hasNext() {
      return this.index < MemoryEfficientHashMap.this.keys.length;
    }

    public Entry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      this.last = this.index;
      final Entry<K, V> toReturn = new HashEntry(this.index++);
      advanceToItem();
      return toReturn;
    }

    public void remove() {
      if (this.last < 0) {
        throw new IllegalStateException();
      }
      internalRemove(this.last);
      if (MemoryEfficientHashMap.this.keys[this.last] != null) {
        this.index = this.last;
      }
      this.last = -1;
    }

    private void advanceToItem() {
      for (; this.index < MemoryEfficientHashMap.this.keys.length; ++this.index) {
        if (MemoryEfficientHashMap.this.keys[this.index] != null) {
          return;
        }
      }
    }
  }

  private class EntrySet extends AbstractSet<Entry<K, V>> {
    @Override
    public boolean add(final Entry<K, V> entry) {
      final boolean result = !MemoryEfficientHashMap.this.containsKey(entry
          .getKey());
      MemoryEfficientHashMap.this.put(entry.getKey(), entry.getValue());
      return result;
    }

    @Override
    public boolean addAll(final Collection<? extends Entry<K, V>> c) {
      MemoryEfficientHashMap.this.ensureSizeFor(size() + c.size());
      return super.addAll(c);
    }

    @Override
    public void clear() {
      MemoryEfficientHashMap.this.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      final Entry<K, V> entry = (Entry<K, V>) o;
      final V value = MemoryEfficientHashMap.this.get(entry.getKey());
      return MemoryEfficientHashMap.this.valueEquals(value, entry.getValue());
    }

    @Override
    public int hashCode() {
      return MemoryEfficientHashMap.this.hashCode();
    }

    @Override
    public Iterator<java.util.Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      final Entry<K, V> entry = (Entry<K, V>) o;
      final int index = findKey(entry.getKey());
      if ((index >= 0)
          && valueEquals(MemoryEfficientHashMap.this.values[index],
              entry.getValue())) {
        internalRemove(index);
        return true;
      }
      return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      boolean didRemove = false;
      for (final Object o : c) {
        didRemove |= remove(o);
      }
      return didRemove;
    }

    @Override
    public int size() {
      return MemoryEfficientHashMap.this.size;
    }
  }

  private class HashEntry implements Entry<K, V> {
    private final int index;

    public HashEntry(final int index) {
      this.index = index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      final Entry<K, V> entry = (Entry<K, V>) o;
      return keyEquals(getKey(), entry.getKey())
          && valueEquals(getValue(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    public K getKey() {
      return (K) unmaskNullKey(MemoryEfficientHashMap.this.keys[this.index]);
    }

    @SuppressWarnings("unchecked")
    public V getValue() {
      return (V) MemoryEfficientHashMap.this.values[this.index];
    }

    @Override
    public int hashCode() {
      return keyHashCode(getKey()) ^ valueHashCode(getValue());
    }

    @SuppressWarnings("unchecked")
    public V setValue(final V value) {
      final V previous = (V) MemoryEfficientHashMap.this.values[this.index];
      MemoryEfficientHashMap.this.values[this.index] = value;
      return previous;
    }

    @Override
    public String toString() {
      return getKey() + "=" + getValue();
    }
  }

  private class KeyIterator implements Iterator<K> {
    private int index = 0;
    private int last  = -1;

    {
      advanceToItem();
    }

    public boolean hasNext() {
      return this.index < MemoryEfficientHashMap.this.keys.length;
    }

    @SuppressWarnings("unchecked")
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      this.last = this.index;
      final Object toReturn = unmaskNullKey(MemoryEfficientHashMap.this.keys[this.index++]);
      advanceToItem();
      return (K) toReturn;
    }

    public void remove() {
      if (this.last < 0) {
        throw new IllegalStateException();
      }
      internalRemove(this.last);
      if (MemoryEfficientHashMap.this.keys[this.last] != null) {
        this.index = this.last;
      }
      this.last = -1;
    }

    private void advanceToItem() {
      for (; this.index < MemoryEfficientHashMap.this.keys.length; ++this.index) {
        if (MemoryEfficientHashMap.this.keys[this.index] != null) {
          return;
        }
      }
    }
  }

  private class KeySet extends AbstractSet<K> {
    @Override
    public void clear() {
      MemoryEfficientHashMap.this.clear();
    }

    @Override
    public boolean contains(final Object o) {
      return MemoryEfficientHashMap.this.containsKey(o);
    }

    @Override
    public int hashCode() {
      int result = 0;
      for (int i = 0; i < MemoryEfficientHashMap.this.keys.length; ++i) {
        final Object key = MemoryEfficientHashMap.this.keys[i];
        if (key != null) {
          result += keyHashCode(unmaskNullKey(key));
        }
      }
      return result;
    }

    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    @Override
    public boolean remove(final Object o) {
      final int index = findKey(o);
      if (index >= 0) {
        internalRemove(index);
        return true;
      }
      return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      boolean didRemove = false;
      for (final Object o : c) {
        didRemove |= remove(o);
      }
      return didRemove;
    }

    @Override
    public int size() {
      return MemoryEfficientHashMap.this.size;
    }
  }

  private class ValueIterator implements Iterator<V> {
    private int index = 0;
    private int last  = -1;

    {
      advanceToItem();
    }

    public boolean hasNext() {
      return this.index < MemoryEfficientHashMap.this.keys.length;
    }

    @SuppressWarnings("unchecked")
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      this.last = this.index;
      final Object toReturn = MemoryEfficientHashMap.this.values[this.index++];
      advanceToItem();
      return (V) toReturn;
    }

    public void remove() {
      if (this.last < 0) {
        throw new IllegalStateException();
      }
      internalRemove(this.last);
      if (MemoryEfficientHashMap.this.keys[this.last] != null) {
        this.index = this.last;
      }
      this.last = -1;
    }

    private void advanceToItem() {
      for (; this.index < MemoryEfficientHashMap.this.keys.length; ++this.index) {
        if (MemoryEfficientHashMap.this.keys[this.index] != null) {
          return;
        }
      }
    }
  }

  private class Values extends AbstractCollection<V> {
    @Override
    public void clear() {
      MemoryEfficientHashMap.this.clear();
    }

    @Override
    public boolean contains(final Object o) {
      return MemoryEfficientHashMap.this.containsValue(o);
    }

    @Override
    public int hashCode() {
      int result = 0;
      for (int i = 0; i < MemoryEfficientHashMap.this.keys.length; ++i) {
        if (MemoryEfficientHashMap.this.keys[i] != null) {
          result += valueHashCode(MemoryEfficientHashMap.this.values[i]);
        }
      }
      return result;
    }

    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    @Override
    public boolean remove(final Object o) {
      if (o == null) {
        for (int i = 0; i < MemoryEfficientHashMap.this.keys.length; ++i) {
          if ((MemoryEfficientHashMap.this.keys[i] != null)
              && (MemoryEfficientHashMap.this.values[i] == null)) {
            internalRemove(i);
            return true;
          }
        }
      } else {
        for (int i = 0; i < MemoryEfficientHashMap.this.keys.length; ++i) {
          if (valueEquals(MemoryEfficientHashMap.this.values[i], o)) {
            internalRemove(i);
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      boolean didRemove = false;
      for (final Object o : c) {
        didRemove |= remove(o);
      }
      return didRemove;
    }

    @Override
    public int size() {
      return MemoryEfficientHashMap.this.size;
    }
  }

  private static final Object NULL_KEY = new Serializable() {

                                         private static final long serialVersionUID = 1L;

                                         Object readResolve() {
                                           return NULL_KEY;
                                         }
                                       };

  static Object maskNullKey(final Object k) {
    return (k == null) ? NULL_KEY : k;
  }

  static Object unmaskNullKey(final Object k) {
    return (k == NULL_KEY) ? null : k;
  }

  /**
   * Backing store for all the keys; transient due to custom serialization.
   * Default access to avoid synthetic accessors from inner classes.
   */
  private transient Object[] keys;

  /**
   * Number of pairs in this set; transient due to custom serialization. Default
   * access to avoid synthetic accessors from inner classes.
   */
  private transient int      size = 0;

  /**
   * Backing store for all the values; transient due to custom serialization.
   * Default access to avoid synthetic accessors from inner classes.
   */
  private transient Object[] values;

  public MemoryEfficientHashMap() {
    initTable(INITIAL_TABLE_SIZE);
  }


  public void clear() {
    initTable(INITIAL_TABLE_SIZE);
    this.size = 0;
  }

  public boolean containsKey(final Object key) {
    return findKey(key) >= 0;
  }

  public boolean containsValue(final Object value) {
    if (value == null) {
      for (int i = 0; i < this.keys.length; ++i) {
        if ((this.keys[i] != null) && (this.values[i] == null)) {
          return true;
        }
      }
    } else {
      for (final Object existing : this.values) {
        if (valueEquals(existing, value)) {
          return true;
        }
      }
    }
    return false;
  }

  public Set<Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object o) {
    if (!(o instanceof Map)) {
      return false;
    }
    final Map<K, V> other = (Map<K, V>) o;
    return entrySet().equals(other.entrySet());
  }

  @SuppressWarnings("unchecked")
  public V get(final Object key) {
    final int index = findKey(key);
    return (index < 0) ? null : (V) this.values[index];
  }

  @Override
  public int hashCode() {
    int result = 0;
    for (int i = 0; i < this.keys.length; ++i) {
      final Object key = this.keys[i];
      if (key != null) {
        result += keyHashCode(unmaskNullKey(key))
            ^ valueHashCode(this.values[i]);
      }
    }
    return result;
  }

  public boolean isEmpty() {
    return this.size == 0;
  }

  public Set<K> keySet() {
    return new KeySet();
  }

  @SuppressWarnings("unchecked")
  public V put(final K key, final V value) {
    ensureSizeFor(this.size + 1);
    final int index = findKeyOrEmpty(key);
    if (this.keys[index] == null) {
      ++this.size;
      this.keys[index] = maskNullKey(key);
      this.values[index] = value;
      return null;
    } else {
      final Object previousValue = this.values[index];
      this.values[index] = value;
      return (V) previousValue;
    }
  }

  public void putAll(final Map<? extends K, ? extends V> m) {
    ensureSizeFor(this.size + m.size());
    internalPutAll(m);
  }

  @SuppressWarnings("unchecked")
  public V remove(final Object key) {
    final int index = findKey(key);
    if (index < 0) {
      return null;
    }
    final Object previousValue = this.values[index];
    internalRemove(index);
    return (V) previousValue;
  }

  public int size() {
    return this.size;
  }

  @Override
  public String toString() {
    if (this.size == 0) {
      return "{}";
    }
    final StringBuilder buf = new StringBuilder(32 * size());
    buf.append('{');

    boolean needComma = false;
    for (int i = 0; i < this.keys.length; ++i) {
      Object key = this.keys[i];
      if (key != null) {
        if (needComma) {
          buf.append(',').append(' ');
        }
        key = unmaskNullKey(key);
        final Object value = this.values[i];
        buf.append(key == this ? "(this Map)" : key).append('=')
            .append(value == this ? "(this Map)" : value);
        needComma = true;
      }
    }
    buf.append('}');
    return buf.toString();
  }

  public Collection<V> values() {
    return new Values();
  }

  /**
   * Adapted from org.apache.commons.collections.map.AbstractHashedMap.
   */
  @SuppressWarnings("unchecked")
  protected void doReadObject(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    final int capacity = in.readInt();
    initTable(capacity);
    final int items = in.readInt();
    for (int i = 0; i < items; i++) {
      final Object key = in.readObject();
      final Object value = in.readObject();
      put((K) key, (V) value);
    }
  }

  /**
   * Adapted from org.apache.commons.collections.map.AbstractHashedMap.
   */
  protected void doWriteObject(final ObjectOutputStream out) throws IOException {
    out.writeInt(this.keys.length);
    out.writeInt(this.size);
    for (int i = 0; i < this.keys.length; ++i) {
      final Object key = this.keys[i];
      if (key != null) {
        out.writeObject(unmaskNullKey(key));
        out.writeObject(this.values[i]);
      }
    }
  }

  /**
   * Returns whether two keys are equal for the purposes of this set.
   */
  protected boolean keyEquals(final Object a, final Object b) {
    return (a == null) ? (b == null) : a.equals(b);
  }

  /**
   * Returns the hashCode for a key.
   */
  protected int keyHashCode(final Object k) {
    return (k == null) ? 0 : k.hashCode();
  }

  /**
   * Returns whether two values are equal for the purposes of this set.
   */
  protected boolean valueEquals(final Object a, final Object b) {
    return (a == null) ? (b == null) : a.equals(b);
  }

  /**
   * Returns the hashCode for a value.
   */
  protected int valueHashCode(final Object v) {
    return (v == null) ? 0 : v.hashCode();
  }

  /**
   * Ensures the map is large enough to contain the specified number of entries.
   * Default access to avoid synthetic accessors from inner classes.
   */
  void ensureSizeFor(final int expectedSize) {
    if (this.keys.length * 3 >= expectedSize * 4) {
      return;
    }

    int newCapacity = this.keys.length << 1;
    while (newCapacity * 3 < expectedSize * 4) {
      newCapacity <<= 1;
    }

    final Object[] oldKeys = this.keys;
    final Object[] oldValues = this.values;
    initTable(newCapacity);
    for (int i = 0; i < oldKeys.length; ++i) {
      final Object k = oldKeys[i];
      if (k != null) {
        int newIndex = getKeyIndex(unmaskNullKey(k));
        while (this.keys[newIndex] != null) {
          if (++newIndex == this.keys.length) {
            newIndex = 0;
          }
        }
        this.keys[newIndex] = k;
        this.values[newIndex] = oldValues[i];
      }
    }
  }

  /**
   * Returns the index in the key table at which a particular key resides, or -1
   * if the key is not in the table. Default access to avoid synthetic accessors
   * from inner classes.
   */
  int findKey(final Object k) {
    int index = getKeyIndex(k);
    while (true) {
      final Object existing = this.keys[index];
      if (existing == null) {
        return -1;
      }
      if (keyEquals(k, unmaskNullKey(existing))) {
        return index;
      }
      if (++index == this.keys.length) {
        index = 0;
      }
    }
  }

  /**
   * Returns the index in the key table at which a particular key resides, or
   * the index of an empty slot in the table where this key should be inserted
   * if it is not already in the table. Default access to avoid synthetic
   * accessors from inner classes.
   */
  int findKeyOrEmpty(final Object k) {
    int index = getKeyIndex(k);
    while (true) {
      final Object existing = this.keys[index];
      if (existing == null) {
        return index;
      }
      if (keyEquals(k, unmaskNullKey(existing))) {
        return index;
      }
      if (++index == this.keys.length) {
        index = 0;
      }
    }
  }

  /**
   * Removes the entry at the specified index, and performs internal management
   * to make sure we don't wind up with a hole in the table. Default access to
   * avoid synthetic accessors from inner classes.
   */
  void internalRemove(final int index) {
    this.keys[index] = null;
    this.values[index] = null;
    --this.size;
    plugHole(index);
  }

  private int getKeyIndex(final Object k) {
    int h = keyHashCode(k);
    // Copied from Apache's AbstractHashedMap; prevents power-of-two collisions.
    h += ~(h << 9);
    h ^= (h >>> 14);
    h += (h << 4);
    h ^= (h >>> 10);
    // Power of two trick.
    return h & (this.keys.length - 1);
  }

  private void initTable(final int capacity) {
    this.keys = new Object[capacity];
    this.values = new Object[capacity];
  }

  private void internalPutAll(final Map<? extends K, ? extends V> m) {
    for (final Entry<? extends K, ? extends V> entry : m.entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      final int index = findKeyOrEmpty(key);
      if (this.keys[index] == null) {
        ++this.size;
        this.keys[index] = maskNullKey(key);
        this.values[index] = value;
      } else {
        this.values[index] = value;
      }
    }
  }

  /**
   * Tricky, we left a hole in the map, which we have to fill. The only way to
   * do this is to search forwards through the map shuffling back values that
   * match this index until we hit a null.
   */
  private void plugHole(int hole) {
    int index = hole + 1;
    if (index == this.keys.length) {
      index = 0;
    }
    while (this.keys[index] != null) {
      final int targetIndex = getKeyIndex(unmaskNullKey(this.keys[index]));
      if (hole < index) {
        /*
         * "Normal" case, the index is past the hole and the "bad range" is from
         * hole (exclusive) to index (inclusive).
         */
        if (!((hole < targetIndex) && (targetIndex <= index))) {
          // Plug it!
          this.keys[hole] = this.keys[index];
          this.values[hole] = this.values[index];
          this.keys[index] = null;
          this.values[index] = null;
          hole = index;
        }
      } else {
        /*
         * "Wrapped" case, the index is before the hole (we've wrapped) and the
         * "good range" is from index (exclusive) to hole (inclusive).
         */
        if ((index < targetIndex) && (targetIndex <= hole)) {
          // Plug it!
          this.keys[hole] = this.keys[index];
          this.values[hole] = this.values[index];
          this.keys[index] = null;
          this.values[index] = null;
          hole = index;
        }
      }
      if (++index == this.keys.length) {
        index = 0;
      }
    }
  }

  private void readObject(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    doReadObject(in);
  }

  private void writeObject(final ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    doWriteObject(out);
  }
}
