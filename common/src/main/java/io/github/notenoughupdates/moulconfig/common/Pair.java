package io.github.notenoughupdates.moulconfig.common;

import java.util.Objects;

public final class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = Objects.requireNonNull(left, "left must not be null");
        this.right = Objects.requireNonNull(right, "right must not be null");
    }

    public L left() {
        return this.left;
    }

    public R right() {
        return this.right;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair<?, ?> other = (Pair<?, ?>) o;
            return this.left.equals(other.left) && this.right.equals(other.right);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.right);
    }

    @Override
    public String toString() {
        return String.format("Pair[left=%s, right=%s]", this.left, this.right);
    }
}
