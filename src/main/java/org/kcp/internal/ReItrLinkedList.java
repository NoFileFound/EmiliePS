package org.kcp.internal;

// Imports
import java.util.*;

public class ReItrLinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
    transient int size = 0;
    transient Node<E> first;
    transient Node<E> last;
    private static final long serialVersionUID = 8249177923269915620L;

    public ReItrLinkedList() {

    }

    public ReItrLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    private void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if(f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }

        size++;
        modCount++;
    }

    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if(l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }

        size++;
        modCount++;
    }

    void linkBefore(E e, Node<E> succ) {
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if(pred == null) {
            first = newNode;
        } else {
            pred.next = newNode;
        }

        size++;
        modCount++;
    }

    private E unlinkFirst(Node<E> f) {
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null;
        first = next;
        if(next == null) {
            last = null;
        } else {
            next.prev = null;
        }

        size--;
        modCount++;
        return element;
    }

    private E unlinkLast(Node<E> l) {
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null;
        last = prev;
        if(prev == null) {
            first = null;
        } else {
            prev.next = null;
        }

        size--;
        modCount++;
        return element;
    }

    E unlink(Node<E> x) {
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;
        if(prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if(next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }

    @Override
    public E getFirst() {
        final Node<E> f = first;
        if(f == null) {
            throw new NoSuchElementException();
        }

        return f.item;
    }

    @Override
    public E getLast() {
        final Node<E> l = last;
        if(l == null) {
            throw new NoSuchElementException();
        }

        return l.item;
    }

    @Override
    public E removeFirst() {
        final Node<E> f = first;
        if(f == null) {
            throw new NoSuchElementException();
        }

        return unlinkFirst(f);
    }

    @Override
    public E removeLast() {
        final Node<E> l = last;
        if(l == null) {
            throw new NoSuchElementException();
        }

        return unlinkLast(l);
    }

    @Override
    public void addFirst(E e) {
        linkFirst(e);
    }

    @Override
    public void addLast(E e) {
        linkLast(e);
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if(o == null) {
            for(Node<E> x = first; x != null; x = x.next) {
                if(x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for(Node<E> x = first; x != null; x = x.next) {
                if(o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        if(numNew == 0) {
            return false;
        }

        Node<E> pred, succ;
        if(index == size) {
            succ = null;
            pred = last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        for(Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;
            Node<E> newNode = new Node<>(pred, e, null);
            if(pred == null) {
                first = newNode;
            } else {
                pred.next = newNode;
            }
            pred = newNode;
        }

        if(succ == null) {
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;
        modCount++;
        return true;
    }

    @Override
    public void clear() {
        for(Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }

        first = last = null;
        size = 0;
        modCount++;
    }

    @Override
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    @Override
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }

    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);
        if(index == size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index));
        }
    }

    @Override
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void checkElementIndex(int index) {
        if(!isElementIndex(index)) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private void checkPositionIndex(int index) {
        if(!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    Node<E> node(int index) {
        if(index < (size >> 1)) {
            Node<E> x = first;
            for(int i = 0; i < index; i++) {
                x = x.next;
            }

            return x;
        } else {
            Node<E> x = last;
            for(int i = size - 1; i > index; i--) {
                x = x.prev;
            }

            return x;
        }
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        if(o == null) {
            for(Node<E> x = first; x != null; x = x.next) {
                if(x.item == null) {
                    return index;
                }
                index++;
            }
        } else {
            for(Node<E> x = first; x != null; x = x.next) {
                if(o.equals(x.item)) {
                    return index;
                }
                index++;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = size;
        if(o == null) {
            for(Node<E> x = last; x != null; x = x.prev) {
                index--;
                if(x.item == null) {
                    return index;
                }
            }
        } else {
            for(Node<E> x = last; x != null; x = x.prev) {
                index--;
                if(o.equals(x.item)) {
                    return index;
                }
            }
        }
        return -1;
    }

    @Override
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    @Override
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    @Override
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    @Override
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if(o == null) {
            for(Node<E> x = last; x != null; x = x.prev) {
                if(x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for(Node<E> x = last; x != null; x = x.prev) {
                if(o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ReusableIterator<E> iterator() {
        return listIterator();
    }

    @Override
    public ReusableListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ReusableListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    private class ListItr implements ReusableListIterator<E> {
        private Node<E> lastReturned = null;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount = modCount;
        ListItr(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        @Override
        public ReusableListIterator<E> rewind() {
            return rewind(0);
        }

        @Override
        public ReusableListIterator<E> rewind(int index) {
            lastReturned = null;
            next = (index == size) ? null : node(index);
            nextIndex = index;
            expectedModCount = modCount;
            return this;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            checkForComodification();
            if(!hasPrevious()) {
                throw new NoSuchElementException();
            }

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if(lastReturned == null) {
                throw new IllegalStateException();
            }

            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if(next == lastReturned) {
                next = lastNext;
            } else {
                nextIndex--;
            }

            lastReturned = null;
            expectedModCount++;
        }

        @Override
        public void set(E e) {
            if(lastReturned == null) {
                throw new IllegalStateException();
            }
            checkForComodification();
            lastReturned.item = e;
        }

        @Override
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if(next == null) {
                linkLast(e);
            } else {
                linkBefore(e, next);
            }

            nextIndex++;
            expectedModCount++;
        }

        final void checkForComodification() {
            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());

        @Override
        public boolean hasNext() {
            return itr.hasPrevious();
        }

        @Override
        public E next() {
            return itr.previous();
        }

        @Override
        public void remove() {
            itr.remove();
        }
    }

    private ReItrLinkedList<E> superClone() {
        try {
            return (ReItrLinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public Object clone() {
        ReItrLinkedList<E> clone = superClone();
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;
        for(Node<E> x = first; x != null; x = x.next) {
            clone.add(x.item);
        }

        return clone;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for(Node<E> x = first; x != null; x = x.next) {
            result[i++] = x.item;
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if(a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i = 0;
        Object[] result = a;
        for(Node<E> x = first; x != null; x = x.next) {
            result[i++] = x.item;
        }

        if(a.length > size) {
            a[size] = null;
        }

        return a;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        for(Node<E> x = first; x != null; x = x.next) {
            s.writeObject(x.item);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        for(int i = 0; i < size; i++) {
            linkLast((E) s.readObject());
        }
    }
}