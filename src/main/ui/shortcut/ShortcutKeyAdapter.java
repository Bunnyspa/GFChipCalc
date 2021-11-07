package main.ui.shortcut;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class ShortcutKeyAdapter extends KeyAdapter {

    Map<Shortcut, Runnable> map = new HashMap<>();

    public void addShortcut(int keyCode, Runnable r) {
        map.put(new Shortcut(keyCode), r);
    }

    public void addShortcut_c(int keyCode, Runnable r) {
        map.put(new Shortcut(keyCode, true), r);
    }

    public void addShortcut_cs(int keyCode, Runnable r) {
        map.put(new Shortcut(keyCode, true, true), r);
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        int key = evt.getKeyCode();
        boolean ctrl = evt.isControlDown();
        boolean shift = evt.isShiftDown();
        Shortcut sc;
        if (map.containsKey(sc = new Shortcut(key, ctrl, shift))) {
            map.get(sc).run();
        } else if (map.containsKey(sc = new Shortcut(key, ctrl))) {
            map.get(sc).run();
        } else if (map.containsKey(sc = new Shortcut(key))) {
            map.get(sc).run();
        }
    }

}
