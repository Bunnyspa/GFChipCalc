package main.puzzle.assembly.dxz.zdd;

/**
 *
 * @author Bunnyspa
 */
public class ZDD {

    public static final ZDDNode TRUE_TERMINAL = new ZDDNode();

    public static ZDDNode unique(int i, ZDDNode l, ZDDNode h, ZDDNodeTable Z) {
        ZDDNode element = Z.get(i, l, h);
        if (element != null) {
            return element;
        }

        ZDDNode node = new ZDDNode(i, l, h);
        Z.add(node);
        return node;
    }
}
