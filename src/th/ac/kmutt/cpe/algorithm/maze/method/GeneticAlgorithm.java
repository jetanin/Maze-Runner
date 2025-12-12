package th.ac.kmutt.cpe.algorithm.maze.method;

import th.ac.kmutt.cpe.algorithm.maze.structure.MazeData;
import th.ac.kmutt.cpe.algorithm.maze.ui.MazeFrame;

public class GeneticAlgorithm {
    private static final int directions[][] = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };
    MazeData data;
    MazeFrame frame;
    Position pos;
    private volatile boolean cancelled = false;

    public  void runGenetic() {
        // Genetic algorithm with goal-directed bias, repair, and distance-map fitness.
        // Key fix: keep genomes aligned with their evaluation when sorting/selecting.
        final int populationSize = Math.max(10, frame.getGaPopulation());
        final int maxGenerations = Math.max(1, frame.getGaGenerations());
        int estSteps = estimateShortestSteps();
        int area = data.N() * data.M();
        double scale = area >= 2500 ? 3.0 : 1.5; // bigger mazes get longer genomes
        int upperCap = Math.max(300, area / 2);  // allow larger cap for big mazes
        final int genomeLength = Math.max(
            Math.min((int)Math.round(estSteps * scale), upperCap),
            data.N() + data.M()
        );
        final double mutationRate = frame.getGaMutationRate();
        final double goalBias = frame.getGaGoalBias();
        final java.util.Random rnd = new java.util.Random(42);

        final int[][] distMap = computeDistanceMap();

        // Helper to evaluate a genome
        class EvalResult { int cost; java.util.List<int[]> path; boolean reached; }
        java.util.function.Function<int[], EvalResult> evaluate = genome -> {
            // reset temp visited
            boolean[][] seen = new boolean[data.N()][data.M()];
            int x = data.getEntranceX(), y = data.getEntranceY();
            int cost = 0;
            java.util.ArrayList<int[]> path = new java.util.ArrayList<>();
            path.add(new int[]{x,y});
            seen[x][y] = true;
            for (int i=0;i<genome.length;i++) {
                int move = genome[i]%4;
                // Occasionally override with a goal-directed move
                if (rnd.nextDouble() < goalBias) {
                    move = chooseDirectedMove(x, y, distMap, rnd);
                }
                int[] d = directions[move];
                int nx = x + d[0], ny = y + d[1];
                if (!data.inArea(nx, ny) || data.getMazeChar(nx,ny)!=MazeData.ROAD) {
                    cost += 50; // heavier penalty for invalid move
                    continue;
                }
                x = nx; y = ny;
                int w = data.weight!=null?data.weight[x][y]:1; int stepCost = w>0?w:1; cost += stepCost;
                if (!seen[x][y]) {
                    seen[x][y]=true;
                } else {
                    cost += 2; // small loop penalty
                }
                path.add(new int[]{x,y});
                if (x==data.getExitX() && y==data.getExitY()) break;
            }
            boolean reached = (x==data.getExitX() && y==data.getExitY());
            if (!reached) {
                // Penalize non-finished routes proportional to remaining grid distance
                int d = safeGoalDistance(distMap, x, y);
                cost += d * 120; // strong steering toward goal
            }
            // Shorter solutions slightly preferred among ties
            cost += path.size();
            EvalResult r = new EvalResult();
            r.cost=cost; r.path=path; r.reached=reached;
            return r;
        };

        class Candidate {
            final int[] genome;
            final EvalResult eval;
            Candidate(int[] genome, EvalResult eval) { this.genome = genome; this.eval = eval; }
        }

        // Initialize population (mix random and goal-directed seeded genomes)
        java.util.List<int[]> pop = new java.util.ArrayList<>(populationSize);
        int seeded = Math.max(2, populationSize / 10);
        for (int i=0;i<seeded;i++) {
            pop.add(generateDirectedGenome(genomeLength, distMap, rnd));
        }
        for (int i=seeded;i<populationSize;i++){
            int[] g=new int[genomeLength];
            for(int j=0;j<genomeLength;j++) g[j]=rnd.nextInt(4);
            pop.add(g);
        }

        int bestCost = Integer.MAX_VALUE;
        java.util.List<int[]> bestPath=null;
        boolean bestReached=false;
        String algoName="Genetic";
        long t0 = System.nanoTime();
        int gen = 0;
        int stagnation = 0;
        // Evolve up to maxGenerations; also stops early when a goal-reaching path is found.
        while (!cancelled && !bestReached && gen < maxGenerations) {
            // Evaluate with alignment
            java.util.List<Candidate> candidates = new java.util.ArrayList<>(populationSize);
            for (int[] g : pop) candidates.add(new Candidate(g, evaluate.apply(g)));
            // Sort by reached then cost
            candidates.sort((a, b) -> {
                int c1 = (a.eval.reached ? 0 : 1);
                int c2 = (b.eval.reached ? 0 : 1);
                if (c1 != c2) return Integer.compare(c1, c2);
                return Integer.compare(a.eval.cost, b.eval.cost);
            });
            // Elitism
            java.util.List<int[]> next = new java.util.ArrayList<>(populationSize);
            int eliteCount = Math.max(1, Math.min(frame.getGaElitismCount(), populationSize-1));
            for (int i=0;i<eliteCount;i++) {
                int[] elite = candidates.get(i).genome.clone();
                // Small greedy repair to help elites approach the goal if not reached
                if (!candidates.get(i).eval.reached) {
                    greedyRepair(elite, genomeLength, distMap, rnd);
                }
                next.add(elite);
            }
            // Track best
            EvalResult br = candidates.get(0).eval;
            boolean improved = false;
            if (br.reached) {
                if (!bestReached || br.cost < bestCost) {
                    bestReached = true;
                    bestCost = br.cost;
                    bestPath = br.path;
                    improved = true;
                }
            } else if (!bestReached && br.cost < bestCost) {
                bestCost = br.cost;
                bestPath = br.path;
                improved = true;
            }

            if (improved) stagnation = 0; else stagnation++;

            // Animate occasionally to keep UI responsive
            if (!cancelled && br.path != null && gen % 5 == 0) {
                clearTransientMarks();
                renderTravellingPath(br.path);
            }
            if (bestReached) break; // stop once a path reaches the goal
            if (stagnation > 80) break; // stop if stuck too long
            // Crossover + mutation to refill
            while (next.size() < populationSize) {
                int parentPool = Math.max(eliteCount, Math.min(populationSize, 20));
                int[] p1 = candidates.get(rnd.nextInt(parentPool)).genome;
                int[] p2 = candidates.get(rnd.nextInt(parentPool)).genome;
                int[] child = new int[genomeLength];
                int cut = 1 + rnd.nextInt(genomeLength-1);
                System.arraycopy(p1, 0, child, 0, cut);
                System.arraycopy(p2, cut, child, cut, genomeLength-cut);
                // mutation
                for (int j=0;j<genomeLength;j++) {
                    if (rnd.nextDouble() < mutationRate) child[j] = rnd.nextInt(4);
                }
                // Occasionally bias a segment toward goal
                if (rnd.nextDouble() < 0.15) {
                    directedSegmentMutation(child, distMap, rnd);
                }
                // If child still looks poor (heuristic), apply greedy repair
                if (rnd.nextDouble() < 0.2) {
                    greedyRepair(child, genomeLength, distMap, rnd);
                }
                next.add(child);
            }
            pop = next;
            // Occasionally update UI with cost-only to avoid clutter
            if (gen % 10 == 0) {
                Integer uiCost = (br.path != null ? computeRouteCost(br.path) : null);
                frame.updateMetrics(uiCost, null, null, (System.nanoTime()-t0)/1_000_000L, algoName);
            }
            gen++;
        }
        long t1 = System.nanoTime();
        // Render best path (paint finished route only once)
        resetState();
        if (bestPath != null) {
            for (int[] cell : bestPath) {
                if (cancelled) break;
                int bx = cell[0], by = cell[1];
                if (data.inArea(bx, by)) data.result[bx][by] = true;
            }
            frame.render(data);
        }
        // Final report: show only the best route metrics
        Integer finalCost = (bestPath != null ? computeRouteCost(bestPath) : null);
        int finalSteps = (bestPath != null ? bestPath.size() : 0);
        int finalVisited = (bestPath != null ? countUnique(bestPath) : 0);
        frame.updateMetrics(finalCost, finalSteps, finalVisited, (t1-t0)/1_000_000L, algoName);
        if (pos != null) pos.setData(-1, -1, false);
    }

    // Helper: clear transient exploration marks used for travelling animation
    private void clearTransientMarks() {
        for (int i = 0; i < data.N(); i++) {
            for (int j = 0; j < data.M(); j++) {
                data.path[i][j] = false;
            }
        }
        frame.render(data);
    }

    // Animate the current best GA candidate path as travelling steps
    private void renderTravellingPath(java.util.List<int[]> path) {
        for (int[] cell : path) {
            if (cancelled) break;
            int x = cell[0], y = cell[1];
            pos.setData(x, y, true); // uses pause based on UI speed
        }
        // After travelling, keep the last travelled cells marked as path
        frame.render(data);
    }

    // Choose a move that reduces distance-to-goal (using distMap) and avoids walls.
    private int chooseDirectedMove(int x, int y, int[][] distMap, java.util.Random rnd) {
        int bestMove = -1;
        int bestDist = safeGoalDistance(distMap, x, y);
        for (int m = 0; m < 4; m++) {
            int nx = x + directions[m][0];
            int ny = y + directions[m][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx, ny) != MazeData.ROAD) continue;
            int d = safeGoalDistance(distMap, nx, ny);
            if (d < bestDist) {
                bestDist = d;
                bestMove = m;
            }
        }
        if (bestMove != -1) return bestMove;
        // fallback: any valid move
        java.util.ArrayList<Integer> candidates = new java.util.ArrayList<>();
        for (int m = 0; m < 4; m++) {
            int nx = x + directions[m][0];
            int ny = y + directions[m][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx, ny) != MazeData.ROAD) continue;
            candidates.add(m);
        }
        if (!candidates.isEmpty()) return candidates.get(rnd.nextInt(candidates.size()));
        return rnd.nextInt(4);
    }

    // Replace a random segment with goal-directed steps
    private void directedSegmentMutation(int[] g, int[][] distMap, java.util.Random rnd) {
        int segLen = Math.max(5, Math.min(20, g.length / 6));
        int startIdx = rnd.nextInt(Math.max(1, g.length - segLen));
        // Replace segment with goal-directed steps based on current simulated position
        int x = data.getEntranceX(), y = data.getEntranceY();
        for (int i = 0; i < startIdx; i++) {
            int mv = g[i] % 4;
            int nx = x + directions[mv][0];
            int ny = y + directions[mv][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx,ny)!=MazeData.ROAD) continue;
            x = nx; y = ny;
        }
        for (int i = startIdx; i < Math.min(g.length, startIdx + segLen); i++) {
            int mv = chooseDirectedMove(x, y, distMap, rnd);
            g[i] = mv;
            int nx = x + directions[mv][0];
            int ny = y + directions[mv][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx,ny)!=MazeData.ROAD) break;
            x = nx; y = ny;
        }
    }

    // Append a small greedy tail to help elites approach goal
    private void greedyRepair(int[] g, int genomeLength, int[][] distMap, java.util.Random rnd) {
        int x = data.getEntranceX(), y = data.getEntranceY();
        for (int i = 0; i < genomeLength; i++) {
            int mv = g[i] % 4;
            int nx = x + directions[mv][0];
            int ny = y + directions[mv][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx,ny)!=MazeData.ROAD) continue;
            x = nx; y = ny;
            if (x==data.getExitX() && y==data.getExitY()) return;
        }
        // Replace last few steps with goal-directed steps
        int tail = Math.min(20, genomeLength/4);
        for (int i = genomeLength - tail; i < genomeLength; i++) {
            int mv = chooseDirectedMove(x, y, distMap, rnd);
            g[i] = mv;
            int nx = x + directions[mv][0];
            int ny = y + directions[mv][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx,ny)!=MazeData.ROAD) break;
            x = nx; y = ny;
            if (x==data.getExitX() && y==data.getExitY()) break;
        }
    }

    // Estimate shortest steps from entrance to exit using unweighted BFS (ignores weights)
    private int estimateShortestSteps() {
        int n = data.N(), m = data.M();
        boolean[][] seen = new boolean[n][m];
        java.util.ArrayDeque<Position> q = new java.util.ArrayDeque<>();
        Position s = new Position(data.getEntranceX(), data.getEntranceY(), null);
        q.add(s);
        seen[s.x][s.y] = true;
        while (!q.isEmpty()) {
            Position cur = q.poll();
            if (cur.x == data.getExitX() && cur.y == data.getExitY()) {
                // count steps via backtracking
                int steps = 0;
                Position p = cur;
                while (p != null) { steps++; p = p.prev; }
                return steps;
            }
            for (int[] d : directions) {
                int nx = cur.x + d[0], ny = cur.y + d[1];
                if (data.inArea(nx, ny) && !seen[nx][ny] && data.getMazeChar(nx,ny)==MazeData.ROAD) {
                    seen[nx][ny] = true;
                    q.add(new Position(nx, ny, cur));
                }
            }
        }
        // fallback to Manhattan distance + padding if unreachable by BFS
        int md = Math.abs(data.getEntranceX()-data.getExitX()) + Math.abs(data.getEntranceY()-data.getExitY());
        return md + 20;
    }

    // Generate a genome that tends to move toward the goal while avoiding walls
    private int[] generateDirectedGenome(int length, int[][] distMap, java.util.Random r) {
        int[] g = new int[length];
        int x = data.getEntranceX(), y = data.getEntranceY();
        for (int i = 0; i < length; i++) {
            int mv;
            // Mostly choose directed moves; occasional random to escape dead-ends
            if (r.nextDouble() < 0.85) {
                mv = chooseDirectedMove(x, y, distMap, r);
            } else {
                mv = r.nextInt(4);
            }
            g[i] = mv;
            int nx = x + directions[mv][0];
            int ny = y + directions[mv][1];
            if (!data.inArea(nx, ny) || data.getMazeChar(nx,ny)!=MazeData.ROAD) continue;
            x = nx; y = ny;
            if (x==data.getExitX() && y==data.getExitY()) break;
        }
        return g;
    }

    private int safeGoalDistance(int[][] distMap, int x, int y) {
        if (distMap == null || !data.inArea(x, y)) {
            return Math.abs(x - data.getExitX()) + Math.abs(y - data.getExitY());
        }
        int d = distMap[x][y];
        if (d == Integer.MAX_VALUE) {
            return (Math.abs(x - data.getExitX()) + Math.abs(y - data.getExitY())) * 5;
        }
        return d;
    }

    // BFS from goal to compute shortest steps ignoring weights; used for fitness/direction.
    private int[][] computeDistanceMap() {
        int n = data.N(), m = data.M();
        int[][] dist = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) dist[i][j] = Integer.MAX_VALUE;
        }
        int gx = data.getExitX(), gy = data.getExitY();
        if (!data.inArea(gx, gy) || data.getMazeChar(gx, gy) != MazeData.ROAD) return dist;

        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        dist[gx][gy] = 0;
        q.add(new int[]{gx, gy});
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int x = cur[0], y = cur[1];
            int cd = dist[x][y];
            for (int[] d : directions) {
                int nx = x + d[0], ny = y + d[1];
                if (!data.inArea(nx, ny)) continue;
                if (data.getMazeChar(nx, ny) != MazeData.ROAD) continue;
                if (dist[nx][ny] != Integer.MAX_VALUE) continue;
                dist[nx][ny] = cd + 1;
                q.add(new int[]{nx, ny});
            }
        }
        return dist;
    }

    private int computeRouteCost(java.util.List<int[]> path) {
        if (path == null || path.size() < 2) return 0;
        int cost = 0;
        for (int i = 1; i < path.size(); i++) {
            int x = path.get(i)[0];
            int y = path.get(i)[1];
            if (!data.inArea(x, y)) continue;
            int w = (data.weight != null ? data.weight[x][y] : 1);
            cost += (w > 0 ? w : 1);
        }
        return cost;
    }

    private int countUnique(java.util.List<int[]> path) {
        if (path == null) return 0;
        boolean[][] seen = new boolean[data.N()][data.M()];
        int c = 0;
        for (int[] cell : path) {
            int x = cell[0], y = cell[1];
            if (!data.inArea(x, y)) continue;
            if (!seen[x][y]) {
                seen[x][y] = true;
                c++;
            }
        }
        return c;
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

    // Helper: returns a friendly label for the currently loaded maze (file name or size)
    private String getMazeLabel() {
        // Prefer original source file name via toString if MazeData exposes it
        try {
            if (data != null) {
                String s = data.toString();
                if (s != null && !s.trim().isEmpty()) return s;
            }
        } catch (Throwable ignored) {}
        // Fallback to dimensions
        if (data != null) {
            return data.N() + "x" + data.M();
        }
        return "(no maze)";
    }
}
