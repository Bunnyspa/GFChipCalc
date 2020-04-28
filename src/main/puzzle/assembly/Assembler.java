package main.puzzle.assembly;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Stat;
import main.puzzle.preset.PuzzlePreset;
import main.puzzle.preset.PuzzlePresetMap;
import main.puzzle.zdd.ZDD;
import main.puzzle.zdd.ZDDMemoCache;
import main.puzzle.zdd.ZDDNode;
import main.puzzle.zdd.ZDDNodeTable;
import main.resource.Resources;
import main.setting.Setting;

/**
 *
 * @author Bunnyspa
 */
public class Assembler {

    public enum Status {
        STOPPED, RUNNING, PAUSED
    }

    private static final int RESULT_LIMIT = 100;

    private final App app;
    private final PuzzlePresetMap fullPM, partialPM;

    private Progress progress;
    private boolean boardsChanged;

    private volatile Status status = Status.STOPPED;
    private final BooleanSupplier checkPause = () -> checkPause();

    public Assembler(App app) {
        this.app = app;
        // Full Preset
        fullPM = new PuzzlePresetMap();
        for (String name : Board.NAMES) {
            for (int star = 1; star <= 5; star++) {
                String minType;
                if (Board.NAME_M2.equals(name)) {
                    minType = Chip.TYPE_4;
                } else {
                    minType = Chip.TYPE_5A;
                }
                List<PuzzlePreset> presets = Resources.loadPresets(name, star, minType, false);
                if (!presets.isEmpty()) {
                    fullPM.put(name, star, presets, minType);
                }
            }
        }
        // Partial Preset
        partialPM = new PuzzlePresetMap();
        partialPM.put(Board.NAME_M2, 5, Resources.loadPresets(Board.NAME_M2, 5, Chip.TYPE_5B, true), Chip.TYPE_5B);
    }

    public boolean presetExists(String name, int star, boolean alt) {
        return getPresets(name, star, alt) != null;
    }

    private List<PuzzlePreset> getPresets(String name, int star, boolean alt) {
        return alt ? partialPM.get(name, star) : fullPM.get(name, star);
    }

    public String getMinType(String name, int star, boolean alt) {
        return alt ? partialPM.getMinType(name, star) : fullPM.getMinType(name, star);
    }

    public boolean hasPartial(String name, int star) {
        return partialPM.containsKey(name, star);
    }

    public void set(Progress p) {
        progress = p;
        new Thread(() -> {
            if (status == Status.STOPPED) {
                status = Status.PAUSED;
            }
            if (p.status != Progress.FINISHED) {
                combine();
            } else {
                setProgBar();
            }
            status = Status.STOPPED;
            app.mf.process_stop();
        }).start();
    }

    private void prog_inc() {
        progress.nDone++;
        app.mf.process_prog(progress.nDone);
    }

    public void pause() {
        status = Status.PAUSED;
    }

    public void resume() {
        status = Status.RUNNING;
    }

    public void stop() {
        status = Status.STOPPED;
    }

    public Status getStatus() {
        return status;
    }

    public boolean boardsUpdated() {
        return boardsChanged;
    }

    public AssemblyResult getResult() {
        boardsChanged = false;
        return new AssemblyResult(progress.getBoards(), progress.getChipFreqs());
    }

    private void setProgBar() {
        app.mf.process_setProgBar(progress.nDone, progress.nTotal);
    }

    public void publishBoard(Board board) {
        switch (progress.markType) {
            case Setting.BOARD_MARKTYPE_CELL:
                if (board.getMarkedCellCount() < progress.markMin || progress.markMax < board.getMarkedCellCount()) {
                    return;
                }
                break;
            case Setting.BOARD_MARKTYPE_CHIP:
                if (board.getMarkedChipCount() < progress.markMin || progress.markMax < board.getMarkedChipCount()) {
                    return;
                }
                break;
            default:
                throw new AssertionError();
        }

        board.minimizeTicket();
        board.colorChips();

        if (progress.allowRotation || board.getTicketCount() == 0) {
            if (progress.isBoardEmpty()) {
                progress.addBoard(board);
            } else {
                int i = 0;
                int n = progress.getBoardSize();
                boolean insert = false;
                while (i < n && !insert) {
                    Board cb = progress.getBoard(i);
                    if (board.getStatPerc() > cb.getStatPerc()) {
                        insert = true;
                    } else if (board.getStatPerc() == cb.getStatPerc()) {
                        switch (progress.sortType) {
                            case Setting.BOARD_SORTTYPE_XP:
                                if (board.getXP() < cb.getXP()
                                        || (board.getXP() == cb.getXP() && board.getTicketCount() < cb.getTicketCount())) {
                                    insert = true;
                                }
                                break;
                            case Setting.BOARD_SORTTYPE_TICKET:
                                if (board.getTicketCount() < cb.getTicketCount()
                                        || (board.getTicketCount() == cb.getTicketCount() && board.getXP() < cb.getXP())) {
                                    insert = true;
                                }
                                break;
                            default:
                                throw new AssertionError();
                        }
                    }
                    i++;
                }
                progress.addBoard(i - (insert ? 1 : 0), board);
                if (RESULT_LIMIT > 0 && progress.getBoardSize() > RESULT_LIMIT) {
                    progress.removeLastBoard();
                }
            }
            progress.nComb++;
            boardsChanged = true;
        }
    }

    public static PuzzlePreset genPreset(String boardName, int boardStar, List<String> names, BooleanSupplier checkPause) {
        return genPreset_DXZ(boardName, boardStar, names, checkPause);
    }

    private static PuzzlePreset genPreset_DXZ(String boardName, int boardStar, List<String> names, BooleanSupplier checkPause) {
        PuzzleMatrix<Integer> puzzle = Board.initMatrix(boardName, boardStar);

        Set<Point> emptyCoords = puzzle.getCoords(Board.EMPTY);

        int nCol_name = names.size();
        int nCol_cell = puzzle.getNumContaining(Board.EMPTY);
        int nCol = nCol_name + nCol_cell;

        List<Point> cols_pt = new ArrayList<>(nCol_cell);
        puzzle.getCoords(Board.EMPTY).forEach((p) -> cols_pt.add(p));

        List<String> rows_name = new ArrayList<>();
        List<Integer> rows_rot = new ArrayList<>();
        List<Point> rows_loc = new ArrayList<>();

        List<boolean[]> rows = new ArrayList<>();
        for (int i = 0; i < nCol_name; i++) {
            String name = names.get(i);
            Chip c = new Chip(name);
            for (int rot = 0; rot < c.getMaxRotation(); rot++) {
                c.setRotation(rot);
                for (Point bp : emptyCoords) {
                    Set<Point> tps = translate(c, bp);
                    if (Board.isChipPlaceable(puzzle, tps)) {
                        boolean[] row = new boolean[nCol];
                        row[i] = true;
                        tps.forEach((p) -> row[nCol_name + cols_pt.indexOf(p)] = true);
                        rows.add(row);
                        rows_name.add(name);
                        rows_rot.add(rot);
                        rows_loc.add(bp);
                    }
                }
            }
        }

        if (rows.isEmpty()) {
            return PuzzlePreset.empty();
        }

        BinaryMatrix matrix = new BinaryMatrix(rows);
        ZDDNode node = DXZ(matrix, nCol_name, checkPause);
        if (node == null) {
            return PuzzlePreset.empty();
        }
        Set<Set<Integer>> results = node.get();
        for (Set<Integer> resultRows : results) {
            List<String> rNames = new ArrayList<>();
            List<Integer> rRots = new ArrayList<>();
            List<Point> rLocs = new ArrayList<>();
            List<Integer> sortedRows = new ArrayList<>(resultRows);
            sortedRows.sort((o1, o2) -> Chip.compareName(rows_name.get(o1), rows_name.get(o2)));
            sortedRows.forEach((r) -> {
                rNames.add(rows_name.get(r));
                rRots.add(rows_rot.get(r));
                rLocs.add(rows_loc.get(r));
            });
            PuzzlePreset preset = new PuzzlePreset(boardName, boardStar, rNames, rRots, rLocs);
            return preset;
        }
        return PuzzlePreset.empty();
    }

    private static Set<Point> translate(Chip c, Point bp) {
        Point cfp = c.getPivot();
        Set<Point> cps = c.getAllCoords();
        cps.forEach((cp) -> cp.translate(bp.x - cfp.x, bp.y - cfp.y));
        return cps;
    }

    private static class DXZResult {

        public final ZDDNode node;
        public final boolean trueTerminalFound;

        public DXZResult(ZDDNode node, boolean trueTerminalFound) {
            this.node = node;
            this.trueTerminalFound = trueTerminalFound;
        }
    }

    private static ZDDNode DXZ(BinaryMatrix X, int nCol_name, BooleanSupplier checkPause) {
        ZDDMemoCache C = new ZDDMemoCache();
        ZDDNodeTable Z = new ZDDNodeTable();
        return DXZ_search(X, C, Z, nCol_name, checkPause).node;
    }

    private static DXZResult DXZ_search(BinaryMatrix A, ZDDMemoCache C, ZDDNodeTable Z, int nCol_name, BooleanSupplier checkPause) {
        if (A.isEmpty()) {
            return new DXZResult(ZDDNode.TRUE_TERMINAL, true);
        }
        Set<Integer> colA = A.getCols();
        if (C.containsKey(colA)) {
            return new DXZResult(C.get(colA), false);
        }
        Set<Integer> rowA = A.getRows();
        Optional<Integer> cOp = colA.stream().filter((col) -> col <= nCol_name).findAny();
        if (!cOp.isPresent()) {
            return null;
        }
        int c = cOp.get();
        ZDDNode x = null;
        for (int r : rowA) {
            if (checkPause.getAsBoolean() && A.get(r, c)) {
                A.hideRowChain(r);
                DXZResult result = DXZ_search(A, C, Z, nCol_name, checkPause);
                ZDDNode y = result.node;
                if (y != null) {
                    x = ZDD.unique(r, x, y, Z);
                }
                if (result.trueTerminalFound) {
                    return new DXZResult(x, true);
                }
                A.revertLastHiding();
            }
        }
        C.put(colA, x);
        return new DXZResult(x, false);
    }

    private void combine() {
        BlockingQueue<PuzzlePreset> q = new ArrayBlockingQueue<>(5);
        ChipCombinationIterator cIt = new ChipCombinationIterator(progress.chips);

        Thread presetThread = new Thread(() -> combine_p(q, cIt));
        Thread combineThread = new Thread(() -> combine_c(q, cIt));

        presetThread.start();
        combineThread.start();
        try {
            presetThread.join();
            combineThread.join();
        } catch (InterruptedException ex) {
        }
    }

    private void combine_p(BlockingQueue<PuzzlePreset> q, ChipCombinationIterator cIt) {
        // Dictionary
        if (progress.status == Progress.DICTIONARY) {
            List<PuzzlePreset> presets = getPresets(progress.name, progress.star, progress.tag == 1);

            progress.nTotal = (int) presets.stream()
                    .filter((p) -> cIt.hasEnoughChips(p))
                    .filter((p) -> !progress.symmetry || p.isSymmetric())
                    .count();

            setProgBar();

            Iterator<PuzzlePreset> pIt = presets.stream().skip(progress.nDone)
                    .filter((p) -> cIt.hasEnoughChips(p))
                    .filter((p) -> !progress.symmetry || p.isSymmetric())
                    .iterator();
            while (checkPause() && pIt.hasNext()) {
                PuzzlePreset preset = pIt.next();
                offer(q, preset);
            }
        } //
        // Algorithm X
        else {
            PresetCombinationIterator it = new PresetCombinationIterator(progress.name, progress.star, progress.chips);
            progress.nTotal = it.total();
            setProgBar();

            it.skip(progress.nDone);
            while (checkPause() && it.hasNext()) {
                PuzzlePreset preset = combine_p_algX_genPreset(it);
                offer(q, preset);
            }
        }
        offer(q, PuzzlePreset.end());
    }

    private PuzzlePreset combine_p_algX_genPreset(PresetCombinationIterator iterator) {
        if (!iterator.isNextValid()) {
            iterator.skip();
            return PuzzlePreset.empty();
        }
        List<String> names = iterator.next();
        return genPreset(progress.name, progress.star, names, checkPause);
    }

    private void combine_c(BlockingQueue<PuzzlePreset> q, ChipCombinationIterator cIt) {
        while (checkPause()) {
            PuzzlePreset preset = poll(q);
            if (preset == null || preset.isEnd()) {
                return;
            }
            if (!preset.isEmpty()) {
                // Show progress
                app.mf.process_showImage(preset);
                cIt.init(preset);
                // For all combinations
                while (checkPause() && cIt.hasNext()) {
                    List<Chip> candidates = cIt.next();
                    // Check PT
                    boolean addable = true;
                    Stat pt = progress.pt.toStat();
                    for (Chip c : candidates) {
                        pt.subtract(c.getPt());
                        if (pt.anyNeg()) {
                            addable = false;
                            break;
                        }
                    }

                    // add
                    if (addable) {
                        List<Chip> chips = new ArrayList<>();
                        for (int i = 0; i < candidates.size(); i++) {
                            Chip c = new Chip(candidates.get(i));
                            c.setRotation(preset.getChipRotations().get(i));
                            chips.add(c);
                        }
                        publishBoard(new Board(progress.name, progress.star, progress.stat.toStat(), chips, preset.getChipLocations()));
                    }
                }
            }
            prog_inc();
        }
    }

    private synchronized boolean checkPause() {
        while (status == Status.PAUSED) {
            wait_();
        }
        return status == Status.RUNNING;
    }

    private void offer(BlockingQueue<PuzzlePreset> q, PuzzlePreset preset) {
        while (checkPause() && !q.offer(preset)) {
            wait_();
        }
    }

    private PuzzlePreset poll(BlockingQueue<PuzzlePreset> q) {
        PuzzlePreset preset = null;
        while (checkPause() && null == (preset = q.poll())) {
            wait_();
        }
        return preset;
    }

    private synchronized void wait_() {
        try {
            wait(10);
        } catch (InterruptedException ex) {
        }
    }
//
//    private static class BMLabel {
//
//        final List<String> rows_name;
//        final List<Integer> rows_rot;
//        final List<Point> rows_loc;
//
//        public BMLabel(List<String> rows_name, List<Integer> rows_rot, List<Point> rows_loc) {
//            this.rows_name = rows_name;
//            this.rows_rot = rows_rot;
//            this.rows_loc = rows_loc;
//        }
//    }
}
