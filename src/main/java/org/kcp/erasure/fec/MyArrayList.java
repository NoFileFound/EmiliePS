package org.kcp.erasure.fec;

// Imports
import java.util.ArrayList;

public class MyArrayList<E> extends ArrayList<E> {
    public MyArrayList() {super();}
    public MyArrayList(int initialCapacity) {
        super(initialCapacity);
    }
    public void removeRange(int fromIndex, int toIndex){
        super.removeRange(fromIndex, toIndex);
    }
}