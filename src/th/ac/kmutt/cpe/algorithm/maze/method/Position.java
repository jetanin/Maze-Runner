package th.ac.kmutt.cpe.algorithm.maze.method;

import th.ac.kmutt.cpe.algorithm.maze.structure.MazeData;
import th.ac.kmutt.cpe.algorithm.maze.ui.MazeFrame;
import th.ac.kmutt.cpe.algorithm.maze.ui.MazeUtil;

public class Position {
    int x, y; Position prev;
    MazeData data;
    MazeFrame frame;
    private volatile boolean cancelled = false;

    Position(int x, int y, Position prev){
        this.x=x; this.y=y; this.prev=prev;
    }
    
    public void setData(int x, int y, boolean isPath) {
        if (cancelled) return;
        if (data != null && data.inArea(x, y)) {
            data.path[x][y] = isPath;
        }
        if (frame != null) frame.render(data);
        try {
            if (frame != null) MazeUtil.pause(frame.getDelayMs());
        } catch (Exception ignored) {}
    }

    public int findPath(Node p) {
        int steps = 0;
        Node cur = p;
        while (cur != null) {
            if (data != null) data.result[cur.x][cur.y] = true;
            cur = cur.prev;
            steps++;
        }
        return steps;
    }

    public int findPath(Position p) {
        int steps = 0;
        Position cur = p;
        while (cur != null) {
            if (data != null) data.result[cur.x][cur.y] = true;
            cur = cur.prev;
            steps++;
        }
        return steps;
    }
}

