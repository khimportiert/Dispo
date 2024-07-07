import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class DrawingPanel extends JPanel {
    protected int scale = 1;
    protected LinkedList<Fahrzeug> fahrzeuge = new LinkedList<>();
    protected LinkedList<Auftrag> aufträge = new LinkedList<>();
    protected LinkedList<Auftrag> lost = new LinkedList<>();
    protected LinkedList<Position[]> paths = new LinkedList<>();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fahrzeuge.forEach(v -> {
            drawFahrzeug(g2d, v);
        });

        aufträge.forEach(v -> {
            drawAuftrag(g2d, v, Color.black);
        });

        lost.forEach(v -> {
            drawLost(g2d, v);
        });

        paths.forEach(p -> {
            drawPath(g2d, p);
        });
    }

    void drawAuftrag(Graphics2D g2d, Auftrag v, Color c) {
        int x1 = v.startPosition.x * scale;
        int y1 = v.startPosition.y * scale;
        int x2 = v.endPosition.x * scale;
        int y2 = v.endPosition.y * scale;

        g2d.setColor(c);
        g2d.drawOval(x1, y1, 10, 10);
        g2d.drawString(v.arrivalTime.getTimeString(), x1, y1);
        g2d.drawOval(x2, y2, 10, 10);
        Time newtime = new Time(v.arrivalTime);
        g2d.drawString(newtime.addTime(v.travelTime).getTimeString(), x2, y2);

        g2d.drawLine(x1, y1, x2, y2);
    }

    void drawLost(Graphics2D g2d, Auftrag v) {
        drawAuftrag(g2d, v, Color.RED);
    }

    void drawFahrzeug(Graphics2D g2d, Fahrzeug v) {
        int x = v.position.x * scale;
        int y = v.position.y * scale;
        g2d.setColor(Color.BLUE);
        g2d.drawOval(x, y, 10, 10);
        g2d.drawString(v.label, x, y);
    }

    void drawPath(Graphics2D g2d, Position[] pos) {
        int x1 = pos[0].x * scale;
        int y1 = pos[0].y * scale;
        int x2 = pos[1].x * scale;
        int y2 = pos[1].y * scale;
        g2d.setColor(Color.BLUE);

        g2d.drawLine(x1, y1, x2, y2);
    }

    void setScale(int scale) {
        this.scale = scale;
    }

    void addFahrzeug(Fahrzeug v) {
        fahrzeuge.add(v);
    }

    void addAuftrag(Auftrag v) {
        aufträge.add(v);
    }

    void addLost(Auftrag v) {
        lost.add(v);
    }

    void addPath(Position[] pos) {
        this.paths.add(pos);
    }

    void clearPaths() {
        this.paths = new LinkedList<Position[]>();
    }

    public void save(String pathname)
    {
        BufferedImage bImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D cg = bImg.createGraphics();
        paintAll(cg);
        try {
            if (ImageIO.write(bImg, "png", new File(pathname)))
            {
                System.out.println("-- saved");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}