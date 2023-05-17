package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main extends JPanel implements KeyListener, ActionListener, MouseMotionListener {

    private static Player player = new Player();
    private static JFrame frame = new JFrame();
    private static Container pane = frame.getContentPane();

    private ArrayList<Shape> shapes;

    private Graphics2D graphics;

    public static void main(String[] args) {

        Main game = new Main();
        pane.add(game);
        pane.addKeyListener(game);
        frame.addKeyListener(game);
        frame.addMouseMotionListener(game);
        pane.setFocusable(true);
        frame.setFocusable(true);

    }

    public Main() {

        shapes = new ArrayList<>();

        addKeyListener(this);

        pane.setLayout(new BorderLayout());

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setVisible(true);

        setFocusable(true);

        ArrayList<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));

        Shape demoSphere = new Shape(tris, ShapeType.SPHERE, 0, 0, 50);
        demoSphere.setWindowDimensions(frame.getWidth(), frame.getHeight());
        demoSphere.setPlayer(player);

        shapes.add(demoSphere);

    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        graphics = g2;

        g2.setColor(Color.CYAN);
        g2.fillRect(0, 0, frame.getWidth(), frame.getHeight() / 2);
        g2.setColor(Color.GREEN);
        g2.fillRect(0, frame.getHeight() / 2, frame.getWidth(), frame.getHeight() / 2);

        player.setWindowDimensions(frame.getWidth(), frame.getHeight());
        if (player.isInEscapeMenu()) {
            graphics.setColor(Color.BLACK);
            graphics.drawString("Escape Menu", getWidth() / 2, getHeight() / 2);
        }

        for (Shape s : shapes) {

            if (s.calculatePitchToPlayer()) {
                s.setWindowDimensions(frame.getWidth(), frame.getHeight());
                s.draw(g2);
            }

        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int keyCode = e.getKeyCode();

        // DOUBLE REPAINTS ARE FOR SMOOTHER ANIMATIONS

        if (keyCode == KeyEvent.VK_D) {

            player.x += player.getSpeed();

        } else if (keyCode == KeyEvent.VK_A) {

            player.x -= player.getSpeed();

        } else if (keyCode == KeyEvent.VK_W) {

            player.z += player.getSpeed();

        } else if (keyCode == KeyEvent.VK_S) {

            player.z -= player.getSpeed();

        } else if (keyCode == KeyEvent.VK_ESCAPE) {

            if (player.isInEscapeMenu()) {
                player.setInEscapeMenu(false);
            } else {
                player.setInEscapeMenu(true);
            }

        }
        repaint();
    }
    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

        if (player.isInEscapeMenu()) {

        } else {
            player.calculatePitch(e.getX(), e.getY());
            moveMouse(new Point(getWidth() / 2, getHeight() / 2));
            repaint();
        }


    }

    public void moveMouse(Point p) {
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        // Search the devices for the one that draws the specified point.
        for (GraphicsDevice device: gs) {
            GraphicsConfiguration[] configurations =
                    device.getConfigurations();
            for (GraphicsConfiguration config: configurations) {
                Rectangle bounds = config.getBounds();
                if(bounds.contains(p)) {
                    // Set point to screen coordinates.
                    Point b = bounds.getLocation();
                    Point s = new Point(p.x - b.x, p.y - b.y);

                    try {
                        Robot r = new Robot(device);
                        r.mouseMove(s.x, s.y);
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }

                    return;
                }
            }
        }
        // Couldn't move to the point, it may be off screen.
        return;
    }

}

