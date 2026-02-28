package org.kcp.internal;

// Imports
import java.util.ListIterator;

public interface ReusableListIterator<E> extends ListIterator<E>, ReusableIterator<E> {
    @Override ReusableListIterator<E> rewind();
    ReusableListIterator<E> rewind(int index);
}