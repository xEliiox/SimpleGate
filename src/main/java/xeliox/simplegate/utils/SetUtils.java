package xeliox.simplegate.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetUtils {

    /**
     * @param collection1
     * @param collection2
     * @param <T>
     * @return Returns the difference of 2 sets i.e. a Set of all elements which appear in set 1 but do not appear in set 2.<br>
     * Note that set 2 may have **more elements** which are not in set 1, but they do not appear in the difference.<br>
     * For example ***c1={1,2,3} c2={2,3,5,6} diff(c1,c2) = {1}***
     */
    public static <T> Set<T> diff(Collection<T> collection1, Collection<T> collection2) {
        Set<T> difference = new HashSet<>(collection1);
        if (collection2 instanceof Set) {
            difference.removeIf(collection2::contains);
        } else {
            difference.removeAll(new HashSet<>(collection2));
        }
        return difference;
    }
}