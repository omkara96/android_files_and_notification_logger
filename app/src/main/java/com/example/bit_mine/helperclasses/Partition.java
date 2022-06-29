package com.example.bit_mine.helperclasses;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public final class Partition<DbParam> extends AbstractList<ArrayList<DbParam>> {

    private final List<DbParam> list;
    private final int chunkSize;

    public Partition(List<DbParam> list, int chunkSize) {
        this.list = new ArrayList<>(list);
        this.chunkSize = chunkSize;
    }

    public static <DbParam> Partition<DbParam> ofSize(List<DbParam> list, int chunkSize) {
        return new Partition<>(list, chunkSize);
    }

    @Override
    public ArrayList<DbParam> get(int index) {
        int start = index * chunkSize;
        int end = Math.min(start + chunkSize, list.size());

        if (start > end) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of the list range <0," + (size() - 1) + ">");
        }

        return new ArrayList<DbParam>(list.subList(start, end));
    }

    @Override
    public int size() {
        return (int) Math.ceil((double) list.size() / (double) chunkSize);
    }
}
