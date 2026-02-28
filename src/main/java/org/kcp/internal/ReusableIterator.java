package org.kcp.internal;

// Imports
import java.util.Iterator;

public interface ReusableIterator<E> extends Iterator<E> {
    ReusableIterator<E> rewind();
}