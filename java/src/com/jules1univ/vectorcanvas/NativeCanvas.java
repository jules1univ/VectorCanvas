package com.jules1univ.vectorcanvas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JPanel;

public final class NativeCanvas extends JPanel {

    private static native BufferedImage render(String xml);

    static {
        NativeLibLoader.load();
    }

    private String svg;
    private BufferedImage image;

    public static NativeCanvas of(String svg) {
        return new NativeCanvas(svg);
    }

    public static NativeCanvas of(Path path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return new NativeCanvas(sb.toString());
        } catch (IOException e) {
            return null;
        }
    }

    private NativeCanvas(String xml) {
        this.svg = xml;
        this.update();
    }

    public void setSVG(String xml) {
        this.svg = xml;
        
        this.update();
        
        this.revalidate();
        this.repaint();
    }

    public String getSVG() {
        return svg;
    }

    public BufferedImage getImage() {
        return image;
    }

    private void update() {
        if (this.svg == null || this.svg.isBlank()) {
            this.image = null;
            return;
        }
        try {
            this.image = render(svg);
        } catch (Exception e) {
            this.image = null;
        }

        if (image != null) {
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int x = (getWidth()  - image.getWidth())  / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            g.drawImage(image, x, y, this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (image != null) {
            return new Dimension(image.getWidth(), image.getHeight());
        }
        return super.getPreferredSize();
    }
}