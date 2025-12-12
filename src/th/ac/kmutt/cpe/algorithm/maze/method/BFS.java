package th.ac.kmutt.cpe.algorithm.maze.method;

import th.ac.kmutt.cpe.algorithm.maze.structure.MazeData;
import th.ac.kmutt.cpe.algorithm.maze.ui.MazeFrame;

public class BFS {
    private static final int directions[][] = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };
    MazeData data;
    MazeFrame frame;
    Position pos;

    private volatile boolean cancelled = false;

    public void runBFS() {
        java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();
        Position entrance = new Position(data.getEntranceX(), data.getEntranceY(), null);
        queue.add(entrance);
        if (data.inArea(entrance.x, entrance.y)) data.visited[entrance.x][entrance.y] = true;

        boolean isSolved = false;
        int visitedCount = 0;
        long t0 = System.nanoTime();
        Position end = null;

        while (!queue.isEmpty() && !cancelled) {
            Position cur = queue.poll();
            visitedCount++;
            pos.setData(cur.x, cur.y, true);
            if (cur.x == data.getExitX() && cur.y == data.getExitY()) { isSolved = true; end = cur; break; }
            for (int[] d : directions) {
                int nx = cur.x + d[0], ny = cur.y + d[1];
                if (data.inArea(nx, ny) && !data.visited[nx][ny] && data.getMazeChar(nx,ny)==MazeData.ROAD) {
                    data.visited[nx][ny] = true;
                    queue.add(new Position(nx, ny, cur));
                }
            }
        }

        long t1 = System.nanoTime();
        if (isSolved && end != null) {
            int steps = pos.findPath(end);
            long ms = (t1 - t0) / 1_000_000L;
            frame.updateMetrics(null, steps, visitedCount, ms, "BFS");
        } else {
            frame.updateMetrics(null, null, visitedCount, (System.nanoTime()-t0)/1_000_000L, "BFS");
        }
        pos.setData(-1, -1, false);
    }
}
