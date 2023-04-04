import java.awt.*;


public class FadingEventText {

    private Color colour;

    private Font font = new Font("Arial", Font.BOLD, 20);

    private Color drawColour;

    private Position position;

    private String text;

    private int fadeValue;

    public FadingEventText(String text, Position position, Color startColour) {
        this.colour = startColour;
        fadeValue = 255;
        drawColour = new Color(colour.getRed(),colour.getGreen(),colour.getBlue(),fadeValue);
        this.position = position;
        this.text = text;
    }

    public void update(int deltaTime) {
        int changeAmount = deltaTime / 6;
        fadeValue = Math.max(0,fadeValue - changeAmount);
        position.y -= changeAmount / 2;
        drawColour = new Color(colour.getRed(),colour.getGreen(),colour.getBlue(),fadeValue);
    }

    public boolean isExpired() {
        return fadeValue == 0;
    }


    public void paint(Graphics g) {
        g.setColor(drawColour);
        g.setFont(font);
        g.drawString(text,position.x, position.y);
    }
}