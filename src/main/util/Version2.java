package main.util;

/**
 *
 * @author Bunnyspa
 */
public class Version2 {

    public final int v1, v2;

    public Version2() {
        v1 = 0;
        v2 = 0;
    }

    public Version2(String version) {
        String[] verStrs = version.split("\\.");
        if (2 == verStrs.length) {
            v1 = Integer.valueOf(verStrs[0]);
            v2 = Integer.valueOf(verStrs[1]);
        } else {
            v1 = 0;
            v2 = 0;
        }
    }

    public Version2(int v1, int v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public boolean isCurrent(int cv1, int cv2) {
        if (v1 < cv1) {
            return false;
        }
        return !(v1 == cv1 && v2 < cv2);
    }

    public boolean isCurrent(String version) {
        Version2 v = new Version2(version);
        return isCurrent(v.v1, v.v2);
    }

    public String toData() {
        return String.join(".", String.valueOf(v1), String.valueOf(v2));
    }

    @Override
    public String toString() {
        return toData();
    }
}
