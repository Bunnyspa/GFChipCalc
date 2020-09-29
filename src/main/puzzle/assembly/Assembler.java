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
import main.App;
import main.puzzle.Board;
import main.puzzle.BoardTemplate;
import main.puzzle.BoardTemplateMap;
import main.puzzle.Chip;
import main.puzzle.Puzzle;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Shape;
import main.puzzle.zdd.ZDD;
import main.puzzle.zdd.ZDDMemoCache;
import main.puzzle.zdd.ZDDNode;
import main.puzzle.zdd.ZDDNodeTable;
import main.setting.Setting;
import main.util.IO;

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
    private final BoardTemplateMap fullBTM, partialBTM;

    private Progress progress;
    private boolean boardsChanged;

    private volatile Status status = Status.STOPPED;
    private final BooleanSupplier checkPause = () -> checkPause();

    public Assembler(App app) {
        this.app = app;
        // Full BoardTemplate
        fullBTM = new BoardTemplateMap();
        for (String name : Board.NAMES) {
            for (int star = 1; star <= 5; star++) {
                List<BoardTemplate> templates = IO.loadBoardTemplates(name, star, false);
                Shape.Type minType = Shape.Type._5A;
                if (Board.NAME_M2.equals(name)
                        || (Board.NAME_MK153.equals(name) && star <= 2)) {
                    minType = Shape.Type._4;
                } else if (Board.NAME_MK153.equals(name) && star == 5) {
                    minType = Shape.Type._5B;
                }
                fullBTM.put(name, star, templates, minType);
            }
        }
        // Partial BoardTemplate
        partialBTM = new BoardTemplateMap();
        List<BoardTemplate> templates = IO.loadBoardTemplates(Board.NAME_M2, 5, true);
        partialBTM.put(Board.NAME_M2, 5, templates, Shape.Type._5B);
    }

    public boolean btExists(String name, int star, boolean alt) {
        return getBT(name, star, alt) != null;
    }

    private List<BoardTemplate> getBT(String name, int star, boolean alt) {
        return alt ? partialBTM.get(name, star) : fullBTM.get(name, star);
    }

    public Shape.Type getMinType(String name, int star, boolean alt) {
        return alt ? partialBTM.getMinType(name, star) : fullBTM.getMinType(name, star);
    }

    public boolean hasPartial(String name, int star) {
        return partialBTM.containsKey(name, star);
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

    public synchronized void publishBoard(Board board) {
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

        if (progress.settingRotation || board.getTicketCount() == 0) {
            progress.addBoard(board);
            if (RESULT_LIMIT > 0 && progress.getBoardSize() > RESULT_LIMIT) {
                progress.removeLastBoard();
            }
            progress.nComb++;
            boardsChanged = true;
        }
    }

    public static BoardTemplate generateBT(String boardName, int boardStar, List<Shape> shapes, BooleanSupplier checkPause) {
        return generateBT_DXZ(boardName, boardStar, shapes, checkPause);
    }

    private static BoardTemplate generateBT_DXZ(String boardName, int boardStar, List<Shape> shapes, BooleanSupplier checkPause) {
        PuzzleMatrix<Integer> puzzle = Board.initMatrix(boardName, boardStar);

        Set<Point> emptyCoords = puzzle.getCoords(Board.EMPTY);

        int nCol_name = shapes.size();
        int nCol_cell = puzzle.getNumContaining(Board.EMPTY);
        int nCol = nCol_name + nCol_cell;

        List<Point> cols_pt = new ArrayList<>(nCol_cell);
        puzzle.getCoords(Board.EMPTY).forEach((p) -> cols_pt.add(p));

        List<Shape> rows_shape = new ArrayList<>();
        List<Integer> rows_rotation = new ArrayList<>();
        List<Point> rows_location = new ArrayList<>();

        List<boolean[]> rows = new ArrayList<>();
        for (int i = 0; i < nCol_name; i++) {
            Shape shape = shapes.get(i);
            Chip c = new Chip(shape);
            for (int rot = 0; rot < c.getMaxRotation(); rot++) {
                c.setRotation(rot);
                for (Point bp : emptyCoords) {
                    Set<Point> tps = translate(c, bp);
                    if (Board.isChipPlaceable(puzzle, tps)) {
                        boolean[] row = new boolean[nCol];
                        row[i] = true;
                        tps.forEach((p) -> row[nCol_name + cols_pt.indexOf(p)] = true);
                        rows.add(row);
                        rows_shape.add(shape);
                        rows_rotation.add(rot);
                        rows_location.add(bp);
                    }
                }
            }
        }

        if (rows.isEmpty()) {
            return BoardTemplate.empty();
        }

        BinaryMatrix matrix = new BinaryMatrix(rows);
        ZDDNode node = DXZ(matrix, nCol_name, checkPause);
        if (node == null) {
            return BoardTemplate.empty();
        }
        Set<Set<Integer>> results = node.get();
        for (Set<Integer> resultRows : results) {

            List<Puzzle> puzzles = new ArrayList<>();

            List<Integer> sortedRows = new ArrayList<>(resultRows);
            sortedRows.sort((o1, o2) -> Shape.compare(rows_shape.get(o1), rows_shape.get(o2)));
            sortedRows.forEach((r) -> {
                puzzles.add(new Puzzle(
                        rows_shape.get(r),
                        rows_rotation.get(r),
                        rows_location.get(r)
                ));
            });
            BoardTemplate bt = new BoardTemplate(boardName, boardStar, puzzles);
            return bt;
        }
        return BoardTemplate.empty();
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
        BlockingQueue<BoardTemplate> q = new ArrayBlockingQueue<>(5);
        ChipCombinationIterator cIt = new ChipCombinationIterator(progress.chips);

        Thread templateThread = new Thread(() -> combine_template(q, cIt));
        Thread assembleThread = new Thread(() -> combine_assemble(q, cIt));

        templateThread.start();
        assembleThread.start();
        try {
            templateThread.join();
            assembleThread.join();
        } catch (InterruptedException ex) {
        }
    }

    private void combine_template(BlockingQueue<BoardTemplate> q, ChipCombinationIterator cIt) {
        // Dictionary
        if (progress.status == Progress.DICTIONARY) {
            List<BoardTemplate> templates = getBT(progress.name, progress.star, progress.tag == 1);

            progress.nTotal = (int) templates.stream()
                    .filter((p) -> cIt.hasEnoughChips(p))
                    .filter((p) -> !progress.settingSymmetry || p.isSymmetric())
                    .count();

            setProgBar();

            Iterator<BoardTemplate> pIt = templates.stream().skip(progress.nDone)
                    .filter((p) -> cIt.hasEnoughChips(p))
                    .filter((p) -> !progress.settingSymmetry || p.isSymmetric())
                    .iterator();
            while (checkPause() && pIt.hasNext()) {
                BoardTemplate template = pIt.next();
                offer(q, template);
            }
        } //
        // Algorithm X
        else {
            TCIHandler it = new TCIHandler(progress.name, progress.star, progress.chips);
            progress.nTotal = it.total();
            setProgBar();

            it.skip(progress.nDone);
            while (checkPause() && it.hasNext()) {
                BoardTemplate template = combine_template_algX(it);
                offer(q, template);
            }
        }
        offer(q, BoardTemplate.end());
    }

    private BoardTemplate combine_template_algX(TCIHandler iterator) {
        if (!iterator.isNextValid()) {
            iterator.skip();
            return BoardTemplate.empty();
        }
        List<Shape> shapes = iterator.next();
        return generateBT(progress.name, progress.star, shapes, checkPause);
    }

    private void combine_assemble(BlockingQueue<BoardTemplate> q, ChipCombinationIterator cIt) {
        progress.setComparator();
        while (checkPause()) {
            BoardTemplate template = poll(q);
            if (template == null || template.isEnd()) {
                return;
            }
            if (!template.isEmpty()) {
                // Show progress
                app.mf.process_showImage(template);
                cIt.init(template);
                // For all combinations
                while (checkPause() && cIt.hasNext()) {
                    List<Chip> candidates = cIt.next();
                    // Check PT
                    boolean addable = true;
                    int[] pt = progress.pt.toArray();
                    for (Chip c : candidates) {
                        int[] cpt = c.getPt().toArray();
                        for (int i = 0; i < 4; i++) {
                            pt[i] -= cpt[i];
                            if (pt[i] < 0) {
                                addable = false;
                                break;
                            }
                        }

                    }

                    // add
                    if (addable) {
                        List<Chip> chips = new ArrayList<>();
                        for (int i = 0; i < candidates.size(); i++) {
                            Chip c = new Chip(candidates.get(i));
                            c.setRotation(template.getChipRotations().get(i));
                            chips.add(c);
                        }
                        publishBoard(new Board(progress.name, progress.star, progress.stat, chips, template.getChipLocations()));
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

    private void offer(BlockingQueue<BoardTemplate> q, BoardTemplate template) {
        while (checkPause() && !q.offer(template)) {
            wait_();
        }
    }

    private BoardTemplate poll(BlockingQueue<BoardTemplate> q) {
        BoardTemplate template = null;
        while (checkPause() && null == (template = q.poll())) {
            wait_();
        }
        return template;
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
