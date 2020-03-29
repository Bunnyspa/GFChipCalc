package main.util;

/**
 *
 * @author Bunnyspa
 */
public class FRational {

    private final int n, d;

    public FRational(int n, int d) {
        this.n = n;
        this.d = d;
    }

    public Rational getRational() {
        return new Rational(n, d);
    }

    public double getDouble() {
        double nDouble = n;
        return nDouble / d;
    }

    @Override
    public String toString() {
        return getRational().toString();
    }
}
