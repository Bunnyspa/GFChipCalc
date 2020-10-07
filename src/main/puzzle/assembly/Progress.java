package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.setting.Setting;

/**
 *
 * @author Bunnyspa
 */
public class Progress {

    // Progress
    public int nComb;
    public int nDone;
    public int nTotal;
    private TreeSet<Board> boards;

    public Progress(int sortType) {
        this.nDone = 0;
        this.boards = new TreeSet<>(getComparator(sortType));
    }

    public Progress(int sortType, int nComb, int progress, int progMax, List<Board> boards) {
        this.nComb = nComb;
        this.nDone = progress;
        this.nTotal = progMax;

        this.boards = new TreeSet<>(getComparator(sortType));
        this.boards.addAll(boards);
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
                ? (o1, o2) -> {
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
                : (o1, o2) -> {
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
                };
    }

    public List<ChipFreq> getChipFreqs() {
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, Double> percMap = new HashMap<>();
        Map<String, Chip> chipIDMap = new HashMap<>();
        for (Board b : boards) {
            for (String id : b.getChipIDs()) {
                if (!countMap.containsKey(id)) {
                    countMap.put(id, 0);
                    percMap.put(id, 0.0);
                    chipIDMap.put(id, new Chip(b.getChip(id)));
                }
                countMap.put(id, countMap.get(id) + 1);
                percMap.put(id, Math.max(percMap.get(id), b.getStatPerc()));
            }
        }

        double max = 1;

        for (String id : countMap.keySet()) {
            double val = countMap.get(id) * percMap.get(id);
            if (max < val) {
                max = val;
            }
        }

        final double max_ = max;

        List<ChipFreq> out = new ArrayList<>(countMap.size());
        for (String id : countMap.keySet()) {
            int count = countMap.get(id);
            Chip c = chipIDMap.get(id);
            c.resetRotation();
            c.resetLevel();
            double freq = count * percMap.get(id) / max_;
            out.add(new ChipFreq(c, count, freq));
        }

        Collections.sort(out, (o1, o2) -> Double.compare(o2.freq, o1.freq));

        return out;
    }
}
