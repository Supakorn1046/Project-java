import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GamePanel extends JPanel implements ActionListener {

    private static final int TOTAL_SPAWNS = 100;

    private static final int TIMER_DELAY = 30;

    private static final int GAME_WIDTH = 600;

    private static final int GAME_HEIGHT = 800;

    private static final int PERFECT_LINE_HEIGHT = GAME_HEIGHT-RhythmElement.SIZE-50;


    private final Color FADING_NICE_COLOUR = new Color(0,62,49);

    private final Color FADING_INCORRECT_COLOUR = new Color(128,0,0);

    private final Color FADING_PERFECT_COLOUR = new Color(255, 207, 61);

    private final Color FADING_TOOSOON_COLOUR = new Color(38, 216, 239);


    public enum GameState { Ready, Playing, GameEnded };


    private GameState gameState;

    private Timer gameTimer;

    private List<RhythmElement> rhythmElementList;

    private List<FadingEventText> fadingEventTexts;

    private Random rand;

    private int spawnTimer = 0;

    private int timeBetweenSpawns = 500;

    private int score;

    private int comboStreak;

    private int spawnsRemaining;

    private MessagePanel messagePanel;

    private int speedFactor = 5;


    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH,GAME_HEIGHT));
        setBackground(Color.lightGray);
        gameTimer = new Timer(TIMER_DELAY,this);
        rhythmElementList = new ArrayList<>();
        fadingEventTexts = new ArrayList<>();
        rand = new Random();
        spawnsRemaining = TOTAL_SPAWNS;
        messagePanel = new MessagePanel(new Position(0,GAME_HEIGHT/2-50), GAME_WIDTH,70);
        messagePanel.showStartMessage();
        comboStreak = 0;
        gameState = GameState.Ready;
    }


    public void handleInput(int keyCode) {
        if(keyCode == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        } else if(gameState == GameState.Playing && RhythmElement.isCharacterValid((char)keyCode)) {
            if(rhythmElementList.size() > 0) {
                handleRhythmInteraction((char)keyCode);
            }
        } else if(gameState == GameState.Ready && keyCode == KeyEvent.VK_SPACE) {
            gameState = GameState.Playing;
            gameTimer.start();
        } else if(gameState == GameState.GameEnded && keyCode == KeyEvent.VK_SPACE) {
            gameState = GameState.Ready;
            spawnsRemaining = TOTAL_SPAWNS;
            messagePanel.showStartMessage();
            comboStreak = 0;
            score = 0;
            repaint();
        }
    }


    public void update() {
        if(gameState != GameState.Playing) return;

        updateRhythmSpawnTimer();
        updateRhythmElements();
        updateFadingText();

        if(spawnsRemaining == 0 && rhythmElementList.size() == 0) {
            gameState = GameState.GameEnded;
            fadingEventTexts.clear();
            messagePanel.showGameOver(score);
            gameTimer.stop();
        }
        repaint();
    }


    public void paint(Graphics g) {
        super.paint(g);
        if(gameState == GameState.Playing) {
            for (RhythmElement rhythmElement : rhythmElementList) {
                rhythmElement.paint(g);
            }
            for(FadingEventText text : fadingEventTexts) {
                text.paint(g);
            }
        } else {
            messagePanel.paint(g);
        }
        // Draw circles to show where the perfect score can be achieved.
        g.setColor(Color.BLACK);
        for(int x = RhythmElement.SIZE; x < GAME_WIDTH-RhythmElement.SIZE; x+=RhythmElement.SIZE) {
            g.drawOval(x,PERFECT_LINE_HEIGHT,RhythmElement.SIZE,RhythmElement.SIZE);
        }
        drawScore(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }


    private void addNewRhythmElement() {
        if(spawnsRemaining == 0) return;
        spawnsRemaining--;

        int xPosition = rand.nextInt((getWidth()-2*RhythmElement.SIZE)/RhythmElement.SIZE)*RhythmElement.SIZE+RhythmElement.SIZE;
        rhythmElementList.add(new RhythmElement(new Position(xPosition,-RhythmElement.SIZE),rand));
    }


    private void updateRhythmSpawnTimer() {
        spawnTimer += TIMER_DELAY;
        if(spawnTimer >= timeBetweenSpawns) {
            addNewRhythmElement();
            spawnTimer = 0;
        }
    }


    private void updateRhythmElements() {
        for(int i = 0; i < rhythmElementList.size(); i++) {
            rhythmElementList.get(i).update(TIMER_DELAY/speedFactor);
            if(rhythmElementList.get(i).position.y > getHeight()) {
                rhythmElementList.remove(i);
                i--;
                comboStreak = 0;
            }
        }
    }


    private void updateFadingText() {
        for(int i = 0; i < fadingEventTexts.size(); i++) {
            fadingEventTexts.get(i).update(TIMER_DELAY);
            if(fadingEventTexts.get(i).isExpired()) {
                fadingEventTexts.remove(i);
                i--;
            }
        }
    }


    private void handleRhythmInteraction(char key) {
        // Find next element to be pressed (not too far past the Perfect).
        int rhythmIndex;
        for(rhythmIndex = 0; rhythmIndex < rhythmElementList.size(); rhythmIndex++) {
            if(rhythmElementList.get(rhythmIndex).position.y < GAME_HEIGHT-50) {
                break;
            }
        }
        if(rhythmIndex == rhythmElementList.size()) return;

        if(rhythmElementList.get(rhythmIndex).position.y < GAME_HEIGHT / 2) {
            addFadingText("TOO SOON!", rhythmIndex, FADING_TOOSOON_COLOUR);
            comboStreak = 0;
        } else if(rhythmElementList.get(rhythmIndex).isCharacterCorrect(key)) {
            if(rhythmElementList.get(rhythmIndex).position.y > PERFECT_LINE_HEIGHT - RhythmElement.SIZE/2) {
                score += (10 + comboStreak)*2;
                addFadingText("PERFECT! +" + (10 + comboStreak)*2, rhythmIndex, FADING_PERFECT_COLOUR);
                comboStreak++;
                System.out.println(score);
            } else {
                score += 10 + comboStreak;
                addFadingText("NICE! +" + (10 + comboStreak), rhythmIndex, FADING_NICE_COLOUR);
                comboStreak++;
                System.out.println(score);
            }
        } else {
            addFadingText("INCORRECT!", rhythmIndex, FADING_INCORRECT_COLOUR);
            comboStreak = 0;
        }
        rhythmElementList.remove(rhythmIndex);
    }


    private void addFadingText(String message, int elementIndexToSpawnOff, Color colour) {
        fadingEventTexts.add(new FadingEventText(message,
                new Position(rhythmElementList.get(elementIndexToSpawnOff).position), colour));
    }


    private void drawScore(Graphics g) {
        String scoreText = String.valueOf(score);
        g.setFont(new Font("Arial", Font.BOLD, 35));
        int width = g.getFontMetrics().stringWidth(scoreText);
        g.setColor(Color.BLACK);
        g.drawString(scoreText, GAME_WIDTH/2 - width/2, 40);
    }
}