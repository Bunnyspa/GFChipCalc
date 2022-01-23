package main.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.App;
import main.puzzle.Board;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.setting.Filter;
import main.setting.StatPreset;
import main.ui.resource.AppText;

public enum Unit {
    BGM71("BGM-71", Color.BLUE,
            // Grid
            new int[][]{
                {6, 6, 6, 6, 6, 6, 6, 6},
                {6, 4, 4, 4, 3, 3, 3, 6},
                {6, 4, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 5, 6},
                {6, 3, 3, 3, 5, 5, 5, 6},
                {6, 6, 6, 6, 6, 6, 6, 6}
            },
            // Innate Stat
            new Stat(155, 402, 349, 83),
            // Board Stats
            new Stat[]{
                new Stat(95, 165, 96, 23),
                new Stat(114, 198, 115, 28),
                new Stat(133, 231, 134, 32),
                new Stat(162, 280, 162, 39),
                new Stat(190, 329, 191, 46)
            },
            // Resonances
            new int[]{4, 10, 16, 22, 28, 32, 36},
            new Stat[]{
                new Stat(16, 0, 6, 0),
                new Stat(0, 8, 0, 3),
                new Stat(36, 0, 8, 0),
                new Stat(0, 14, 10, 0),
                new Stat(46, 0, 0, 6),
                new Stat(0, 18, 14, 0),
                new Stat(60, 26, 0, 0)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(4, 0, 6, 0),
                new Stat(0, 10, 9, 0),
                new Stat(5, 0, 0, 6),
                new Stat(0, 12, 10, 0),
                new Stat(7, 15, 0, 0),
                new Stat(8, 0, 12, 0),
                new Stat(0, 16, 14, 0),
                new Stat(9, 0, 0, 10),
                new Stat(0, 18, 18, 0),
                new Stat(12, 24, 0, 0)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5B,
                        new Stat(157, 328, 191, 45), new Stat(13, 10, 10, 3),
                        new Stat(1, 0, 0, 0), new Stat(3, 3, 3, 3)),
                new StatPreset(Shape.Type._5B,
                        new Stat(189, 328, 140, 45), new Stat(16, 10, 7, 3),
                        new Stat(1, 0, 1, 0), new Stat(4, 3, 1, 3))
            }
    ),
    AGS30("AGS-30", Color.ORANGE,
            // Grid
            new int[][]{
                {6, 6, 5, 5, 6, 6, 6, 6},
                {6, 3, 3, 2, 2, 6, 6, 6},
                {4, 3, 1, 1, 1, 1, 6, 6},
                {4, 2, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 2, 4},
                {6, 6, 1, 1, 1, 1, 3, 4},
                {6, 6, 6, 2, 2, 3, 3, 6},
                {6, 6, 6, 6, 5, 5, 6, 6}
            },
            // Innate Stat
            new Stat(78, 144, 198, 386),
            // Board Stats
            new Stat[]{
                new Stat(53, 65, 60, 117),
                new Stat(64, 78, 72, 140),
                new Stat(75, 91, 84, 163),
                new Stat(90, 111, 102, 198),
                new Stat(106, 130, 120, 233)
            },
            // Resonances
            new int[]{4, 10, 16, 24, 30, 34, 38},
            new Stat[]{
                new Stat(8, 0, 4, 0),
                new Stat(0, 4, 0, 8),
                new Stat(14, 0, 6, 0),
                new Stat(0, 8, 0, 10),
                new Stat(26, 0, 12, 0),
                new Stat(0, 14, 0, 12),
                new Stat(36, 0, 0, 16)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(2, 0, 5, 0),
                new Stat(0, 4, 0, 8),
                new Stat(3, 0, 8, 0),
                new Stat(0, 6, 0, 12),
                new Stat(3, 0, 0, 13),
                new Stat(4, 0, 12, 0),
                new Stat(0, 10, 0, 12),
                new Stat(4, 0, 16, 0),
                new Stat(0, 16, 0, 16),
                new Stat(8, 0, 0, 18)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5A,
                        null, null,
                        new Stat(), new Stat(3, 5, 5, 5))
            }
    ),
    _2B14("2B14", Color.ORANGE,
            // Grid
            new int[][]{
                {6, 6, 6, 6, 6, 6, 6, 6},
                {6, 6, 5, 6, 6, 5, 6, 6},
                {6, 2, 1, 1, 1, 1, 3, 6},
                {4, 2, 1, 1, 1, 1, 3, 4},
                {4, 2, 1, 1, 1, 1, 3, 4},
                {6, 2, 1, 1, 1, 1, 3, 6},
                {6, 6, 5, 6, 6, 5, 6, 6},
                {6, 6, 6, 6, 6, 6, 6, 6}
            },
            // Innate Stat
            new Stat(152, 58, 135, 160),
            // Board Stats
            new Stat[]{
                new Stat(114, 29, 45, 54),
                new Stat(136, 35, 54, 64),
                new Stat(159, 41, 63, 75),
                new Stat(193, 49, 77, 91),
                new Stat(227, 58, 90, 107)
            },
            // Resonances
            new int[]{4, 10, 16, 20, 24, 28, 32},
            new Stat[]{
                new Stat(16, 0, 6, 0),
                new Stat(0, 3, 0, 5),
                new Stat(36, 0, 0, 0),
                new Stat(0, 4, 8, 0),
                new Stat(58, 0, 0, 7),
                new Stat(0, 8, 0, 10),
                new Stat(82, 0, 8, 0)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(2, 0, 4, 0),
                new Stat(2, 3, 0, 4),
                new Stat(3, 0, 6, 0),
                new Stat(4, 4, 0, 4),
                new Stat(5, 0, 0, 5),
                new Stat(6, 0, 9, 0),
                new Stat(7, 3, 0, 6),
                new Stat(7, 0, 10, 0),
                new Stat(4, 5, 0, 9),
                new Stat(10, 0, 0, 6)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5A,
                        new Stat(227, 33, 90, 90), new Stat(20, 1, 5, 6),
                        new Stat(2, 0, 0, 0), new Stat(5, 1, 5, 3)),
                new StatPreset(Shape.Type._5A,
                        new Stat(227, 58, 80, 90), new Stat(20, 2, 4, 6),
                        new Stat(2, 0, 0, 0), new Stat(5, 2, 1, 3)),
                new StatPreset(Shape.Type._5B,
                        new Stat(220, 58, 90, 90), new Stat(19, 2, 5, 6),
                        new Stat(2, 0, 0, 0), new Stat(4, 2, 3, 3))
            }
    ),
    M2("M2", Color.BLUE,
            // Grid
            new int[][]{
                {5, 3, 3, 6, 6, 6, 6, 5},
                {6, 3, 1, 1, 6, 6, 2, 4},
                {6, 6, 1, 1, 6, 2, 2, 4},
                {6, 6, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 6, 6},
                {4, 2, 2, 6, 1, 1, 6, 6},
                {4, 2, 6, 6, 1, 1, 3, 6},
                {5, 6, 6, 6, 6, 3, 3, 5}
            },
            // Innate Stat
            new Stat(113, 49, 119, 182),
            // Board Stats
            new Stat[]{
                new Stat(103, 30, 49, 74),
                new Stat(124, 36, 59, 89),
                new Stat(145, 42, 68, 104),
                new Stat(176, 51, 83, 126),
                new Stat(206, 60, 97, 148)
            },
            // Resonances
            new int[]{4, 10, 16, 20, 24, 28, 32},
            new Stat[]{
                new Stat(13, 0, 6, 0),
                new Stat(0, 3, 0, 6),
                new Stat(30, 0, 0, 0),
                new Stat(0, 4, 8, 0),
                new Stat(48, 0, 0, 9),
                new Stat(0, 8, 0, 13),
                new Stat(68, 0, 8, 0)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(2, 0, 4, 0),
                new Stat(1, 3, 0, 5),
                new Stat(3, 0, 6, 0),
                new Stat(3, 4, 0, 5),
                new Stat(4, 0, 0, 6),
                new Stat(5, 0, 9, 0),
                new Stat(6, 2, 0, 8),
                new Stat(6, 0, 9, 0),
                new Stat(3, 5, 0, 11),
                new Stat(8, 0, 0, 8)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._4,
                        null, null,
                        new Stat(), new Stat(5, 2, 5, 5))
            }
    ),
    AT4("AT4", Color.BLUE,
            // Grid
            new int[][]{
                {6, 6, 6, 1, 1, 6, 6, 6},
                {6, 6, 1, 1, 1, 1, 6, 6},
                {6, 1, 1, 1, 1, 1, 1, 6},
                {2, 1, 1, 6, 6, 1, 1, 3},
                {2, 2, 2, 6, 6, 3, 3, 3},
                {6, 2, 2, 4, 4, 3, 3, 6},
                {6, 6, 5, 4, 4, 5, 6, 6},
                {6, 6, 6, 5, 5, 6, 6, 6}
            },
            // Innate Stat
            new Stat(113, 261, 284, 134),
            // Board Stats
            new Stat[]{
                new Stat(85, 131, 95, 45),
                new Stat(102, 157, 114, 54),
                new Stat(118, 183, 133, 63),
                new Stat(144, 222, 161, 76),
                new Stat(169, 261, 190, 90)
            },
            // Resonances
            new int[]{4, 10, 16, 22, 28, 32, 36},
            new Stat[]{
                new Stat(12, 0, 5, 0),
                new Stat(0, 5, 0, 5),
                new Stat(27, 0, 7, 0),
                new Stat(0, 10, 9, 0),
                new Stat(35, 0, 0, 10),
                new Stat(0, 12, 12, 0),
                new Stat(46, 18, 0, 0)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(3, 0, 5, 0),
                new Stat(0, 7, 8, 0),
                new Stat(4, 0, 0, 10),
                new Stat(0, 8, 9, 0),
                new Stat(5, 10, 0, 0),
                new Stat(6, 0, 10, 0),
                new Stat(0, 11, 12, 0),
                new Stat(7, 0, 0, 17),
                new Stat(0, 12, 15, 0),
                new Stat(9, 16, 0, 0)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5A,
                        new Stat(167, 261, 174, 65), new Stat(14, 8, 9, 5),
                        new Stat(1, 0, 0, 0), new Stat(4, 3, 2, 4)),
                new StatPreset(Shape.Type._6,
                        new Stat(166, 261, 174, 65), new Stat(14, 8, 9, 5),
                        new Stat(1, 0, 1, 0), new Stat(3, 3, 2, 4))
            }
    ),
    QLZ04("QLZ-04", Color.ORANGE,
            // Grid
            new int[][]{
                {6, 6, 6, 6, 6, 6, 6, 6},
                {5, 3, 6, 6, 6, 6, 3, 5},
                {5, 3, 3, 6, 6, 3, 3, 5},
                {4, 1, 1, 1, 1, 1, 1, 4},
                {4, 1, 1, 1, 1, 1, 1, 4},
                {6, 1, 1, 2, 2, 1, 1, 6},
                {6, 6, 2, 2, 2, 2, 6, 6},
                {6, 6, 6, 2, 2, 6, 6, 6}
            },
            // Innate Stat
            new Stat(77, 136, 188, 331),
            // Board Stats
            new Stat[]{
                new Stat(61, 72, 66, 117),
                new Stat(73, 86, 79, 140),
                new Stat(85, 100, 93, 163),
                new Stat(103, 122, 112, 198),
                new Stat(122, 143, 132, 233)
            },
            // Resonances
            new int[]{4, 10, 16, 24, 30, 34, 38},
            new Stat[]{
                new Stat(9, 0, 6, 0),
                new Stat(0, 6, 0, 6),
                new Stat(15, 0, 6, 0),
                new Stat(0, 9, 0, 9),
                new Stat(28, 0, 12, 0),
                new Stat(0, 15, 0, 10),
                new Stat(38, 0, 0, 14)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(3, 0, 3, 0),
                new Stat(2, 3, 0, 4),
                new Stat(4, 0, 6, 0),
                new Stat(3, 3, 0, 4),
                new Stat(5, 0, 0, 5),
                new Stat(5, 0, 10, 0),
                new Stat(6, 4, 0, 6),
                new Stat(6, 0, 10, 0),
                new Stat(4, 6, 0, 10),
                new Stat(8, 0, 0, 8)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5B,
                        null, null,
                        new Stat(), new Stat(3, 4, 3, 4))
            }
    ),
    MK153("Mk 153", Color.BLUE,
            // Grid
            new int[][]{
                {6, 6, 2, 2, 6, 6, 6, 6},
                {6, 6, 2, 2, 5, 5, 5, 6},
                {6, 6, 2, 2, 4, 4, 4, 6},
                {6, 6, 2, 2, 3, 3, 4, 6},
                {1, 1, 1, 1, 1, 1, 3, 3},
                {1, 1, 1, 1, 1, 1, 3, 3},
                {6, 5, 1, 1, 6, 6, 6, 6},
                {6, 6, 1, 1, 6, 6, 6, 6}
            },
            // Innate Stat
            new Stat(107, 224, 233, 107),
            // Board Stats
            new Stat[]{
                new Stat(98, 137, 95, 44),
                new Stat(117, 164, 114, 52),
                new Stat(137, 191, 133, 61),
                new Stat(166, 232, 162, 74),
                new Stat(195, 273, 190, 87)
            },
            // Resonances
            new int[]{4, 10, 16, 24, 30, 34, 38},
            new Stat[]{
                new Stat(24, 0, 6, 0),
                new Stat(0, 12, 0, 10),
                new Stat(24, 0, 6, 0),
                new Stat(0, 12, 12, 0),
                new Stat(32, 0, 0, 10),
                new Stat(0, 18, 12, 0),
                new Stat(32, 18, 0, 0)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(4, 0, 4, 0),
                new Stat(0, 8, 6, 0),
                new Stat(6, 0, 0, 10),
                new Stat(0, 8, 8, 0),
                new Stat(6, 6, 0, 0),
                new Stat(10, 0, 10, 0),
                new Stat(0, 10, 10, 0),
                new Stat(10, 0, 0, 10),
                new Stat(0, 12, 12, 0),
                new Stat(16, 16, 0, 0)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5B,
                        new Stat(195, 273, 140, 75), new Stat(17, 9, 7, 5),
                        new Stat(0, 0, 1, 0), new Stat(4, 5, 2, 3)),
                new StatPreset(Shape.Type._5B,
                        new Stat(189, 263, 176, 75), new Stat(16, 8, 9, 5),
                        new Stat(1, 0, 1, 0), new Stat(4, 3, 2, 3))
            }
    ),
    PP93("PP-93", Color.ORANGE,
            // Grid
            new int[][]{
                {6, 6, 6, 5, 5, 6, 6, 6},
                {6, 6, 6, 1, 1, 6, 6, 6},
                {6, 6, 3, 1, 1, 3, 6, 6},
                {4, 2, 1, 1, 1, 1, 1, 4},
                {4, 2, 1, 1, 1, 1, 1, 4},
                {6, 6, 3, 1, 1, 3, 6, 6},
                {6, 6, 6, 2, 2, 6, 6, 6},
                {6, 6, 6, 5, 5, 6, 6, 6}
            },
            // Innate Stat
            new Stat(138, 67, 182, 166),
            // Board Stats
            new Stat[]{
                new Stat(85, 28, 50, 46),
                new Stat(102, 33, 60, 55),
                new Stat(118, 38, 70, 64),
                new Stat(144, 47, 85, 77),
                new Stat(169, 55, 100, 91)
            },
            // Resonances
            new int[]{10, 15, 18, 26},
            new Stat[]{
                new Stat(10, 3, 8, 8),
                new Stat(0, 4, 15, 20),
                new Stat(10, 8, 15, 18),
                new Stat(0, 0, 30, 0)
            },
            // Iteration Stats
            new Stat[]{
                new Stat(2, 0, 4, 0),
                new Stat(1, 3, 0, 5),
                new Stat(3, 0, 6, 0),
                new Stat(3, 4, 0, 5),
                new Stat(4, 0, 0, 6),
                new Stat(5, 0, 9, 0),
                new Stat(6, 2, 0, 8),
                new Stat(6, 0, 9, 0),
                new Stat(3, 5, 0, 11),
                new Stat(8, 0, 0, 8)
            },
            // Presets
            new StatPreset[]{
                new StatPreset(Shape.Type._5A,
                        null, null,
                        new Stat(0, 0, 0, 0), new Stat(5, 5, 5, 5))
            }
    );

    private static final Map<String, Unit> NAMES = new HashMap<String, Unit>() {
        {
            for (Unit unit : Unit.values()) {
                put(unit.name, unit);
            }
        }
    };
    private static final Map<String, Unit> FILENAMES = new HashMap<String, Unit>() {
        {
            for (Unit unit : Unit.values()) {
                put(unit.getFileName(), unit);
            }
        }
    };

    public static Unit byName(String name) {
        Unit unit = NAMES.get(name);
        if (unit != null) {
            return unit;
        }
        return FILENAMES.get(name);
    }

    private final String name;
    private final Color color;
    private final int[][] grid;
    private final Stat innateStat;
    private final Stat[] boardStats;
    private final int[] resonanceCellReqs;
    private final Stat[] resonanceStats;
    private final Stat[] iterationStats;
    private final StatPreset[] presets;
    private final int[] rotationSteps;

    private Unit(String name, Color color, int[][] grid,
            Stat innateStat, Stat[] boardStats, int[] resonanceCellReqs, Stat[] resonanceStats, Stat[] iterationStats,
            StatPreset[] presets) {
        this.name = name;
        this.color = color;
        this.innateStat = innateStat;
        this.grid = grid;
        this.boardStats = boardStats;
        this.resonanceCellReqs = resonanceCellReqs;
        this.resonanceStats = resonanceStats;
        this.iterationStats = iterationStats;
        this.presets = presets;
        this.rotationSteps = getRotationSteps(this);
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return name.replace("-", "").replace(" ", "").toLowerCase();
    }

    public Integer[][] getGrid() {
        Integer[][] out = new Integer[grid.length][grid[0].length];
        for (int x = 0; x < grid.length; x++) {
            int[] row = grid[x];
            for (int y = 0; y < row.length; y++) {
                int i = row[y];
                out[x][y] = i;
            }
        }
        return out;
    }

    public Color getColor() {
        return color;
    }

    public int getRotationStep(int star) {
        if (star < 0 || rotationSteps.length <= star) {
            return 4;
        }
        return rotationSteps[star - 1];
    }

    public Stat getInnateStat() {
        return innateStat;
    }

    public Stat[] getBoardStats() {
        Stat[] out = new Stat[boardStats.length];
        for (int i = 0; i < boardStats.length; i++) {
            out[i] = boardStats[i];
        }
        return out;
    }

    public Map<Integer, Stat> GetResonanceStats() {
        Map<Integer, Stat> out = new HashMap<>();
        for (int i = 0; i < resonanceStats.length; i++) {
            int cell = resonanceCellReqs[i];
            Stat Stat = resonanceStats[i];
            out.put(cell, Stat);
        }
        return out;
    }

    public Stat[] getIterationStats() {
        Stat[] out = new Stat[iterationStats.length];
        for (int i = 0; i < iterationStats.length; i++) {
            out[i] = iterationStats[i];
        }
        return out;
    }

    public Stat getIterationStatSum(int iteration) {
        return new Stat(Arrays.asList(getIterationStats()).subList(0, iteration));
    }

    public boolean hasPreset() {
        return presets.length > 0;
    }

    public boolean hasDefaultPreset() {
        if (presets.length != 1) {
            return false;
        }
        return presets[0].stat == null;
    }

    public Stat getPresetStat(int index) {
        Stat out = presets[index].stat;
        return out != null ? out : Board.getMaxStat(this, 5);
    }

    public Stat getPresetPt(int index) {
        Stat out = presets[index].pt;
        return out != null ? out : Board.getMaxPt(this, 5);
    }

    public Stat getPresetFilterPtMin(int index) {
        return presets[index].ptFilterMin;
    }

    public Stat getPresetFilterPtMax(int index) {
        return presets[index].ptFilterMax;
    }

    public boolean[] getPresetTypeFilter(int index) {
        Shape.Type type = presets[index].typeMin;
        boolean[] out = new boolean[Filter.NUM_TYPE];
        for (int i = 0; i < Filter.NUM_TYPE - type.id + 1; i++) {
            out[i] = true;
        }
        return out;
    }

    public List<String> getPresetStrings(App app) {
        List<String> out = new ArrayList<>(presets.length);
        for (int i = 0; i < presets.length; i++) {
            Stat pt = presets[i].pt;
            String item;
            if (pt == null) {
                item = app.getText(AppText.CSET_PRESET_OPTION, String.valueOf(i + 1));
            } else {
                item = pt.toStringSlash();
            }
            Shape.Type type = presets[i].typeMin;

            item += " (" + type.toString() + (type == Shape.Type._6 ? "" : "-6") + ")";

            out.add(item);
        }

        return out;
    }

    @Override
    public String toString() {
        return name;
    }

    private static int[] getRotationSteps(Unit unit) {
        int[] steps = new int[5];
        for (int star = 1; star <= 5; star++) {
            PuzzleMatrix<Integer> unrotated = Board.initMatrix(unit, star);
            for (int i = 1; i <= 4; i++) {
                PuzzleMatrix<Integer> b = Board.initMatrix(unit, star);
                b.rotateContent(i, Board.UNUSED);
                if (unrotated.equals(b)) {
                    steps[star - 1] = i;
                    break;
                }
            }
        }
        return steps;
    }

    public enum Color {
        ORANGE(0),
        BLUE(1);

        private static final Map<Integer, Color> IDS = new HashMap<Integer, Color>() {
            {
                for (Color color : Color.values()) {
                    put(color.id, color);
                }
            }
        };

        private static final Map<String, Color> STRINGS = new HashMap<String, Color>() {
            {
                for (Color color : Color.values()) {
                    put(color.toString(), color);
                }
            }
        };

        public static Color byId(int id) {
            return IDS.get(id);
        }

        public static Color byString(String s) {
            return STRINGS.get(s);
        }

        public final int id;

        private Color(int id) {
            this.id = id;
        }
    }
}
