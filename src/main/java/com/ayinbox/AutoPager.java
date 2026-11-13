package com.ayinbox;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Walks every page of a list lazily, following {@code next_cursor} while {@code has_more} is true.
 * Each call to {@link #iterator()} starts again from the first page.
 */
public final class AutoPager<T> implements Iterable<T> {
    private final Function<String, Page<T>> fetch;

    AutoPager(Function<String, Page<T>> fetch) {
        this.fetch = fetch;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<T> rows;
            private String cursor;
            private boolean last;

            @Override
            public boolean hasNext() {
                while (rows == null || !rows.hasNext()) {
                    if (last) return false;
                    Page<T> page = fetch.apply(cursor);
                    rows = page.getData().iterator();
                    cursor = page.getNextCursor();
                    last = !page.hasMore() || cursor == null;
                }
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return rows.next();
            }
        };
    }

    /** A sequential stream over every row. */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
