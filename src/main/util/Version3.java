package main.util;

/**
 *
 * @author Bunnyspa
 */
public class Version3 {

    public final int v1, v2, v3;

    public Version3() {
        v1 = 0;
        v2 = 0;
        v3 = 0;
    }

    public Version3(String version) {
        String[] verStrs = version.split("\\.");
        if (3 == verStrs.length) {
            v1 = Integer.valueOf(verStrs[0]);
            v2 = Integer.valueOf(verStrs[1]);
            v3 = Integer.valueOf(verStrs[2]);
        } else {
            v1 = 0;
            v2 = 0;
            v3 = 0;
        }
    }

    public Version3(int v1, int v2, int v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public boolean isCurrent(int cv1, int cv2, int cv3) {
        if (v1 < cv1) {
            return false;
        }
        if (v1 == cv1 && v2 < cv2) {
            return false;
        }
        return !(v1 == cv1 && v2 == cv2 && v3 < cv3);
    }

    public boolean isCurrent(String version) {
        Version3 v = new Version3(version);
        return isCurrent(v.v1, v.v2, v.v3);
    }

    public String toData() {
        return String.join(".", String.valueOf(v1), String.valueOf(v2), String.valueOf(v3));
    }

    @Override
    public String toString() {
        return toData();
    }
}
