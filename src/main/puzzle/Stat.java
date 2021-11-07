package main.puzzle;

import java.io.Serializable;
import java.util.Collection;

public class Stat implements Comparable<Stat>, Serializable {

    public static final int DMG = 0;
    public static final int BRK = 1;
    public static final int HIT = 2;
    public static final int RLD = 3;
    public final int dmg, brk, hit, rld;

    public static Stat chipStatSum(Collection<Chip> chips) {
        int dmg = 0;
        int brk = 0;
        int hit = 0;
        int rld = 0;
        for (Chip chip : chips) {
            Stat s = chip.getStat();
            dmg += s.dmg;
            brk += s.brk;
            hit += s.hit;
            rld += s.rld;
        }
        return new Stat(dmg, brk, hit, rld);
    }

    public static Stat chipPtSum(Collection<Chip> chips) {
        int dmg = 0;
        int brk = 0;
        int hit = 0;
        int rld = 0;
        for (Chip chip : chips) {
            Stat s = chip.getPt();
            dmg += s.dmg;
            brk += s.brk;
            hit += s.hit;
            rld += s.rld;
        }
        return new Stat(dmg, brk, hit, rld);
    }

    public static Stat chipOldStatSum(Collection<Chip> chips) {
        int dmg = 0;
        int brk = 0;
        int hit = 0;
        int rld = 0;
        for (Chip chip : chips) {
            Stat s = chip.getOldStat();
            dmg += s.dmg;
            brk += s.brk;
            hit += s.hit;
            rld += s.rld;
        }
        return new Stat(dmg, brk, hit, rld);
    }

    public Stat() {
        this.dmg = 0;
        this.brk = 0;
        this.hit = 0;
        this.rld = 0;
    }

    public Stat(int val) {
        this.dmg = val;
        this.brk = val;
        this.hit = val;
        this.rld = val;
    }

    public Stat(int dmg, int brk, int hit, int rld) {
        this.dmg = dmg;
        this.brk = brk;
        this.hit = hit;
        this.rld = rld;
    }

    public Stat(int[] v) {
        this.dmg = v[0];
        this.brk = v[1];
        this.hit = v[2];
        this.rld = v[3];
    }

    public Stat(Collection<Stat> stats) {
        int[] s = new int[4];
        for (Stat stat : stats) {
            int[] array = stat.toArray();
            for (int i = 0; i < 4; i++) {
                s[i] += array[i];
            }
        }

        this.dmg = s[0];
        this.brk = s[1];
        this.hit = s[2];
        this.rld = s[3];
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

    public Stat limit(Stat max) {
        int newDmg = Math.min(this.dmg, max.dmg);
        int newBrk = Math.min(this.brk, max.brk);
        int newHit = Math.min(this.hit, max.hit);
        int newRld = Math.min(this.rld, max.rld);
        return new Stat(newDmg, newBrk, newHit, newRld);
    }

    public String toStringSlash() {
        return String.join("/",
                String.valueOf(dmg),
                String.valueOf(brk),
                String.valueOf(hit),
                String.valueOf(rld)
        );
    }

    public boolean allZero() {
        return dmg == 0 && brk == 0 && hit == 0 && rld == 0;
    }

    public int[] toArray() {
        int[] out = {dmg, brk, hit, rld};
        return out;
    }

    public int sum() {
        return dmg + brk + hit + rld;
    }

    public String toData() {
        return String.join(",", String.valueOf(dmg), String.valueOf(brk), String.valueOf(hit), String.valueOf(rld));
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
            Stat fstat = (Stat) obj;
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

    @Override
    public String toString() {
        return "[" + toData() + "]";
    }
}
