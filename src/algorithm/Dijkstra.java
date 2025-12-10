package algorithm;
import java.util.*;
import ui.MazeData;

public class Dijkstra implements PathFinder {

    // Represents a position in the maze
    static class Position {
        int row, col;
        
        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Position position = (Position) obj;
            return row == position.row && col == position.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
        
        @Override
        public String toString() {
            return "(" + row + "," + col + ")";
        }
    }
    
    // Node for priority queue
    static class Node {
        Position pos;
        int distance;
        
        Node(Position pos, int distance) {
            this.pos = pos;
            this.distance = distance;
        }
    }

    public static Map<Position, Integer> dijkstra(MazeData mazeData) {
        char[][] maze = mazeData.getMaze();
        int[][] weights = mazeData.getMazeInt();
        int N = mazeData.getN();
        int M = mazeData.getM();
        
        // Find start position
        Position start = null;
        
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (maze[i][j] == 'S') {
                    start = new Position(i, j);
                }
            }
        }
        
        if (start == null) {
            throw new IllegalArgumentException("No start position 'S' found in maze");
        }
        
        // Initialize distances map
        Map<Position, Integer> distances = new HashMap<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (maze[i][j] != '#') { // Not a wall
                    distances.put(new Position(i, j), Integer.MAX_VALUE);
                }
            }
        }
        distances.put(start, 0);

        // Priority queue for Dijkstra's algorithm
        PriorityQueue<Node> pq = new PriorityQueue<>(
            Comparator.comparingInt(node -> node.distance)
        );
        
        pq.offer(new Node(start, 0));
        
        // Directions: up, right, down, left
        int[] dr = {-1, 0, 1, 0};
        int[] dc = {0, 1, 0, -1};

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            Position currentPos = current.pos;
            int currentDistance = current.distance;

            // Skip if we've already found a better path
            if (currentDistance > distances.get(currentPos)) {
                continue;
            }

            // Explore all 4 neighbors
            for (int i = 0; i < 4; i++) {
                int newRow = currentPos.row + dr[i];
                int newCol = currentPos.col + dc[i];
                
                // Check bounds
                if (newRow < 0 || newRow >= N || newCol < 0 || newCol >= M) {
                    continue;
                }
                
                // Check if it's a wall
                if (maze[newRow][newCol] == '#') {
                    continue;
                }
                
                Position neighbor = new Position(newRow, newCol);
                
                // Calculate weight for moving to this cell
                int weight;
                if (maze[newRow][newCol] == 'S' || maze[newRow][newCol] == 'G') {
                    weight = 1; // Start and goal have weight 1
                } else {
                    weight = weights[newRow][newCol];
                    if (weight == -1) weight = 1; // Fallback for special chars
                }
                
                int newDistance = currentDistance + weight;

                // If we found a shorter path, update it
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    pq.offer(new Node(neighbor, newDistance));
                }
            }
        }

        return distances;
    }
    
    public static List<Position> getShortestPath(MazeData mazeData) {
        char[][] maze = mazeData.getMaze();
        int[][] weights = mazeData.getMazeInt();
        int N = mazeData.getN();
        int M = mazeData.getM();
        
        // Find start and goal positions
        Position start = null;
        Position goal = null;
        
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (maze[i][j] == 'S') {
                    start = new Position(i, j);
                } else if (maze[i][j] == 'G') {
                    goal = new Position(i, j);
                }
            }
        }
        
        if (start == null || goal == null) {
            return new ArrayList<>();
        }
        
        // Run Dijkstra and also track the path
        Map<Position, Integer> distances = new HashMap<>();
        Map<Position, Position> previous = new HashMap<>();
        
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (maze[i][j] != '#') {
                    distances.put(new Position(i, j), Integer.MAX_VALUE);
                }
            }
        }
        distances.put(start, 0);

        PriorityQueue<Node> pq = new PriorityQueue<>(
            Comparator.comparingInt(node -> node.distance)
        );
        
        pq.offer(new Node(start, 0));
        
        int[] dr = {-1, 0, 1, 0};
        int[] dc = {0, 1, 0, -1};

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            Position currentPos = current.pos;
            int currentDistance = current.distance;

            if (currentDistance > distances.get(currentPos)) {
                continue;
            }

            // If we reached the goal, we can stop
            if (currentPos.equals(goal)) {
                break;
            }

            for (int i = 0; i < 4; i++) {
                int newRow = currentPos.row + dr[i];
                int newCol = currentPos.col + dc[i];
                
                if (newRow < 0 || newRow >= N || newCol < 0 || newCol >= M) {
                    continue;
                }
                
                if (maze[newRow][newCol] == '#') {
                    continue;
                }
                
                Position neighbor = new Position(newRow, newCol);
                
                int weight;
                if (maze[newRow][newCol] == 'S' || maze[newRow][newCol] == 'G') {
                    weight = 1;
                } else {
                    weight = weights[newRow][newCol];
                    if (weight == -1) weight = 1;
                }
                
                int newDistance = currentDistance + weight;

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentPos);
                    pq.offer(new Node(neighbor, newDistance));
                }
            }
        }
        
        // Reconstruct path
        List<Position> path = new ArrayList<>();
        Position current = goal;
        
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        
        // If path doesn't start with start position, no path was found
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return new ArrayList<>();
        }
        
        return path;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            // Test single maze from command line argument
            String mazeFile = args[0];
            try {
                MazeData mazeData = new MazeData(mazeFile);
                System.out.println("Testing: " + mazeFile);
                System.out.println("Maze size: " + mazeData.getN() + "x" + mazeData.getM());
                
                List<Position> path = getShortestPath(mazeData);
                if (!path.isEmpty()) {
                    Map<Position, Integer> distances = dijkstra(mazeData);
                    Position goal = path.get(path.size()-1);
                    System.out.println("✓ Path found! Steps: " + path.size() + ", Cost: " + distances.get(goal));
                } else {
                    System.out.println("✗ No path found!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            // Default: test first few mazes
            String[] mazeFiles = {"MAZE/m15_15.txt", "MAZE/m24_20.txt", "MAZE/m30_30.txt"};
            
            for (String mazeFile : mazeFiles) {
                try {
                    MazeData mazeData = new MazeData(mazeFile);
                    System.out.println("\nTesting: " + mazeFile + " (" + mazeData.getN() + "x" + mazeData.getM() + ")");
                    
                    List<Position> path = getShortestPath(mazeData);
                    if (!path.isEmpty()) {
                        Map<Position, Integer> distances = dijkstra(mazeData);
                        Position goal = path.get(path.size()-1);
                        System.out.println("✓ Path found! Steps: " + path.size() + ", Cost: " + distances.get(goal));
                    } else {
                        System.out.println("✗ No path found!");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
