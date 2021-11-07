package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.List;
import main.util.IO;

public class ProgressFile {

    public final CalcSetting cs;
    public final CalcExtraSetting ces;
    public final Progress p;

    public ProgressFile(CalcSetting cs, CalcExtraSetting ces, Progress p) {
        this.cs = cs;
        this.ces = ces;
        this.p = p;
    }

    public String toData() {
        List<String> lines = new ArrayList<>();

        lines.add(String.valueOf(ces.calcMode));

        lines.add(cs.unit.getName());
        lines.add(String.valueOf(cs.unitStar));

        lines.add(IO.data(cs.maxLevel));
        lines.add(IO.data(ces.matchColor));
        lines.add(IO.data(cs.rotation));
        lines.add(IO.data(cs.symmetry));

        lines.add(String.valueOf(ces.markMin));
        lines.add(String.valueOf(ces.markMax));
        lines.add(String.valueOf(ces.markType));
        lines.add(String.valueOf(ces.sortType));

        lines.add(cs.stat.toData());
        lines.add(cs.pt.toData());

        lines.add(String.valueOf(p.nComb));
        lines.add(String.valueOf(p.nDone));
        lines.add(String.valueOf(p.nTotal));
        lines.add(String.valueOf(ces.calcModeTag));

        // Chips
        lines.add(String.valueOf(ces.chips.size()));
        ces.chips.forEach((c) -> lines.add(c.toData()));

        // Boards
        p.getBoards().forEach((b) -> {
            lines.add(String.valueOf(b.getChipCount()));
            b.forEachChip((c) -> lines.add(
                    String.valueOf(ces.chips.indexOf(c)) + ","
                    + String.valueOf(c.getRotation()) + ","
                    + IO.data(b.getLocation(c))
            ));
        });
        // Done
        return String.join(System.lineSeparator(), lines);
    }
}
