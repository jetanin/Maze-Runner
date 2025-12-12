package th.ac.kmutt.cpe.algorithm.maze.method;

public class Node {
    public int x, y;
    public int cost;
    public Node prev;

    public Node(int x, int y, int cost, Node prev) {
        this.x = x;
        this.y = y;
        this.cost = cost;
        this.prev = prev;
    }
    
}
