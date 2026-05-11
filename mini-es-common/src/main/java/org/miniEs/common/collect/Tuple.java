package org.miniEs.common.collect;

/**
 * A generic immutable tuple.
 * Mirrors org.elasticsearch.common.collect.Tuple.
 */
public class Tuple<V1, V2> {

    private final V1 v1;
    private final V2 v2;

    public Tuple(V1 v1, V2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public static <V1, V2> Tuple<V1, V2> tuple(V1 v1, V2 v2) {
        return new Tuple<>(v1, v2);
    }

    public V1 v1() { return v1; }
    public V2 v2() { return v2; }

    @Override
    public String toString() {
        return "(" + v1 + ", " + v2 + ")";
    }
}
