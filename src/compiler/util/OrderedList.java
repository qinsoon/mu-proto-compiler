package compiler.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * if (comparator.compare(i1, i2) < 0) then in the ordered list, i1 is always inserted before i2
 */
public class OrderedList<T> implements List<T> {
    LinkedList<T> list = new LinkedList<T>();
    Comparator<T> comparator;    

    /**
     * if (comparator.compare(i1, i2) < 0) then in the ordered list, i1 is always inserted before i2
     */
    public OrderedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }
    
    public OrderedList(Collection<T> orig, Comparator<T> comparator) {
        this(comparator);
        for (T i : orig)
            add(i);
    }
    
    public T get(int index) {
        return list.get(index);
    }
    
    public T poll() {
        return list.poll();
    }
    
    public boolean add(T i) {
        if (list.size() == 0) {
            list.add(i);
        } else if (comparator.compare(list.get(0), i) > 0) {
            list.add(0, i);
        } else if (comparator.compare(list.get(list.size() - 1), i) < 0) {
            list.add(list.size(), i);        
        } else {
            int iter = 0;
            while (comparator.compare(list.get(iter), i) < 0)
                iter++;
            list.add(iter, i);
        }
        
        return true;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            add(t);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public T set(int index, T element) {
        T ret = list.remove(index);
        list.add(element);
        return ret;
    }

    @Override
    public void add(int index, T element) {
        add(element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}
