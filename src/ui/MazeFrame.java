package ui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class MazeFrame extends JFrame {
    private int canvasWidth;
    private int canvasHeight;
    private MazeData mazeData;
    private int blockSize;

    public MazeFrame(String title, int canvasWidth, int canvasHeight, MazeData mazeData, int blockSize){
        super(title);
        this.canvasHeight = canvasHeight;
        this.canvasWidth = canvasWidth;
        this.mazeData = mazeData;
        this.blockSize = blockSize;

        MazeCanvas canvas = new MazeCanvas();
        this.setContentPane(canvas);

        this.pack();
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private class MazeCanvas extends JPanel{
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            
            if (mazeData == null) return;
            
            char[][] maze = mazeData.getMaze();
            int[][] mazeInt = mazeData.getMazeInt();
            int N = mazeData.getN();
            int M = mazeData.getM();
            
            // Draw maze
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    char cell = maze[i][j];
                    int x = j * blockSize;
                    int y = i * blockSize;
                    
                    // Set color based on cell type
                    Color color = Color.WHITE;
                    if (cell == '#') {
                        color = Color.BLACK;  // Wall
                    } else if (cell == 'S') {
                        color = Color.GREEN;  // Start
                    } else if (cell == 'G') {
                        color = Color.RED;    // Goal
                    }
                    
                    g.setColor(color);
                    g.fillRect(x, y, blockSize, blockSize);
                    
                    // Draw border
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, blockSize, blockSize);

                    // Draw weight text for numeric cells (non-wall)
                    if (cell != '#') {
                        int weight = mazeInt[i][j];
                        if (weight >= 0) {
                            // Choose a readable font size relative to blockSize
                            int fontSize = Math.max(10, blockSize - blockSize / 3);
                            Font font = g.getFont().deriveFont((float) fontSize);
                            g.setFont(font);

                            // Text color: dark for light cells, light for dark cells
                            Color textColor;
                            if (color == Color.BLACK || color == Color.RED || color == Color.GREEN) {
                                textColor = Color.WHITE;
                            } else {
                                textColor = Color.DARK_GRAY;
                            }
                            g.setColor(textColor);

                            String text = String.valueOf(weight);
                            FontMetrics fm = g.getFontMetrics();
                            int textWidth = fm.stringWidth(text);
                            int textAscent = fm.getAscent();

                            // Center text within the block
                            int tx = x + (blockSize - textWidth) / 2;
                            int ty = y + (blockSize + textAscent) / 2 - 2; // slight upward adjustment
                            g.drawString(text, tx, ty);
                        }
                    }
                }
            }
        }

        @Override
        public Dimension getPreferredSize(){
            return new Dimension(canvasWidth, canvasHeight);
        }
    }
}

