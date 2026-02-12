/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.pdmodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Test case for AddImageToPDF.
 *
 * @author Test Author
 */
public class AddImageToPDFTest extends TestCase
{
    private final String outDir = "target/test-output/examples/pdmodel/";
    private File inputPdf;
    private File testJpgImage;
    private File testPngImage;
    private File testTiffImage;
    private File outputPdf;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        File dir = new File(outDir);
        dir.mkdirs();

        // Create a test input PDF
        inputPdf = new File(outDir, "input.pdf");
        createInputPDF(inputPdf);

        // Create test images
        testJpgImage = new File(outDir, "test.jpg");
        testPngImage = new File(outDir, "test.png");
        testTiffImage = new File(outDir, "test.tif");
        createTestImage(testJpgImage, "jpg");
        createTestImage(testPngImage, "png");

        outputPdf = new File(outDir, "output.pdf");
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        deleteIfExists(inputPdf);
        deleteIfExists(testJpgImage);
        deleteIfExists(testPngImage);
        deleteIfExists(testTiffImage);
        deleteIfExists(outputPdf);
    }

    private void deleteIfExists(File file)
    {
        if (file != null && file.exists())
        {
            file.delete();
        }
    }

    /**
     * Create a simple input PDF for testing.
     */
    private void createInputPDF(File file) throws Exception
    {
        PDDocument document = new PDDocument();
        try
        {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.moveTextPositionByAmount(100, 700);
            contentStream.drawString("Original PDF content");
            contentStream.endText();
            contentStream.close();

            document.save(file);
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Create a test image for testing.
     */
    private void createTestImage(File file, String format) throws IOException
    {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.setColor(Color.RED);
        g.fillOval(25, 25, 50, 50);
        g.dispose();

        ImageIO.write(image, format, file);
    }

    /**
     * Test adding a JPG image to PDF.
     */
    public void testAddJpgImageToPDF() throws Exception
    {
        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              testJpgImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        // Verify output file was created
        assertTrue("Output PDF should be created", outputPdf.exists());
        assertTrue("Output PDF should have content", outputPdf.length() > 0);

        // Verify the PDF can be loaded
        PDDocument doc = PDDocument.load(outputPdf);
        assertNotNull("Document should be loaded", doc);
        assertEquals("Document should have 1 page", 1, doc.getNumberOfPages());
        doc.close();
    }

    /**
     * Test adding a PNG image to PDF.
     */
    public void testAddPngImageToPDF() throws Exception
    {
        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              testPngImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        // Verify output file was created
        assertTrue("Output PDF should be created", outputPdf.exists());
        assertTrue("Output PDF should have content", outputPdf.length() > 0);

        // Verify the PDF can be loaded
        PDDocument doc = PDDocument.load(outputPdf);
        assertNotNull("Document should be loaded", doc);
        assertEquals("Document should have 1 page", 1, doc.getNumberOfPages());
        doc.close();
    }

    /**
     * Test main method with valid arguments.
     */
    public void testMainWithValidArguments() throws Exception
    {
        String[] args = new String[] {
            inputPdf.getAbsolutePath(),
            testJpgImage.getAbsolutePath(),
            outputPdf.getAbsolutePath()
        };

        AddImageToPDF.main(args);

        assertTrue("Output PDF should be created via main", outputPdf.exists());
        assertTrue("Output PDF should have content", outputPdf.length() > 0);
    }

    /**
     * Test main method with incorrect number of arguments.
     */
    public void testMainWithIncorrectArguments() throws Exception
    {
        String[] args = new String[] { inputPdf.getAbsolutePath() };

        // Should not throw exception, just print usage
        AddImageToPDF.main(args);
    }

    /**
     * Test with non-existent input PDF.
     */
    public void testWithNonExistentInputPDF()
    {
        File nonExistent = new File(outDir, "nonexistent.pdf");

        AddImageToPDF app = new AddImageToPDF();
        try
        {
            app.createPDFFromImage(nonExistent.getAbsolutePath(),
                                  testJpgImage.getAbsolutePath(),
                                  outputPdf.getAbsolutePath());
            fail("Should throw exception for non-existent input PDF");
        }
        catch (Exception e)
        {
            // Expected
        }
    }

    /**
     * Test with non-existent image file.
     */
    public void testWithNonExistentImage()
    {
        File nonExistent = new File(outDir, "nonexistent.jpg");

        AddImageToPDF app = new AddImageToPDF();
        try
        {
            app.createPDFFromImage(inputPdf.getAbsolutePath(),
                                  nonExistent.getAbsolutePath(),
                                  outputPdf.getAbsolutePath());
            fail("Should throw exception for non-existent image");
        }
        catch (Exception e)
        {
            // Expected
        }
    }

    /**
     * Test with uppercase JPG extension.
     */
    public void testWithUppercaseJPG() throws Exception
    {
        File uppercaseJpg = new File(outDir, "test.JPG");
        createTestImage(uppercaseJpg, "jpg");

        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              uppercaseJpg.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        assertTrue("Should handle uppercase .JPG extension", outputPdf.exists());

        // Cleanup
        uppercaseJpg.delete();
    }

    /**
     * Test adding image to multi-page PDF.
     */
    public void testAddImageToMultiPagePDF() throws Exception
    {
        File multiPagePdf = new File(outDir, "multipage.pdf");

        // Create multi-page PDF
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        doc.save(multiPagePdf);
        doc.close();

        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(multiPagePdf.getAbsolutePath(),
                              testJpgImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        // Verify image is added to first page
        PDDocument resultDoc = PDDocument.load(outputPdf);
        assertEquals("Should maintain page count", 3, resultDoc.getNumberOfPages());
        resultDoc.close();

        // Cleanup
        multiPagePdf.delete();
    }

    /**
     * Test that output file is different from input.
     */
    public void testOutputDifferentFromInput() throws Exception
    {
        long inputSize = inputPdf.length();

        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              testJpgImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        long outputSize = outputPdf.length();
        assertTrue("Output PDF should be larger than input (contains image)",
                  outputSize > inputSize);
    }

    /**
     * Test with very small image.
     */
    public void testWithSmallImage() throws Exception
    {
        File smallImage = new File(outDir, "small.png");
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        ImageIO.write(image, "png", smallImage);

        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              smallImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        assertTrue("Should handle small images", outputPdf.exists());

        // Cleanup
        smallImage.delete();
    }

    /**
     * Test with large image.
     */
    public void testWithLargeImage() throws Exception
    {
        File largeImage = new File(outDir, "large.png");
        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 1000, 1000);
        g.dispose();
        ImageIO.write(image, "png", largeImage);

        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              largeImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        assertTrue("Should handle large images", outputPdf.exists());

        // Cleanup
        largeImage.delete();
    }

    /**
     * Test with various image formats handled by default (non-JPG/TIF).
     */
    public void testWithBMPImage() throws Exception
    {
        File bmpImage = new File(outDir, "test.bmp");
        createTestImage(bmpImage, "bmp");

        AddImageToPDF app = new AddImageToPDF();
        app.createPDFFromImage(inputPdf.getAbsolutePath(),
                              bmpImage.getAbsolutePath(),
                              outputPdf.getAbsolutePath());

        assertTrue("Should handle BMP images", outputPdf.exists());

        // Cleanup
        bmpImage.delete();
    }
}