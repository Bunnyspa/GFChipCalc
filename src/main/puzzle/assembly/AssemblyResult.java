package main.puzzle.assembly;

import java.util.List;
import main.puzzle.Board;

/**
 *
 * @author Bunnyspa
 */
public class AssemblyResult {
    public final List<Board> boards;
    public final List<ChipFreq> freqs;

    public AssemblyResult(List<Board> boards, List<ChipFreq> freqs) {
        this.boards = boards;
        this.freqs = freqs;
    }
}
