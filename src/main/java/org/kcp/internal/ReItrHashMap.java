package org.kcp.internal;

// Imports
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.*;

public class ReItrHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final Entry<?, ?>[] EMPTY_TABLE = {};
    transient Entry<K, V>[] table = (Entry<K, V>[]) EMPTY_TABLE;
    transient int size;
    int threshold;
    final float loadFactor;
    transient int modCount;

    public ReItrHashMap(int initialCapacity, float loadFactor) {
        if(initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }

        if(initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        if(loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

        this.loadFactor = loadFactor;
        threshold = initialCapacity;
        init();
    }

    public ReItrHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public ReItrHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public ReItrHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int)(m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        inflateTable(threshold);
        putAllForCreate(m);
    }

    private static int roundUpToPowerOf2(int number) {
        return number >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }

    private void inflateTable(int toSize) {
        int capacity = roundUpToPowerOf2(toSize);
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry[capacity];
    }


    void init() {

    }

    final int hash(Object k) {
        int h = k.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(Object key) {
        if(key == null) {
            return getForNullKey();
        }

        Entry<K, V> entry = getEntry(key);
        return null == entry ? null : entry.getValue();
    }

    private V getForNullKey() {
        if(size == 0) {
            return null;
        }

        for(Entry<K, V> e = table[0]; e != null; e = e.next) {
            if(e.key == null) {
                return e.value;
            }
        }

        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    final Entry<K, V> getEntry(Object key) {
        if(size == 0) {
            return null;
        }

        int hash = (key == null) ? 0 : hash(key);
        for(Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
            Object k;
            if(e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                return e;
            }
        }

        return null;
    }

    @Override
    public V put(K key, V value) {
        if(table == EMPTY_TABLE) {
            inflateTable(threshold);
        }

        if(key == null) {
            return putForNullKey(value);
        }

        int hash = hash(key);
        int i = indexFor(hash, table.length);
        for(Entry<K, V> e = table[i]; e != null; e = e.next) {
            Object k;
            if(e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(hash, key, value, i);
        return null;
    }

    private V putForNullKey(V value) {
        for(Entry<K, V> e = table[0]; e != null; e = e.next) {
            if(e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(0, null, value, 0);
        return null;
    }

    private void putForCreate(K key, V value) {
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        for(Entry<K, V> e = table[i]; e != null; e = e.next) {
            Object k;
            if(e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }

        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            putForCreate(e.getKey(), e.getValue());
        }
    }

    void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if(oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    void transfer(Entry[] newTable) {
        int newCapacity = newTable.length;
        for(Entry<K, V> e : table) {
            while(null != e) {
                Entry<K, V> next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if(numKeysToBeAdded == 0) {
            return;
        }

        if(table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        if(numKeysToBeAdded > threshold) {
            int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
            if(targetCapacity > MAXIMUM_CAPACITY) {
                targetCapacity = MAXIMUM_CAPACITY;
            }

            int newCapacity = table.length;
            while(newCapacity < targetCapacity) {
                newCapacity <<= 1;
            }

            if(newCapacity > table.length) {
                resize(newCapacity);
            }
        }

        for(Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        Entry<K, V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    final Entry<K, V> removeEntryForKey(Object key) {
        if(size == 0) {
            return null;
        }

        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K, V> prev = table[i];
        Entry<K, V> e = prev;
        while(e != null) {
            Entry<K, V> next = e.next;
            Object k;
            if(e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))) {
                modCount++;
                size--;
                if(prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }

                e.recordRemoval(this);
                return e;
            }

            prev = e;
            e = next;
        }

        return e;
    }

    final Entry<K, V> removeMapping(Object o) {
        if(size == 0 || !(o instanceof Map.Entry)) {
            return null;
        }

        Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
        Object key = entry.getKey();
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K, V> prev = table[i];
        Entry<K, V> e = prev;
        while(e != null) {
            Entry<K, V> next = e.next;
            if(e.hash == hash && e.equals(entry)) {
                modCount++;
                size--;
                if(prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }

                e.recordRemoval(this);
                return e;
            }

            prev = e;
            e = next;
        }

        return e;
    }

    @Override
    public void clear() {
        modCount++;
        Arrays.fill(table, null);
        size = 0;
    }

    @Override
    public boolean containsValue(Object value) {
        if(value == null) {
            return containsNullValue();
        }

        Entry[] tab = table;
        for(int i = 0; i < tab.length; i++) {
            for(Entry e = tab[i]; e != null; e = e.next) {
                if(value.equals(e.value)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsNullValue() {
        Entry[] tab = table;
        for(int i = 0; i < tab.length; i++) {
            for(Entry e = tab[i]; e != null; e = e.next) {
                if(e.value == null) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Object clone() {
        ReItrHashMap<K, V> result = null;
        try {
            result = (ReItrHashMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // assert false;
        }

        if(result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min((int) Math.min(size * Math.min(1 / loadFactor, 4.0f), MAXIMUM_CAPACITY), table.length));
        }

        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);
        return result;
    }

    static class Entry<K, V> implements Map.Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;
        int hash;

        /**
         * Creates new entry.
         */
        Entry(int h, K k, V v, Entry<K, V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        public final V getValue() {
            return value;
        }

        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if(!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if(k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if(v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        @Override
        public final String toString() {
            return getKey() + "=" + getValue();
        }

        void recordAccess(ReItrHashMap<K, V> m) {

        }

        void recordRemoval(ReItrHashMap<K, V> m) {

        }
    }

    void addEntry(int hash, K key, V value, int bucketIndex) {
        if((size >= threshold) && (null != table[bucketIndex])) {
            resize(2 * table.length);
            hash = (null != key) ? hash(key) : 0;
            bucketIndex = indexFor(hash, table.length);
        }

        createEntry(hash, key, value, bucketIndex);
    }

    void createEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K, V> e = table[bucketIndex];
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        size++;
    }

    private abstract class HashIterator<E> implements ReusableIterator<E> {
        Entry<K, V> next;        // next entry to return
        int expectedModCount;   // For fast-fail
        int index;              // current slot
        Entry<K, V> current;     // current entry

        HashIterator() {
            expectedModCount = modCount;
            if(size > 0) { // advance to first entry
                Entry[] t = table;
                while(index < t.length && (next = t[index++]) == null) {}
            }
        }

        void rewind0() {
            expectedModCount = modCount;
            current = next = null;
            index = 0;
            if(size > 0) { // advance to first entry
                Entry[] t = table;
                while(index < t.length && (next = t[index++]) == null) {}
            }
        }

        @Override
        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K, V> nextEntry() {
            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            Entry<K, V> e = next;
            if(e == null) {
                throw new NoSuchElementException();
            }

            if((next = e.next) == null) {
                Entry[] t = table;
                while(index < t.length && (next = t[index++]) == null) {}
            }

            current = e;
            return e;
        }

        @Override
        public void remove() {
            if(current == null) {
                throw new IllegalStateException();
            }

            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            Object k = current.key;
            current = null;
            ReItrHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }
    }

    private final class ValueIterator extends HashIterator<V> {
        @Override
        public ReusableIterator<V> rewind() {
            rewind0();
            return this;
        }

        @Override
        public V next() {
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator<K> {
        @Override
        public ReusableIterator<K> rewind() {
            rewind0();
            return this;
        }

        @Override
        public K next() {
            return nextEntry().getKey();
        }
    }

    private final class EntryIterator extends HashIterator<Map.Entry<K, V>> {
        @Override
        public ReusableIterator<Map.Entry<K, V>> rewind() {
            rewind0();
            return this;
        }

        @Override
        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }

    Iterator<V> newValueIterator() {
        return new ValueIterator();
    }

    ReusableIterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }

    transient volatile Set<K> keySet = null;
    transient volatile Collection<V> values = null;
    private transient ReItrSet<Map.Entry<K, V>> entrySet = null;

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return newKeyIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return ReItrHashMap.this.removeEntryForKey(o) != null;
        }

        @Override
        public void clear() {
            ReItrHashMap.this.clear();
        }
    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        return(vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return newValueIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public void clear() {
            ReItrHashMap.this.clear();
        }
    }

    @Override
    public ReItrSet<Map.Entry<K, V>> entrySet() {
        return entrySet0();
    }

    private ReItrSet<Map.Entry<K, V>> entrySet0() {
        ReItrSet<Map.Entry<K, V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> implements ReItrSet<Map.Entry<K, V>> {
        @Override
        public ReusableIterator<Map.Entry<K, V>> iterator() {
            return newEntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if(!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            Entry<K, V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        @Override
        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public void clear() {
            ReItrHashMap.this.clear();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if(table == EMPTY_TABLE) {
            s.writeInt(roundUpToPowerOf2(threshold));
        } else {
            s.writeInt(table.length);
        }

        s.writeInt(size);
        if(size > 0) {
            for(Map.Entry<K, V> e : entrySet0()) {
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if(loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " + loadFactor);
        }

        table = (Entry<K, V>[]) EMPTY_TABLE;
        s.readInt();
        int mappings = s.readInt();
        if(mappings < 0) {
            throw new InvalidObjectException("Illegal mappings count: " + mappings);
        }

        int capacity = (int) Math.min(mappings * Math.min(1 / loadFactor, 4.0f), ReItrHashMap.MAXIMUM_CAPACITY);
        if(mappings > 0) {
            inflateTable(capacity);
        } else {
            threshold = capacity;
        }

        init();
        for(int i = 0; i < mappings; i++) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            putForCreate(key, value);
        }
    }

    int capacity() {
        return table.length;
    }

    float loadFactor() {
        return loadFactor;
    }
}