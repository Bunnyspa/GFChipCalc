package main.puzzle;

/**
 *
 * @author Bunnyspa
 */
public class FStat implements Comparable<FStat> {

    public final int dmg, brk, hit, rld;

    public FStat(int val) {
        this.dmg = val;
        this.brk = val;
        this.hit = val;
        this.rld = val;
    }

    public FStat(int dmg, int brk, int hit, int rld) {
        this.dmg = dmg;
        this.brk = brk;
        this.hit = hit;
        this.rld = rld;
    }

    public FStat(int[] v) {
        this.dmg = v[0];
        this.brk = v[1];
        this.hit = v[2];
        this.rld = v[3];
    }

    public FStat(Stat s) {
        this.dmg = s.dmg;
        this.brk = s.brk;
        this.hit = s.hit;
        this.rld = s.rld;
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

    public Stat toStat() {
        return new Stat(this);
    }

    public String toData() {
        return toStat().toData();
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
    public int compareTo(FStat o) {
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
        return toStat().toString();
    }
}
