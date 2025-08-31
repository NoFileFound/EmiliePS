package org.kcp.internal;

// Imports
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import io.netty.util.Recycler;
import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * Reusable iterator
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CodecOutputList<T> extends AbstractList<T> implements RandomAccess {

    private static final Recycler<CodecOutputList> RECYCLER = new Recycler<CodecOutputList>() {
        @Override
        protected CodecOutputList newObject(Handle<CodecOutputList> handle) {
            return new CodecOutputList(handle);
        }
    };

    public static <T> CodecOutputList<T> newInstance() {
        return RECYCLER.get();
    }

    private final Recycler.Handle<CodecOutputList<T>> handle;
    private int size;
    private Object[] array = new Object[16];
    private boolean insertSinceRecycled;

    private CodecOutputList(Recycler.Handle<CodecOutputList<T>> handle) {
        this.handle = handle;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return (T) array[index];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(Object element) {
        checkNotNull(element, "element");

        int newSize = size + 1;
        if (newSize > array.length) {
            expandArray();
        }
        insert(size, element);
        size = newSize;

        return true;
    }

    @Override
    public T set(int index, Object element) {
        checkNotNull(element, "element");
        checkIndex(index);

        Object old = array[index];
        insert(index, element);
        return (T) old;
    }

    @Override
    public void add(int index, Object element) {
        checkNotNull(element, "element");
        checkIndex(index);

        if (size == array.length) {
            expandArray();
        }

        if (index != size - 1) {
            System.arraycopy(array, index, array, index + 1, size - index);
        }

        insert(index, element);
        ++size;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        Object old = array[index];

        int len = size - index - 1;
        if (len > 0) {
            System.arraycopy(array, index + 1, array, index, len);
        }
        array[--size] = null;

        return (T) old;
    }

    @Override
    public void clear() {
        // We only set the size to 0 and not null out the array. Null out the array will explicit requested by
        // calling recycle()
        size = 0;
    }

    /**
     * Recycle the array which will clear it and null out all entries in the kcp.highway.internal storage.
     */
    public void recycle() {
        for (int i = 0; i < size; i++) {
            array[i] = null;
        }
        clear();
        insertSinceRecycled = false;
        handle.recycle(this);
    }

    /**
     * Returns the element on the given index. This operation will not do any range-checks and so is considered unsafe.
     */
    public T getUnsafe(int index) {
        return (T) array[index];
    }

    private void checkIndex(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void insert(int index, Object element) {
        array[index] = element;
        insertSinceRecycled = true;
    }

    private void expandArray() {
        // double capacity
        int newCapacity = array.length << 1;

        if (newCapacity < 0) {
            throw new OutOfMemoryError();
        }

        Object[] newArray = new Object[newCapacity];
        System.arraycopy(array, 0, newArray, 0, array.length);

        array = newArray;
    }
}