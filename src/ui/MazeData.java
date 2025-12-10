package ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MazeData {
    private int N, M;
    private int[][] maze;
    private char[][] mazeChars;

    public int getN(){
        return N;
    }
    public int getM(){
        return M;
    }
    public char[][] getMaze(){
        return mazeChars;
    }
    public int[][] getMazeInt(){
        return maze;
    }

    public MazeData(String fileName) {
        Scanner sc = null;
        try {
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                throw new IllegalArgumentException("Invalid file path: " + fileName);
            }

            FileInputStream fis = new FileInputStream(file);
            sc = new Scanner(new BufferedInputStream(fis), "UTF-8");

            if (!sc.hasNextLine()) {
                throw new IllegalArgumentException("File is empty: " + fileName);
            }

            // Read all lines
            java.util.List<String> lines = new java.util.ArrayList<>();
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }

            if (lines.isEmpty()) {
                throw new IllegalArgumentException("File is empty: " + fileName);
            }

            N = lines.size();
            
            // Parse the first line to determine M and detect format
            String firstLine = lines.get(0);
            Pattern pattern = Pattern.compile("\"([^\"]+)\"|([#SG])");
            Matcher matcher = pattern.matcher(firstLine);
            
            java.util.List<String> tokens = new java.util.ArrayList<>();
            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    tokens.add(matcher.group(1));
                } else {
                    tokens.add(matcher.group(2));
                }
            }
            
            M = tokens.size();
            maze = new int[N][M];
            mazeChars = new char[N][M];

            // Parse all lines
            for (int i = 0; i < N; i++) {
                String line = lines.get(i);
                matcher = pattern.matcher(line);
                tokens.clear();
                
                while (matcher.find()) {
                    if (matcher.group(1) != null) {
                        tokens.add(matcher.group(1));
                    } else {
                        tokens.add(matcher.group(2));
                    }
                }
                
                if (tokens.size() != M) {
                    throw new IllegalArgumentException("Invalid maze row length at line " + (i + 1) + 
                        ": expected " + M + " cells, got " + tokens.size());
                }
                
                for (int j = 0; j < M; j++) {
                    String token = tokens.get(j);
                    mazeChars[i][j] = token.charAt(0);
                    
                    try {
                        maze[i][j] = Integer.parseInt(token);
                    } catch (NumberFormatException e) {
                        // Handle special characters like S (start), G (goal), # (wall)
                        maze[i][j] = -1;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading maze data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }
}

