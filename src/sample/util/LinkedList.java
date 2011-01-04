package sample.util;

import java.util.Collection;

public class LinkedList<T> extends java.util.LinkedList<T> {

    private static final long serialVersionUID = 236;

    @Override
    public boolean add (T e) {
        if (e == null)
            throw new NullPointerException();
        return super.add(e);
    }

    @Override
    public void addFirst (T e) {
        if (e == null)
            throw new NullPointerException();
        super.addFirst(e);
    }

    @Override
    public void addLast (T e) {
        if (e == null)
            throw new NullPointerException();
        super.addLast(e);
    }

    @Override
    public boolean offer (T e) {
        if (e == null)
            throw new NullPointerException();
        return super.offer(e);
    }

    @Override
    public boolean offerFirst (T e) {
        if (e == null)
            throw new NullPointerException();
        return super.offerFirst(e);
    }

    @Override
    public boolean offerLast (T e) {
        if (e == null)
            throw new NullPointerException();
        return super.offerLast(e);
    }

    @Override
    public void push (T e) {
        if (e == null)
            throw new NullPointerException();
        super.push(e);
    }

    @Override
    public void add (int index, T element) {
        if (element == null)
            throw new NullPointerException();
        super.add(index, element);
    }

    @Override
    public boolean addAll (Collection<? extends T> c) {
        if (c.contains(null))
            throw new NullPointerException();
        return super.addAll(c);
    }

    @Override
    public boolean addAll (int index, Collection<? extends T> c) {
        if (c.contains(null))
            throw new NullPointerException();
        return super.addAll(index, c);
    }

    @Override
    public T set (int index, T element) {
        if (element == null)
            throw new NullPointerException();
        return super.set(index, element);
    }



}
