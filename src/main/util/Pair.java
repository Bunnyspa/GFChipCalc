package main.util;

import java.util.Objects;

public class Pair<A, B> {

    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "{" + first.toString() + ", " + second.toString() + "}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.first);
        hash = 47 * hash + Objects.hashCode(this.second);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(this.first, other.first)) {
            return false;
        }
        if (!Objects.equals(this.second, other.second)) {
            return false;
        }
        return true;
    }
}
