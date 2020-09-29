package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import main.puzzle.Board;
import main.puzzle.Chip;
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
    public final boolean settingLevelMax, settingColorMatch, settingRotation, settingSymmetry;
    public final int markMin, markMax, markType, sortType;
    public final Stat pt, stat;
    public final int tag;

    // Progress
    public int nComb;
    public int nDone;
    public int nTotal;
    private TreeSet<Board> boards;

    public Progress(int status, String name, int star,
            List<Chip> chips, Setting setting, int tag) {
        this.status = status;

        this.name = name;
        this.star = star;
        this.chips = chips;

        this.settingLevelMax = setting.levelMax;
        this.settingColorMatch = setting.colorMatch;
        this.settingRotation = setting.rotation;
        this.settingSymmetry = setting.symmetry;

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
        this.tag = tag;
    }

    public Progress(int status, String name, int star,
            boolean maxLevel, boolean matchColor, boolean allowRotation, boolean symmetry,
            int markMin, int markMax, int markType, int sortType,
            Stat stat, Stat pt,
            int nComb, int progress, int progMax,
            List<Chip> chips, List<Board> boards,
            int tag) {
        this.status = status;

        this.name = name;
        this.star = star;

        this.settingLevelMax = maxLevel;
        this.settingColorMatch = matchColor;
        this.settingRotation = allowRotation;
        this.settingSymmetry = symmetry;

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
        this.boards = new TreeSet<>(getComparator(sortType));
        this.boards.addAll(boards);

        this.tag = tag;
    }

    private static Stat getStat(String name, int star, int mode, int presetIndex, Stat stat) {
        switch (mode) {
            case BoardSetting.MAX_PRESET:
                return BoardSetting.getPreset(name, star, presetIndex).stat;
            case BoardSetting.MAX_STAT:
                return stat;
            default:
                return Board.getMaxStat(name, star);
        }
    }

    private static Stat getPT(String name, int star, int mode, int presetIndex, Stat pt, Stat stat) {
        switch (mode) {
            case BoardSetting.MAX_PRESET:
                return BoardSetting.getPreset(name, star, presetIndex).pt;
            case BoardSetting.MAX_STAT:
                return Board.getMaxPt(name, star, stat);
            case BoardSetting.MAX_PT:
                return pt;
            default:
                return Board.getMaxPt(name, star);
        }
    }

    public List<Board> getBoards() {
        return new ArrayList<>(boards);
    }

    public int getBoardSize() {
        return boards.size();
    }

    public void addBoard(Board board) {
        boards.add(board);
    }

    public void removeLastBoard() {
        boards.pollLast();
    }

    private static Comparator<Board> getComparator(int sortType) {
        return sortType == Setting.BOARD_SORTTYPE_XP
                ? new Comparator<Board>() {
            @Override
            public int compare(Board o1, Board o2) {
                int percent = Double.compare(o2.getStatPerc(), o1.getStatPerc());
                if (percent != 0) {
                    return percent;
                }
                int xp = Integer.compare(o1.getXP(), o2.getXP());
                if (xp != 0) {
                    return xp;
                }
                int ticket = Integer.compare(o1.getTicketCount(), o2.getTicketCount());
                if (ticket != 0) {
                    return ticket;
                }
                return o1.compareTo(o2);
            }
        }
                : new Comparator<Board>() {
            @Override
            public int compare(Board o1, Board o2) {
                int percent = Double.compare(o2.getStatPerc(), o1.getStatPerc());
                if (percent != 0) {
                    return percent;
                }
                int ticket = Integer.compare(o1.getTicketCount(), o2.getTicketCount());
                if (ticket != 0) {
                    return ticket;
                }
                int xp = Integer.compare(o1.getXP(), o2.getXP());
                if (xp != 0) {
                    return xp;
                }
                return o1.compareTo(o2);
            }
        };
    }

    public void setComparator() {
        TreeSet<Board> newBoards = new TreeSet<>(getComparator(sortType));
        if (boards != null && !boards.isEmpty()) {
            newBoards.addAll(boards);
        }
        boards = newBoards;
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

        out.sort((o1, o2) -> Double.compare(o2.freq, o1.freq));

        return out;
    }

    public String toData() {
        List<String> lines = new ArrayList<>();

        lines.add(String.valueOf(status));

        lines.add(name);
        lines.add(String.valueOf(star));

        lines.add(IO.data(settingLevelMax));
        lines.add(IO.data(settingColorMatch));
        lines.add(IO.data(settingRotation));
        lines.add(IO.data(settingSymmetry));

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
