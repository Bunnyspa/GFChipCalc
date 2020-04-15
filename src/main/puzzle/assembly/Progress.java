package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.FStat;
import main.puzzle.Stat;
import main.setting.BoardSetting;
import main.setting.Setting;
import main.util.IO;

/**
 *
 * @author Bunnyspa
 */
public class Progress {

    public static final int FINISHED = 0;
    public static final int DICTIONARY = 1;
    public static final int ALGX = 2;

    public final String name;
    public final int star;
    public final List<Chip> chips;

    // Setting
    public int status;
    public final boolean maxLevel, matchColor, allowRotation;
    public final int markMin, markMax, markType, sortType;
    public final FStat pt, stat;
    public final int tag;

    // Progress
    public int nComb;
    public int nDone;
    public int nTotal;
    private final List<Board> boards;

    public Progress(int status, String name, int star,
            List<Chip> chips, Setting setting, int tag) {
        this.status = status;

        this.name = name;
        this.star = star;
        this.chips = chips;

        this.maxLevel = setting.chipLevelMax;
        this.matchColor = setting.chipMatchColor;
        this.allowRotation = setting.chipAllowRotation;

        this.markMin = setting.boardMarkMin;
        this.markMax = setting.boardMarkMax;
        this.markType = setting.boardMarkType;
        this.sortType = setting.boardSortType;

        BoardSetting bs = setting.board;
        int mode = bs.getMode(name, star);
        int presetIndex = bs.getPresetIndex(name, star);
        Stat bsStat = bs.getStat(name, star);
        Stat bsPt = bs.getPt(name, star);
        this.stat = getStat(name, star, mode, presetIndex, bsStat);
        this.pt = getPT(name, star, mode, presetIndex, bsPt, bsStat);

        this.nDone = 0;
        this.boards = new ArrayList<>();
        this.tag = tag;
    }

    public Progress(int status, String name, int star,
            boolean maxLevel, boolean matchColor, boolean allowRotation,
            int markMin, int markMax, int markType, int sortType,
            FStat stat, FStat pt,
            int nComb, int progress, int progMax,
            List<Chip> chips, List<Board> boards,
            int tag) {
        this.status = status;

        this.name = name;
        this.star = star;

        this.maxLevel = maxLevel;
        this.matchColor = matchColor;
        this.allowRotation = allowRotation;

        this.markMin = markMin;
        this.markMax = markMax;
        this.markType = markType;
        this.sortType = sortType;

        this.stat = stat;
        this.pt = pt;

        this.nComb = nComb;
        this.nDone = progress;
        this.nTotal = progMax;

        this.chips = chips;
        this.boards = boards;

        this.tag = tag;
    }

    private static FStat getStat(String name, int star, int mode, int presetIndex, Stat stat) {
        switch (mode) {
            case BoardSetting.MAX_PRESET:
                return BoardSetting.getPreset(name, star, presetIndex).stat;
            case BoardSetting.MAX_STAT:
                return new FStat(stat);
            default:
                return new FStat(Board.getMaxStat(name, star));
        }
    }

    private static FStat getPT(String name, int star, int mode, int presetIndex, Stat pt, Stat stat) {
        switch (mode) {
            case BoardSetting.MAX_PRESET:
                return BoardSetting.getPreset(name, star, presetIndex).pt;
            case BoardSetting.MAX_STAT:
                return new FStat(Board.getMaxPt(name, star, stat));
            case BoardSetting.MAX_PT:
                return new FStat(pt);
            default:
                return new FStat(Board.getMaxPt(name, star));
        }
    }

    public List<Board> getBoards() {
        return new ArrayList<>(boards);
    }

    public boolean isBoardEmpty() {
        return boards.isEmpty();
    }

    public int getBoardSize() {
        return boards.size();
    }

    public void addBoard(Board board) {
        boards.add(board);
    }

    public void addBoard(int index, Board board) {
        boards.add(index, board);
    }

    public void removeLastBoard() {
        boards.remove(boards.size() - 1);
    }

    public Board getBoard(int index) {
        return boards.get(index);
    }

    public List<ChipFreq> getChipFreqs() {
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, Double> percMap = new HashMap<>();
        Map<String, Chip> chipIDMap = new HashMap<>();
        boards.forEach((b) -> {
            b.getChipIDs().forEach((id) -> {
                if (!countMap.containsKey(id)) {
                    countMap.put(id, 0);
                    percMap.put(id, 0.0);
                    chipIDMap.put(id, new Chip(b.getChip(id)));
                }
                countMap.put(id, countMap.get(id) + 1);
                percMap.put(id, Double.max(percMap.get(id), b.getStatPerc()));
            });
        });

        double max = 1;

        for (String id : countMap.keySet()) {
            double val = countMap.get(id) * percMap.get(id);
            if (max < val) {
                max = val;
            }
        }

        final double max_ = max;

        List<ChipFreq> out = new ArrayList<>(countMap.size());
        countMap.keySet().forEach((id) -> {
            int count = countMap.get(id);
            Chip c = chipIDMap.get(id);
            c.resetRotation();
            c.resetLevel();
            double freq = count * percMap.get(id) / max_;
            out.add(new ChipFreq(c, count, freq));
        });

        out.sort((o1, o2) -> o1.freq == o2.freq ? 0 : (o1.freq < o2.freq) ? 1 : -1);

        return out;
    }

    public String toData() {
        List<String> lines = new ArrayList<>();

        lines.add(String.valueOf(status));

        lines.add(name);
        lines.add(String.valueOf(star));

        lines.add(IO.data(maxLevel));
        lines.add(IO.data(matchColor));
        lines.add(IO.data(allowRotation));

        lines.add(String.valueOf(markMin));
        lines.add(String.valueOf(markMax));
        lines.add(String.valueOf(markType));
        lines.add(String.valueOf(sortType));

        lines.add(stat.toData());
        lines.add(pt.toData());

        lines.add(String.valueOf(nComb));
        lines.add(String.valueOf(nDone));
        lines.add(String.valueOf(nTotal));
        lines.add(String.valueOf(tag));

        // Chips
        lines.add(String.valueOf(chips.size()));
        chips.forEach((c) -> lines.add(c.toData()));

        // Boards
        boards.forEach((b) -> {
            lines.add(String.valueOf(b.getChipCount()));
            b.forEachChip((c) -> lines.add(
                    String.valueOf(chips.indexOf(c)) + ","
                    + String.valueOf(c.getRotation()) + ","
                    + IO.data(b.getLocation(c))
            ));
        });
        // Done
        return String.join(System.lineSeparator(), lines);
    }
}
