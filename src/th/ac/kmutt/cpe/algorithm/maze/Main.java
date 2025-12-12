package th.ac.kmutt.cpe.algorithm.maze;

import th.ac.kmutt.cpe.algorithm.maze.method.Run;
import th.ac.kmutt.cpe.algorithm.maze.structure.MazeData;
import th.ac.kmutt.cpe.algorithm.maze.ui.MazeFrame;

public class Main {
    private static final String FILE_NAME = "./MAZE/m15_15.txt";
    // BLOCK_SIZE unused after fixing frame to 1920x1080
    // private static final int BLOCK_SIZE = 10;
    MazeFrame frame;
    MazeData data;
    private Thread currentRunner;
    private volatile boolean cancelled = false;

    Run run;

    public void initFrame() {
        data = new MazeData(FILE_NAME);
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame = new MazeFrame("Maze Solver - " + getMazeLabel(), screen.width-50, screen.height-110);
        frame.setMazeFileName(FILE_NAME);

        // wire shared data/frame into runner and algorithms
        run = new Run(data, frame);

        frame.setControlListener(new MazeFrame.ControlListener() {
            @Override
            public void onRunRequested(String algorithmName) {
                frame.setControlsEnabled(false);
                cancelled = false;
                currentRunner = new Thread(() -> {
                    try {
                        run.runWithAlgorithm(algorithmName);
                    } finally {
                        javax.swing.SwingUtilities.invokeLater(() -> frame.setControlsEnabled(true));
                    }
                }, "maze-runner");
                currentRunner.start();
            }
            @Override
            public void onResetRequested() {
                cancelled = true;
                if (currentRunner != null && currentRunner.isAlive()) {
                    currentRunner.interrupt();
                }
                resetState();
            }

            @Override
            public void onImportRequested(String filePath) {
                try {
                    MazeData newData = new MazeData(filePath);
                    data = newData;
                    // re-wire runner to use new data
                    // BUGFIX: call setData on 'run', not on the anonymous ControlListener
                    run.setData(newData);
                    int bs = frame.getBlockSize();
                    frame.resizeToBlock(bs);
                    frame.setTitle("Maze Solver - " + getMazeLabel());
                    frame.setMazeFileName(filePath);
                    resetState();
                    frame.render(data);
                } catch (RuntimeException ex) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                        javax.swing.JOptionPane.showMessageDialog(frame, ex.getMessage(), "Load Error", javax.swing.JOptionPane.ERROR_MESSAGE)
                    );
                }
            }
        });
        frame.render(data);
    }

    private void resetState() {
        for (int i = 0; i < data.N(); i++) {
            for (int j = 0; j < data.M(); j++) {
                data.visited[i][j] = false;
                data.path[i][j] = false;
                data.result[i][j] = false;
            }
        }
        frame.setTitle("Maze Solver - " + getMazeLabel());
        frame.render(data);
    }

    private String getMazeLabel() {
        try {
            if (data != null) {
                String s = data.toString();
                if (s != null && !s.trim().isEmpty()) return s;
            }
        } catch (Throwable ignored) {}
        if (data != null) {
            return data.N() + "x" + data.M();
        }
        return "(no maze)";
    }

    public static void main(String[] args) {
        new Main().initFrame();
    }
}
