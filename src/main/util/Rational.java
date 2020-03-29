package main.util;

/**
 *
 * @author Bunnyspa
 */
public class Rational {

    private int numerator, denominator;

    public Rational(int n) {
        numerator = n;
        denominator = 1;
    }

    public Rational(int n, int d) {
        numerator = n;
        denominator = d;
    }

    private static int gcd(int a, int b) {
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        int r = a % b;
        return gcd(b, r);
    }

    private static int lcm(int a, int b) {
        return a * b / gcd(a, b);
    }

    private void reduce() {
        int d = gcd(numerator, denominator);
        numerator /= d;
        denominator /= d;
    }

    public Rational add(int n, int d) {
        int l = lcm(denominator, d);
        numerator = numerator * (l / denominator) + n * (l / d);
        denominator = l;
        reduce();
        return this;
    }

    public Rational add(int n) {
        return add(n, 1);
    }

    public Rational add(Rational r) {
        return add(r.numerator, r.denominator);
    }

    public Rational mult(int n, int d) {
        numerator *= n;
        denominator *= d;
        reduce();
        return this;
    }

    public Rational mult(int n) {
        return mult(n, 1);
    }

    public Rational mult(Rational r) {
        return mult(r.numerator, r.denominator);
    }

    public Rational mult(FRational r) {
        return mult(r.getRational());
    }

    public Rational div(int d) {
        return mult(1, d);
    }

    public Rational div(Rational r) {
        return mult(r.denominator, r.numerator);
    }

    public Rational div(FRational r) {
        return div(r.getRational());
    }

    public int getIntFloor() {
        int q = numerator / denominator;
        return q;
    }

    public int getIntCeil() {
        int r = numerator % denominator;
        int q = numerator / denominator;
        if (r > 0) {
            q++;
        }
        return q;
    }

    public double getDouble() {
        double n = numerator;
        return n / denominator;
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
}
