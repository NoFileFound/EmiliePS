package org.kcp.internal;

// Imports
import java.util.Collection;

/**
 * Reusable iterator
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface ReItrCollection<E> extends Collection<E> {
    @Override
    ReusableIterator<E> iterator();
}