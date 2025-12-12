package th.ac.kmutt.cpe.algorithm.maze.method;
import th.ac.kmutt.cpe.algorithm.maze.structure.MazeData;
import th.ac.kmutt.cpe.algorithm.maze.ui.MazeFrame;

public class Run {
    MazeData data;
    MazeFrame frame;
    BFS bfs = new BFS();
    AStar aStar = new AStar();
    Dijkstra dijkstra = new Dijkstra();
    GeneticAlgorithm ga = new GeneticAlgorithm();
    Position pos;

    public Run(MazeData data, MazeFrame frame) {
        this.data = data;
        this.frame = frame;
        this.pos = new Position(0, 0, null);
        // inject shared instances
        bfs.data = data; bfs.frame = frame; bfs.pos = pos;
        aStar.data = data; aStar.frame = frame; aStar.pos = pos;
        dijkstra.data = data; dijkstra.frame = frame; dijkstra.pos = pos;
        ga.data = data; ga.frame = frame; ga.pos = pos;
        pos.data = data; pos.frame = frame;
    }

    public void setData(MazeData newData) {
        this.data = newData;
        bfs.data = newData;
        aStar.data = newData;
        dijkstra.data = newData;
        ga.data = newData;
        pos.data = newData;
    }

    public void runWithAlgorithm(String algo) {
        for (int i = 0; i < data.N(); i++) {
            for (int j = 0; j < data.M(); j++) {
                data.visited[i][j] = false;
                data.path[i][j] = false;
                data.result[i][j] = false;
            }
        }

        switch (algo) {
            case "BFS":
                bfs.runBFS();
                return;
            case "A*":
                aStar.runAStar();
                return;
            case "Genetic":
                ga.runGenetic();
                return;
            case "Dijkstra":
            default:
                dijkstra.runDijkstra();
        }
    }
}
