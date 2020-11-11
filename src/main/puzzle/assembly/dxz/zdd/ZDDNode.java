package main.puzzle.assembly.dxz.zdd;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class ZDDNode {

    private final UUID uuid = UUID.randomUUID();

    final int label;
    final ZDDNode loChild; // false terminal
    final ZDDNode hiChild; // true terminal

    ZDDNode() {
        this.label = 0;
        this.loChild = null;
        this.hiChild = null;
    }

    ZDDNode(int i, ZDDNode l, ZDDNode h) {
        this.label = i;
        this.loChild = l;
        this.hiChild = h;
    }

    boolean isTerminal() {
        return hiChild == null;
    }

    boolean equals(int i, ZDDNode l, ZDDNode h) {
        return label == i && loChild == l && hiChild == h;
    }

    public Set<Set<Integer>> get() {
        Set<Set<Integer>> out = new HashSet<>();
        if (isTerminal()) {
            out.add(new HashSet<>());
            return out;
        }
        hiChild.get().forEach((set) -> {
            Set<Integer> e = new HashSet<>(set);
            e.add(label);
            out.add(e);
        });
        if (loChild != null) {
            loChild.get().forEach((set) -> {
                Set<Integer> e = new HashSet<>(set);
                out.add(e);
            });
        }
        return out;
    }

    @Override
    public String toString() {
        return toString_tree(0);
    }

    private String toString_tree(int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(" ").append(uuid.toString().substring(0, 4));
        String pad = Fn.pad("  ", depth);
        if (hiChild != null && !hiChild.isTerminal()) {
            sb.append(System.lineSeparator()).append(pad).append("+").append(hiChild.toString_tree(depth + 1));
        }
        if (loChild != null) {
            sb.append(System.lineSeparator()).append(pad).append("-").append(loChild.toString_tree(depth + 1));
        }
        return sb.toString();
    }

}
