package main.ui.shortcut;

/**
 *
 * @author Bunnyspa
 */
public class Shortcut {

    private final int keyCode;
    private final boolean ctrl, shift;

    public Shortcut(int keyCode) {
        this.keyCode = keyCode;
        this.ctrl = false;
        this.shift = false;
    }

    public Shortcut(int keyCode, boolean ctrl) {
        this.keyCode = keyCode;
        this.ctrl = ctrl;
        this.shift = false;
    }

    public Shortcut(int keyCode, boolean ctrl, boolean shift) {
        this.keyCode = keyCode;
        this.ctrl = ctrl;
        this.shift = shift;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Shortcut shortcut = (Shortcut) obj;
        return this.keyCode == shortcut.keyCode && this.ctrl == shortcut.ctrl && this.shift == shortcut.shift;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.keyCode;
        hash = 29 * hash + (this.ctrl ? 1 : 0);
        hash = 29 * hash + (this.shift ? 1 : 0);
        return hash;
    }

}
