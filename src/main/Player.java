package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class Player {

    double x;
    double y;
    double z;
    double pitch;
    double yaw;
    private double speed;

    private boolean inEscapeMenu;

    private double windowWidth;
    private double windowHeight;

    public Player() {

        x = 0;
        y = 0;
        z = 0;
        speed = 1;

        pitch = 0;

        inEscapeMenu = false;

    }

    public double getSpeed() {
        return speed;
    }

    public void setWindowDimensions(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public void calculatePitch(int mouseX, int mouseY) {

        if (mouseX != windowWidth / 2) {
            if (mouseX - windowWidth / 2 > 0) {
                pitch -= 1;
            } else {
                pitch += 1;
            }
        }

        if (pitch < 0) {
            pitch *= -359;
        }
        if (pitch > 359) {
            pitch = 0;
        }

        System.out.println("pitch: " + pitch);

    }

    public boolean isInEscapeMenu() {
        return inEscapeMenu;
    }

    public void setInEscapeMenu(boolean b) {
        inEscapeMenu = b;
    }

}
