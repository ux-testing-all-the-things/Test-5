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
package org.apache.pdfbox.examples.documentmanipulation;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Test case for SuperimposePage.
 *
 * @author Test Author
 */
public class SuperimposePageTest extends TestCase
{
    private final String outDir = "target/test-output/examples/documentmanipulation/";
    private File sourcePdf;
    private File outputPdf;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        File dir = new File(outDir);
        dir.mkdirs();

        // Create a source PDF for testing
        sourcePdf = new File(outDir, "source.pdf");
        createSourcePDF(sourcePdf);

        outputPdf = new File(outDir, "superimposed.pdf");
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        if (sourcePdf != null && sourcePdf.exists())
        {
            sourcePdf.delete();
        }
        if (outputPdf != null && outputPdf.exists())
        {
            outputPdf.delete();
        }
    }

    /**
     * Create a simple source PDF for testing.
     */
    private void createSourcePDF(File file) throws Exception
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
            contentStream.drawString("Source PDF content");
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
     * Test the main method creates a superimposed PDF.
     */
    public void testSuperimposePage() throws Exception
    {
        String[] args = new String[] { sourcePdf.getAbsolutePath(), outputPdf.getAbsolutePath() };

        SuperimposePage.main(args);

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
     * Test that superimposed PDF is larger than basic PDF.
     */
    public void testSuperimposedPDFSize() throws Exception
    {
        String[] args = new String[] { sourcePdf.getAbsolutePath(), outputPdf.getAbsolutePath() };

        SuperimposePage.main(args);

        assertTrue("Output PDF should exist", outputPdf.exists());
        assertTrue("Output PDF should have substantial content", outputPdf.length() > 1000);
    }

    /**
     * Test with non-existent source file.
     */
    public void testNonExistentSourceFile()
    {
        File nonExistent = new File(outDir, "nonexistent.pdf");
        String[] args = new String[] { nonExistent.getAbsolutePath(), outputPdf.getAbsolutePath() };

        try
        {
            SuperimposePage.main(args);
            // May fail or create empty output depending on implementation
        }
        catch (Exception e)
        {
            // Expected - source file doesn't exist
        }
    }

    /**
     * Test that output file is created in correct location.
     */
    public void testOutputFileLocation() throws Exception
    {
        String[] args = new String[] { sourcePdf.getAbsolutePath(), outputPdf.getAbsolutePath() };

        SuperimposePage.main(args);

        assertEquals("Output file should be in correct location",
                     outDir + "superimposed.pdf",
                     outputPdf.getAbsolutePath());
        assertTrue("Output file should exist at specified location", outputPdf.exists());
    }

    /**
     * Test with empty source PDF.
     */
    public void testEmptySourcePDF() throws Exception
    {
        File emptyPdf = new File(outDir, "empty.pdf");
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.save(emptyPdf);
        doc.close();

        File output = new File(outDir, "superimposed-empty.pdf");
        String[] args = new String[] { emptyPdf.getAbsolutePath(), output.getAbsolutePath() };

        SuperimposePage.main(args);

        assertTrue("Output should be created even with empty source", output.exists());

        // Cleanup
        emptyPdf.delete();
        output.delete();
    }

    /**
     * Test with multi-page source PDF.
     */
    public void testMultiPageSourcePDF() throws Exception
    {
        File multiPagePdf = new File(outDir, "multipage.pdf");
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        doc.save(multiPagePdf);
        doc.close();

        File output = new File(outDir, "superimposed-multipage.pdf");
        String[] args = new String[] { multiPagePdf.getAbsolutePath(), output.getAbsolutePath() };

        SuperimposePage.main(args);

        assertTrue("Output should be created with multi-page source", output.exists());

        PDDocument resultDoc = PDDocument.load(output);
        assertEquals("Output should have 1 page", 1, resultDoc.getNumberOfPages());
        resultDoc.close();

        // Cleanup
        multiPagePdf.delete();
        output.delete();
    }

    /**
     * Test that the example handles exceptions gracefully.
     */
    public void testExceptionHandling()
    {
        // Test with invalid arguments
        String[] args = new String[] { "", "" };

        try
        {
            SuperimposePage.main(args);
            // Should handle error gracefully
        }
        catch (Exception e)
        {
            // Expected to fail with invalid paths
        }
    }

    /**
     * Test output PDF structure.
     */
    public void testOutputPDFStructure() throws Exception
    {
        String[] args = new String[] { sourcePdf.getAbsolutePath(), outputPdf.getAbsolutePath() };

        SuperimposePage.main(args);

        // Load and verify structure
        PDDocument doc = PDDocument.load(outputPdf);
        assertNotNull("Document should load successfully", doc);

        PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
        assertNotNull("Page should exist", page);
        assertNotNull("Page should have content", page.getContents());

        doc.close();
    }
}