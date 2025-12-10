package ui;

public class Main {
    private static final String MAZE15X15 = "MAZE/m15_15.txt";
    private static final String MAZE24X20 = "MAZE/m24_20.txt";
    private static final String MAZE30X30 = "MAZE/m30_30.txt";
    private static final String MAZE33X35 = "MAZE/m33_35.txt";
    private static final String MAZE40X40 = "MAZE/m40_40.txt";
    private static final String MAZE40X45 = "MAZE/m45_45.txt";
    private static final String MAZE45X45 = "MAZE/m45_45.txt";
    private static final String MAZE50X50 = "MAZE/m50_50.txt";
    private static final String MAZE60X60 = "MAZE/m60_60.txt";
    private static final String MAZE70X60 = "MAZE/m70_60.txt";
    private static final String MAZE80X50 = "MAZE/m80_50.txt";
    private static final String MAZE100X90 = "MAZE/m100_90.txt";
    private static final String MAZE100X100 = "MAZE/m100_100.txt";

    private static final int BLOCK_SIZE = 6;
    MazeFrame frame;

    private void initFrame(String mazeFile) {
        try {
            MazeData data = new MazeData(mazeFile);
            frame = new MazeFrame("Maze Solver", data.getN() * BLOCK_SIZE, data.getM() * BLOCK_SIZE, data, BLOCK_SIZE);
        } catch (Exception e) {
            System.err.println("Failed to initialize frame: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String mazeFile = (args.length > 0) ? args[0] : MAZE15X15;
        new Main().initFrame(mazeFile);
    }
}
