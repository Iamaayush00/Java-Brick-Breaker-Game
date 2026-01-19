package BrickBreakerGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class BrickBreaker extends JPanel implements KeyListener, ActionListener {
    // Game states
    private enum GameState { MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE }
    private GameState gameState = GameState.MENU;
    
    // Game settings
    int level = 1;
    int score = 0;
    int lives = 3;
    private int totalBricks = 0;
    private int bricksDestroyed = 0;
    
    // Timer and animation
    private Timer timer;
    private int delay = 8;
    private long lastTime = System.currentTimeMillis();
    
    // Paddle
    private int playerX = 310;
    private int paddleWidth = 100;
    private int paddleHeight = 12;
    private int paddleY = 530;
    private int paddleSpeed = 0;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    // Ball
    private double ballPosX = 340;
    private double ballPosY = 400;
    double ballXDir = 0;
    double ballYDir = 0;
    private int ballSize = 18;
    double ballSpeed = 3.5;
    
    // Visual effects
    private ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private BrickGenerator bricks;
    private Random random = new Random();
    
    // Colors
    private Color[] brickColors = {
        new Color(231, 76, 60),   // Red
        new Color(230, 126, 34),  // Orange
        new Color(241, 196, 15),  // Yellow
        new Color(46, 204, 113),  // Green
        new Color(52, 152, 219),  // Blue
        new Color(155, 89, 182),  // Purple
        new Color(236, 240, 241)  // Light gray
    };
    
    private Color backgroundColor = new Color(20, 25, 35);
    private Color paddleColor = new Color(52, 152, 219);
    private Color ballColor = new Color(255, 255, 255);

    public BrickBreaker() {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setPreferredSize(new Dimension(700, 600));
        timer = new Timer(delay, this);
        timer.start();
        initLevel(level);
    }
    
    private void initLevel(int lvl) {
        int rows = Math.min(3 + lvl, 8);
        int cols = 7;
        bricks = new BrickGenerator(rows, cols, brickColors);
        totalBricks = rows * cols;
        bricksDestroyed = 0;
        
        // Reset ball position but keep lives and score
        ballPosX = 340;
        ballPosY = 400;
        ballXDir = 0;
        ballYDir = 0;
        playerX = 300;
        paddleSpeed = 0;
    }
    
    private void resetGame() {
        level = 1;
        score = 0;
        lives = 3;
        initLevel(level);
        gameState = GameState.MENU;
    }
    
    private void launchBall() {
        if (ballYDir == 0) {
            double angle = -60 - random.nextInt(60); // -60 to -120 degrees
            ballXDir = ballSpeed * Math.cos(Math.toRadians(angle));
            ballYDir = ballSpeed * Math.sin(Math.toRadians(angle));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Background with gradient
        GradientPaint bgGradient = new GradientPaint(0, 0, backgroundColor, 
                                                      0, getHeight(), backgroundColor.darker());
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        if (gameState == GameState.MENU) {
            drawMenu(g2d);
        } else {
            drawGame(g2d);
            
            if (gameState == GameState.PAUSED) {
                drawPaused(g2d);
            } else if (gameState == GameState.GAME_OVER) {
                drawGameOver(g2d);
            } else if (gameState == GameState.LEVEL_COMPLETE) {
                drawLevelComplete(g2d);
            }
        }
    }
    
    private void drawMenu(Graphics2D g2d) {
        // Title
        g2d.setColor(new Color(52, 152, 219));
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "BRICK BREAKER";
        g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 150);
        
        // Subtitle with glow effect
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = g2d.getFontMetrics();
        String subtitle = "Press SPACE to Start";
        int x = (getWidth() - fm.stringWidth(subtitle)) / 2;
        int y = 250;
        
        // Pulsing effect
        int alpha = (int)(127 + 127 * Math.sin(System.currentTimeMillis() / 500.0));
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.drawString(subtitle, x, y);
        
        // Instructions
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String[] instructions = {
            "Arrow Keys - Move Paddle",
            "SPACE - Launch Ball / Pause",
            "ESC - Return to Menu",
            "",
            "Break all bricks to advance!",
            "Collect power-ups for bonuses!"
        };
        
        int startY = 320;
        for (String line : instructions) {
            fm = g2d.getFontMetrics();
            g2d.drawString(line, (getWidth() - fm.stringWidth(line)) / 2, startY);
            startY += 30;
        }
    }
    
    private void drawGame(Graphics2D g2d) {
        // Draw bricks
        bricks.draw(g2d);
        
        // Draw particles
        for (Particle p : particles) {
            p.draw(g2d);
        }
        
        // Draw power-ups
        for (PowerUp p : powerUps) {
            p.draw(g2d);
        }
        
        // Draw paddle with gradient
        GradientPaint paddleGradient = new GradientPaint(
            playerX, paddleY, paddleColor.brighter(),
            playerX, paddleY + paddleHeight, paddleColor.darker()
        );
        g2d.setPaint(paddleGradient);
        RoundRectangle2D paddle = new RoundRectangle2D.Double(
            playerX, paddleY, paddleWidth, paddleHeight, 10, 10
        );
        g2d.fill(paddle);
        
        // Paddle highlight
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillRoundRect(playerX, paddleY, paddleWidth, paddleHeight/3, 10, 10);
        
        // Draw ball with glow effect
        // Outer glow
        for (int i = 3; i >= 0; i--) {
            int alpha = 50 - i * 12;
            g2d.setColor(new Color(255, 255, 255, alpha));
            g2d.fillOval((int)ballPosX - i*2, (int)ballPosY - i*2, 
                         ballSize + i*4, ballSize + i*4);
        }
        
        // Ball
        GradientPaint ballGradient = new GradientPaint(
            (float)ballPosX, (float)ballPosY, Color.WHITE,
            (float)ballPosX + ballSize, (float)ballPosY + ballSize, ballColor
        );
        g2d.setPaint(ballGradient);
        g2d.fillOval((int)ballPosX, (int)ballPosY, ballSize, ballSize);
        
        // Draw HUD
        drawHUD(g2d);
    }
    
    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.WHITE);
        
        // Score
        g2d.drawString("Score: " + score, 20, 30);
        
        // Level
        g2d.drawString("Level: " + level, getWidth()/2 - 40, 30);
        
        // Lives
        g2d.drawString("Lives: ", getWidth() - 150, 30);
        for (int i = 0; i < lives; i++) {
            g2d.setColor(new Color(231, 76, 60));
            g2d.fillOval(getWidth() - 90 + i * 25, 15, 18, 18);
        }
        
        // Progress bar
        int barWidth = 200;
        int barX = (getWidth() - barWidth) / 2;
        int barY = getHeight() - 25;
        
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRoundRect(barX, barY, barWidth, 10, 5, 5);
        
        int progress = totalBricks > 0 ? (bricksDestroyed * barWidth) / totalBricks : 0;
        GradientPaint progressGradient = new GradientPaint(
            barX, barY, new Color(46, 204, 113),
            barX + progress, barY, new Color(39, 174, 96)
        );
        g2d.setPaint(progressGradient);
        g2d.fillRoundRect(barX, barY, progress, 10, 5, 5);
    }
    
    private void drawPaused(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "PAUSED";
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Press SPACE to Resume";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 50);
    }
    
    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(new Color(231, 76, 60));
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "GAME OVER";
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 - 50);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        text = "Final Score: " + score;
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 20);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Press ENTER to Restart";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 70);
    }
    
    private void drawLevelComplete(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(new Color(46, 204, 113));
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "LEVEL COMPLETE!";
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 - 50);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        text = "Score: " + score;
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 20);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Press ENTER for Next Level";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 70);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            // Update paddle
            if (leftPressed) {
                paddleSpeed = -8;
            } else if (rightPressed) {
                paddleSpeed = 8;
            } else {
                paddleSpeed *= 0.9; // Friction
            }
            
            playerX += paddleSpeed;
            playerX = Math.max(0, Math.min(playerX, getWidth() - paddleWidth));
            
            // Update ball
            if (ballYDir != 0 || ballXDir != 0) {
                ballPosX += ballXDir;
                ballPosY += ballYDir;
                
                // Ball collision with walls
                if (ballPosX <= 0 || ballPosX >= getWidth() - ballSize) {
                    ballXDir = -ballXDir;
                    ballPosX = Math.max(0, Math.min(ballPosX, getWidth() - ballSize));
                }
                
                if (ballPosY <= 0) {
                    ballYDir = -ballYDir;
                    ballPosY = 0;
                }
                
                // Ball collision with paddle
                Rectangle2D ballRect = new Rectangle2D.Double(ballPosX, ballPosY, ballSize, ballSize);
                Rectangle2D paddleRect = new Rectangle2D.Double(playerX, paddleY, paddleWidth, paddleHeight);
                
                if (ballRect.intersects(paddleRect) && ballYDir > 0) {
                    // Calculate hit position on paddle (-1 to 1)
                    double hitPos = ((ballPosX + ballSize/2) - (playerX + paddleWidth/2)) / (paddleWidth/2);
                    
                    // Adjust ball angle based on hit position
                    double angle = hitPos * 60; // Max 60 degrees from vertical
                    ballXDir = ballSpeed * Math.sin(Math.toRadians(angle));
                    ballYDir = -ballSpeed * Math.cos(Math.toRadians(angle));
                    
                    ballPosY = paddleY - ballSize;
                    createParticles((int)ballPosX + ballSize/2, (int)ballPosY + ballSize/2, paddleColor, 5);
                }
                
                // Ball collision with bricks
                for (int i = 0; i < bricks.map.length; i++) {
                    for (int j = 0; j < bricks.map[0].length; j++) {
                        if (bricks.map[i][j] > 0) {
                            int brickX = j * bricks.brickWidth + 80;
                            int brickY = i * bricks.brickHeight + 50;
                            Rectangle2D brickRect = new Rectangle2D.Double(
                                brickX, brickY, bricks.brickWidth, bricks.brickHeight
                            );
                            
                            if (ballRect.intersects(brickRect)) {
                                Color brickColor = brickColors[bricks.map[i][j] - 1];
                                bricks.setBrickValue(0, i, j);
                                bricksDestroyed++;
                                score += 10 * level;
                                
                                // Particle effect
                                createParticles(brickX + bricks.brickWidth/2, 
                                              brickY + bricks.brickHeight/2, brickColor, 15);
                                
                                // Random power-up drop (15% chance)
                                if (random.nextDouble() < 0.15) {
                                    powerUps.add(new PowerUp(brickX + bricks.brickWidth/2, 
                                                            brickY + bricks.brickHeight/2));
                                }
                                
                                // Ball bounce direction
                                double ballCenterX = ballPosX + ballSize/2;
                                double ballCenterY = ballPosY + ballSize/2;
                                
                                if (ballCenterX < brickX || ballCenterX > brickX + bricks.brickWidth) {
                                    ballXDir = -ballXDir;
                                } else {
                                    ballYDir = -ballYDir;
                                }
                                
                                // Check level complete
                                if (bricksDestroyed >= totalBricks) {
                                    gameState = GameState.LEVEL_COMPLETE;
                                }
                                
                                break;
                            }
                        }
                    }
                }
                
                // Ball falls off screen
                if (ballPosY > getHeight()) {
                    lives--;
                    if (lives <= 0) {
                        gameState = GameState.GAME_OVER;
                    } else {
                        ballPosX = playerX + paddleWidth/2 - ballSize/2;
                        ballPosY = paddleY - ballSize - 5;
                        ballXDir = 0;
                        ballYDir = 0;
                    }
                }
            } else {
                // Ball follows paddle before launch
                ballPosX = playerX + paddleWidth/2 - ballSize/2;
                ballPosY = paddleY - ballSize - 5;
            }
            
            // Update particles
            Iterator<Particle> particleIter = particles.iterator();
            while (particleIter.hasNext()) {
                Particle p = particleIter.next();
                p.update();
                if (p.isDead()) {
                    particleIter.remove();
                }
            }
            
            // Update power-ups
            Iterator<PowerUp> powerUpIter = powerUps.iterator();
            while (powerUpIter.hasNext()) {
                PowerUp p = powerUpIter.next();
                p.update();
                
                if (p.y > getHeight()) {
                    powerUpIter.remove();
                } else {
                    Rectangle2D powerUpRect = new Rectangle2D.Double(p.x - 15, p.y - 15, 30, 30);
                    Rectangle2D paddleRect = new Rectangle2D.Double(playerX, paddleY, paddleWidth, paddleHeight);
                    
                    if (powerUpRect.intersects(paddleRect)) {
                        p.activate(this);
                        powerUpIter.remove();
                        createParticles((int)p.x, (int)p.y, p.color, 20);
                    }
                }
            }
        }
        
        repaint();
    }
    
    private void createParticles(int x, int y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_ESCAPE) {
            if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
                resetGame();
            }
        }
        
        if (gameState == GameState.MENU) {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {
                gameState = GameState.PLAYING;
            }
        } else if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (key == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (key == KeyEvent.VK_SPACE) {
                if (ballYDir == 0) {
                    launchBall();
                } else {
                    gameState = GameState.PAUSED;
                }
            }
        } else if (gameState == GameState.PAUSED) {
            if (key == KeyEvent.VK_SPACE) {
                gameState = GameState.PLAYING;
            }
        } else if (gameState == GameState.GAME_OVER) {
            if (key == KeyEvent.VK_ENTER) {
                resetGame();
            }
        } else if (gameState == GameState.LEVEL_COMPLETE) {
            if (key == KeyEvent.VK_ENTER) {
                level++;
                initLevel(level);
                gameState = GameState.PLAYING;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Brick Breaker");
            BrickBreaker game = new BrickBreaker();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

// Brick Generator Class
class BrickGenerator {
    public int[][] map;
    public int brickWidth;
    public int brickHeight;
    private Color[] colors;

    public BrickGenerator(int row, int col, Color[] colors) {
        this.colors = colors;
        map = new int[row][col];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = (i % colors.length) + 1;
            }
        }
        brickWidth = 540 / col;
        brickHeight = 150 / row;
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    int x = j * brickWidth + 80;
                    int y = i * brickHeight + 50;
                    
                    Color brickColor = colors[map[i][j] - 1];
                    
                    // Gradient fill
                    GradientPaint gradient = new GradientPaint(
                        x, y, brickColor.brighter(),
                        x, y + brickHeight, brickColor.darker()
                    );
                    g.setPaint(gradient);
                    g.fillRoundRect(x, y, brickWidth - 2, brickHeight - 2, 8, 8);
                    
                    // Highlight
                    g.setColor(new Color(255, 255, 255, 80));
                    g.fillRoundRect(x, y, brickWidth - 2, brickHeight/3, 8, 8);
                    
                    // Border
                    g.setColor(brickColor.darker().darker());
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(x, y, brickWidth - 2, brickHeight - 2, 8, 8);
                }
            }
        }
    }

    public void setBrickValue(int value, int row, int col) {
        map[row][col] = value;
    }
}

// Particle Class
class Particle {
    private double x, y;
    private double vx, vy;
    private Color color;
    private int life;
    private int maxLife;
    private double size;
    
    public Particle(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        
        Random rand = new Random();
        double angle = rand.nextDouble() * Math.PI * 2;
        double speed = 2 + rand.nextDouble() * 3;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        
        this.maxLife = 30 + rand.nextInt(20);
        this.life = maxLife;
        this.size = 3 + rand.nextDouble() * 3;
    }
    
    public void update() {
        x += vx;
        y += vy;
        vy += 0.3; // Gravity
        life--;
    }
    
    public void draw(Graphics2D g) {
        int alpha = (int)(255 * (life / (double)maxLife));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 
                            Math.max(0, Math.min(255, alpha))));
        g.fillOval((int)x, (int)y, (int)size, (int)size);
    }
    
    public boolean isDead() {
        return life <= 0;
    }
}

// PowerUp Class
class PowerUp {
    double x, y;
    double vy = 2;
    Color color;
    PowerUpType type;
    
    enum PowerUpType {
        EXTRA_LIFE(new Color(231, 76, 60)),
        SCORE_BOOST(new Color(241, 196, 15)),
        SLOW_BALL(new Color(52, 152, 219));
        
        Color color;
        PowerUpType(Color c) { this.color = c; }
    }
    
    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
        
        PowerUpType[] types = PowerUpType.values();
        this.type = types[new Random().nextInt(types.length)];
        this.color = type.color;
    }
    
    public void update() {
        y += vy;
    }
    
    public void draw(Graphics2D g) {
        // Glow effect
        for (int i = 2; i >= 0; i--) {
            int alpha = 30 - i * 10;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g.fillOval((int)x - 15 - i*3, (int)y - 15 - i*3, 30 + i*6, 30 + i*6);
        }
        
        // Main circle
        GradientPaint gradient = new GradientPaint(
            (float)x - 15, (float)y - 15, color.brighter(),
            (float)x + 15, (float)y + 15, color.darker()
        );
        g.setPaint(gradient);
        g.fillOval((int)x - 12, (int)y - 12, 24, 24);
        
        // Symbol
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        String symbol = type == PowerUpType.EXTRA_LIFE ? "♥" : 
                       type == PowerUpType.SCORE_BOOST ? "★" : "◐";
        g.drawString(symbol, (int)x - fm.stringWidth(symbol)/2, (int)y + 6);
    }
    
    public void activate(BrickBreaker game) {
        switch (type) {
            case EXTRA_LIFE:
                game.lives++;
                break;
            case SCORE_BOOST:
                game.score += 50 * game.level;
                break;
            case SLOW_BALL:
                game.ballSpeed = Math.max(2.5, game.ballSpeed * 0.8);
                double currentSpeed = Math.sqrt(game.ballXDir * game.ballXDir + 
                                               game.ballYDir * game.ballYDir);
                if (currentSpeed > 0) {
                    game.ballXDir = (game.ballXDir / currentSpeed) * game.ballSpeed;
                    game.ballYDir = (game.ballYDir / currentSpeed) * game.ballSpeed;
                }
                break;
        }
    }
}