package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Shape {

    private ArrayList<Triangle> tris;
    private ShapeType type;
    private Player player;

    private int windowWidth;
    private int windowHeight;

    private boolean inflated;
    private boolean display;

    double x;
    double y;
    double z;
    double pitchToPlayerRAW;
    double pitchToPlayerFOV;
    double distToPlayer;



    public Shape(ArrayList<Triangle> tris, ShapeType type, double x, double y, double z) {
        this.tris = tris;
        this.type = type;

        inflated = false;
        display = false;

        this.x = x;
        this.y = y;
        this.z = z;

    }

    public void setWindowDimensions(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ArrayList<Triangle> getTris() {
        return tris;
    }

    public ShapeType getType() {
        return type;
    }

    public void draw(Graphics2D g2) {

        if (display) {

            if (type == ShapeType.SPHERE) {


                // FIX INFLATE PROBLEM (run to see)


                if (inflated) {

                } else {

                    for (int i = 0; i < 4; i++) {
                        inflate();
                    }

                    inflated = true;

                }

                double heading = Math.toRadians(pitchToPlayerFOV / 3);
                Matrix3 headingTransform = new Matrix3(new double[]{
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });
                double pitch = Math.toRadians(0);
                Matrix3 pitchTransform = new Matrix3(new double[]{
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 zoomTransform = new Matrix3(new double[]{
                        3 / distToPlayer, 0, 0,
                        0, 3 / distToPlayer, 0,
                        0, 0, 3 / distToPlayer
                });

                Matrix3 transform = headingTransform.multiply(pitchTransform).multiply(zoomTransform);

                BufferedImage img = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);

                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                applyTransforms(transform, zBuffer, img);

                g2.drawImage(img, 0, 0, null);
            } else if (type == ShapeType.RECTANGLE) {

                double heading = Math.toRadians(pitchToPlayerFOV / 3);
                Matrix3 headingTransform = new Matrix3(new double[]{
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });
                double pitch = Math.toRadians(0);
                Matrix3 pitchTransform = new Matrix3(new double[]{
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 zoomTransform = new Matrix3(new double[]{
                        3 / distToPlayer, 0, 0,
                        0, 3 / distToPlayer, 0,
                        0, 0, 3 / distToPlayer
                });

                Matrix3 transform = headingTransform.multiply(pitchTransform).multiply(zoomTransform);

                BufferedImage img = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);

                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                applyTransforms(transform, zBuffer, img);

                g2.drawImage(img, 0, 0, null);

            }
        }

    }

    public void applyTransforms(Matrix3 transform, double[] zBuffer, BufferedImage img) {

        int step = windowWidth / 90;
        double temp = pitchToPlayerFOV + 45;
        temp = temp * step;
        System.out.println("Temp:" + temp);

        for (Triangle t : tris) {
            Vertex v1 = transform.transform(t.v1);
            v1.x += temp;
            v1.y += windowHeight / 2;
            Vertex v2 = transform.transform(t.v2);
            v2.x += temp;
            v2.y += windowHeight / 2;
            Vertex v3 = transform.transform(t.v3);
            v3.x += temp;
            v3.y += windowHeight / 2;

            Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
            Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
            Vertex norm = new Vertex(
                    ab.y * ac.z - ab.z * ac.y,
                    ab.z * ac.x - ab.x * ac.z,
                    ab.x * ac.y - ab.y * ac.x
            );

            double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
            norm.x /= normalLength;
            norm.y /= normalLength;
            norm.z /= normalLength;

            double angleCos = Math.abs(norm.z);

            int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
            int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
            int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
            int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

            double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                    double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                    double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                    if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                        double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                        int zIndex = y * img.getWidth() + x;
                        if (zBuffer[zIndex] < depth) {
                            img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
                            zBuffer[zIndex] = depth;
                        }
                    }
                }
            }
        }

    }

    public Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1 / 2.4);
        int green = (int) Math.pow(greenLinear, 1 / 2.4);
        int blue = (int) Math.pow(blueLinear, 1 / 2.4);

        return new Color(red, green, blue);
    }

    // USED FOR SPHERES
    // SPLITS EACH TRIANGLE INTO FOUR

    public void inflate() {

        ArrayList<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x) / 2, (t.v1.y + t.v2.y) / 2, (t.v1.z + t.v2.z) / 2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x) / 2, (t.v2.y + t.v3.y) / 2, (t.v2.z + t.v3.z) / 2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x) / 2, (t.v1.y + t.v3.y) / 2, (t.v1.z + t.v3.z) / 2);
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }
        for (Triangle t : result) {
            for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        tris = result;

    }

    public boolean calculatePitchToPlayer() {
        double dx = x - player.x;
        double dy = player.y - y;
        double dz = z - player.z;
        double h = Math.hypot(dx, dz);
        distToPlayer = h;

        double temp = Math.atan(dz / dx);
        temp *= 180/Math.PI; // convert to degrees
        System.out.println("tan: " + temp);
        pitchToPlayerRAW = temp;
        System.out.println(pitchToPlayerRAW);

        pitchToPlayerFOV = player.pitch - pitchToPlayerRAW;

        if (Math.abs(pitchToPlayerFOV) >= 45) {
            display = false;
            return false;
        }
        display = true;
        return true;

    }

}
