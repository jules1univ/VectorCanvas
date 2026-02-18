package com.jules1univ.vectorcanvas;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public final class NativeCanvasTest {

    private static final String SIMPLE_SVG = "<?SVG version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<svg width=\"100\" height=\"100\" SVGns=\"http://www.w3.org/2000/svg\">\n" +
            "  <rect x=\"10\" y=\"10\" width=\"80\" height=\"80\" fill=\"blue\"/>\n" +
            "</svg>";

    private static final String CIRCLE_SVG = "<?SVG version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<svg width=\"200\" height=\"200\" SVGns=\"http://www.w3.org/2000/svg\">\n" +
            "  <circle cx=\"100\" cy=\"100\" r=\"50\" fill=\"red\"/>\n" +
            "</svg>";

    private static final String INVALID_SVG = "<svg><rect></svg>";

    private static Path tempSvgFile;

    @BeforeAll
    static public void setUpClass() throws IOException {
        tempSvgFile = Files.createTempFile("test", ".svg");
        Files.writeString(tempSvgFile, SIMPLE_SVG);
    }

    @AfterAll
    static public void tearDownClass() throws IOException {
        if (tempSvgFile != null && Files.exists(tempSvgFile)) {
            Files.delete(tempSvgFile);
        }
    }

    @Test
    public void testNativeLibraryLoads() {
        assertDoesNotThrow(() -> NativeLibLoader.load());
    }

    @Test
    public void testCreateFromValidSvgString() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        assertNotNull(canvas, "Canvas should not be null");
        assertEquals(SIMPLE_SVG, canvas.getSVG(), "SVG should match input");
    }

    @Test
    public void testCreateFromFilePath() {
        NativeCanvas canvas = NativeCanvas.of(tempSvgFile);

        assertNotNull(canvas, "Canvas should not be null");
        assertNotNull(canvas.getSVG(), "SVG should not be null");
        assertTrue(canvas.getSVG().contains("<svg"), "SVG should contain SVG content");
    }

    @Test
    public void testCreateFromNonExistentFile() {
        Path nonExistent = Path.of("does_not_exist.svg");
        NativeCanvas canvas = NativeCanvas.of(nonExistent);

        assertNull(canvas, "Canvas should be null for non-existent file");
    }

    @Test
    public void testSetSVG() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        canvas.setSVG(CIRCLE_SVG);

        assertEquals(CIRCLE_SVG, canvas.getSVG(), "SVG should be updated");
    }

    @Test
    public void testGetSVG() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        String SVG = canvas.getSVG();

        assertNotNull(SVG, "SVG should not be null");
        assertEquals(SIMPLE_SVG, SVG, "SVG should match input");
    }

    @Test
    public void testNullSVG() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        assertDoesNotThrow(() -> canvas.setSVG(null));
        assertNull(canvas.getSVG(), "SVG should be null");
    }

    @Test
    public void testEmptySVG() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        assertDoesNotThrow(() -> canvas.setSVG(""));
        assertEquals("", canvas.getSVG(), "SVG should be empty string");
    }

    @Test
    public void testBlankSVG() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        assertDoesNotThrow(() -> canvas.setSVG("   "));
        assertEquals("   ", canvas.getSVG(), "SVG should be blank string");
    }

    @Test
    public void testPreferredSizeWithValidSvg() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        Dimension size = canvas.getPreferredSize();

        assertNotNull(size, "Preferred size should not be null");
        assertTrue(size.width > 0, "Width should be positive");
        assertTrue(size.height > 0, "Height should be positive");
    }

    @Test
    public void testInvalidSVG() {
        assertDoesNotThrow(() -> {
            NativeCanvas canvas = NativeCanvas.of(INVALID_SVG);
            assertNotNull(canvas, "Canvas should be created even with invalid SVG");
        });
    }

    @Test
    public void testMultipleCanvasInstances() {
        NativeCanvas canvas1 = NativeCanvas.of(SIMPLE_SVG);
        NativeCanvas canvas2 = NativeCanvas.of(CIRCLE_SVG);

        assertNotNull(canvas1, "First canvas should not be null");
        assertNotNull(canvas2, "Second canvas should not be null");
        assertNotEquals(canvas1.getSVG(), canvas2.getSVG(),
                "Canvases should have different SVG");
    }

    @Test
    public void testMultipleSVGUpdates() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        canvas.setSVG(CIRCLE_SVG);
        assertEquals(CIRCLE_SVG, canvas.getSVG());

        canvas.setSVG(SIMPLE_SVG);
        assertEquals(SIMPLE_SVG, canvas.getSVG());

        canvas.setSVG("");
        assertEquals("", canvas.getSVG());
    }

    @Test
    public void testCanvasIsJPanel() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        assertTrue(canvas instanceof JPanel,
                "NativeCanvas should be a JPanel");
    }

    @Test
    public void testComplexSvg() {
        String complexSvg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg width=\"300\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                "  <rect x=\"10\" y=\"10\" width=\"100\" height=\"100\" fill=\"red\"/>\n" +
                "  <circle cx=\"200\" cy=\"200\" r=\"40\" fill=\"blue\"/>\n" +
                "  <line x1=\"0\" y1=\"0\" x2=\"300\" y2=\"300\" stroke=\"green\" stroke-width=\"2\"/>\n" +
                "  <text x=\"150\" y=\"150\" font-size=\"20\" fill=\"black\">Test</text>\n" +
                "</svg>";

        assertDoesNotThrow(() -> {
            NativeCanvas canvas = NativeCanvas.of(complexSvg);
            assertNotNull(canvas);
            assertEquals(complexSvg, canvas.getSVG());
        });
    }

    @Test
    public void testSvgWithAttributes() {
        String svgWithAttrs = "<svg width=\"100\" height=\"100\" viewBox=\"0 0 100 100\" " +
                "SVGns=\"http://www.w3.org/2000/svg\">\n" +
                "  <rect x=\"10\" y=\"10\" width=\"80\" height=\"80\" " +
                "fill=\"blue\" stroke=\"black\" stroke-width=\"2\"/>\n" +
                "</svg>";

        NativeCanvas canvas = NativeCanvas.of(svgWithAttrs);

        assertNotNull(canvas);
        assertEquals(svgWithAttrs, canvas.getSVG());
    }

    @Test
    public void testMultipleLibraryLoads() {
        assertDoesNotThrow(() -> {
            NativeLibLoader.load();
            NativeLibLoader.load();
            NativeLibLoader.load();
        });
    }

    @Test
    public void testExportImage() {
        NativeCanvas canvas = NativeCanvas.of(SIMPLE_SVG);

        assertNotNull(canvas.getImage(), "Image should not be null after rendering");

        File outputImage = new File("./images/test.png");
        outputImage.mkdirs();

        assertDoesNotThrow(() -> ImageIO.write(canvas.getImage(), "png", outputImage),
                "Should be able to write image to file");
    }
}
