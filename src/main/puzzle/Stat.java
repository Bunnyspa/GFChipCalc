package main.puzzle;

import java.io.Serializable;
import main.util.Rational;

/**
 *
 * @author Bunnyspa
 */
public class Stat implements Serializable, Comparable<Stat> {

    public static final int DMG = 0;
    public static final int BRK = 1;
    public static final int HIT = 2;
    public static final int RLD = 3;

    public int dmg, brk, hit, rld;

    public Stat() {
        this.dmg = 0;
        this.brk = 0;
        this.hit = 0;
        this.rld = 0;
    }

    public Stat(int value) {
        this.dmg = value;
        this.brk = value;
        this.hit = value;
        this.rld = value;
    }

    public Stat(int dmg, int brk, int hit, int rld) {
        this.dmg = dmg;
        this.brk = brk;
        this.hit = hit;
        this.rld = rld;
    }

    public Stat(int[] stat) {
        this.dmg = stat[0];
        this.brk = stat[1];
        this.hit = stat[2];
        this.rld = stat[3];
    }

    public Stat(Stat s) {
        this.dmg = s.dmg;
        this.brk = s.brk;
        this.hit = s.hit;
        this.rld = s.rld;
    }

    public Stat(FStat s) {
        this.dmg = s.dmg;
        this.brk = s.brk;
        this.hit = s.hit;
        this.rld = s.rld;
    }

    public int sum() {
        return dmg + brk + hit + rld;
    }

    public int[] toArray() {
        int[] out = {dmg, brk, hit, rld};
        return out;
    }

    public String toData() {
        return String.join(",", String.valueOf(dmg), String.valueOf(brk), String.valueOf(hit), String.valueOf(rld));
    }

    @Override
    public String toString() {
        return "[" + toData() + "]";
    }

    public Stat add(int[] stat) {
        this.dmg += stat[0];
        this.brk += stat[1];
        this.hit += stat[2];
        this.rld += stat[3];
        return this;
    }

    public Stat add(Stat stat) {
        this.dmg += stat.dmg;
        this.brk += stat.brk;
        this.hit += stat.hit;
        this.rld += stat.rld;
        return this;
    }

    public Stat add(FStat stat) {
        return add(stat.toStat());
    }

    public Stat subtract(Stat stat) {
        this.dmg -= stat.dmg;
        this.brk -= stat.brk;
        this.hit -= stat.hit;
        this.rld -= stat.rld;
        return this;
    }

    public Stat subtract(int[] stat) {
        if (stat.length == 4) {
            this.dmg -= stat[0];
            this.brk -= stat[1];
            this.hit -= stat[2];
            this.rld -= stat[3];
        }
        return this;
    }

    public Stat limit(Stat max) {
        this.dmg = Math.min(this.dmg, max.dmg);
        this.brk = Math.min(this.brk, max.brk);
        this.hit = Math.min(this.hit, max.hit);
        this.rld = Math.min(this.rld, max.rld);
        return this;
    }

    public boolean anyNeg() {
        return dmg < 0 || brk < 0 || hit < 0 || rld < 0;
    }

    public boolean allGeq(Stat s) {
        return s.dmg <= dmg
                && s.brk <= brk
                && s.hit <= hit
                && s.rld <= rld;
    }

    public boolean allLeq(Stat s) {
        return dmg <= s.dmg
                && brk <= s.brk
                && hit <= s.hit
                && rld <= s.rld;
    }

    public Stat applyMultCeil(Rational rational) {
        dmg = new Rational(dmg).mult(rational).getIntCeil();
        brk = new Rational(brk).mult(rational).getIntCeil();
        hit = new Rational(hit).mult(rational).getIntCeil();
        rld = new Rational(rld).mult(rational).getIntCeil();
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.dmg;
        hash = 53 * hash + this.brk;
        hash = 53 * hash + this.hit;
        hash = 53 * hash + this.rld;
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
        if (obj.getClass() == Stat.class) {
            Stat stat = (Stat) obj;
            return this.dmg == stat.dmg && this.brk == stat.brk && this.hit == stat.hit && this.rld == stat.rld;
        }
        if (obj.getClass() == FStat.class) {
            FStat fstat = (FStat) obj;
            return this.dmg == fstat.dmg && this.brk == fstat.brk && this.hit == fstat.hit && this.rld == fstat.rld;
        }
        return false;
    }

    @Override
    public int compareTo(Stat o) {
        if (dmg != o.dmg) {
            return o.dmg - dmg;
        }
        if (brk != o.brk) {
            return o.brk - brk;
        }
        if (hit != o.hit) {
            return o.hit - hit;
        }
        if (rld != o.rld) {
            return o.rld - rld;
        }
        return 0;
    }

}
