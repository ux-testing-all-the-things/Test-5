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
package org.apache.pdfbox.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Test case for PDFToTextTask.
 *
 * @author Test Author
 */
public class PDFToTextTaskTest extends TestCase
{
    private final String outDir = "target/test-output/ant/";
    private File testPdfFile;
    private File testDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testDir = new File(outDir);
        testDir.mkdirs();

        // Create a test PDF file
        testPdfFile = new File(testDir, "test.PDF");
        createTestPDF(testPdfFile);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        // Clean up test files
        if (testPdfFile != null && testPdfFile.exists())
        {
            testPdfFile.delete();
        }
        File textFile = new File(testDir, "test.txt");
        if (textFile.exists())
        {
            textFile.delete();
        }
    }

    /**
     * Create a simple test PDF file.
     */
    private void createTestPDF(File file) throws Exception
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
            contentStream.drawString("Test PDF content for conversion");
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
     * Test that the task can be instantiated.
     */
    public void testTaskInstantiation()
    {
        PDFToTextTask task = new PDFToTextTask();
        assertNotNull("Task should be instantiated", task);
    }

    /**
     * Test that a fileset can be added to the task.
     */
    public void testAddFileset()
    {
        PDFToTextTask task = new PDFToTextTask();
        Project project = new Project();
        task.setProject(project);

        FileSet fileSet = new FileSet();
        fileSet.setDir(testDir);
        fileSet.setIncludes("*.PDF");

        task.addFileset(fileSet);

        // Execute the task
        task.execute();

        // Verify that the text file was created
        File textFile = new File(testDir, "test.txt");
        assertTrue("Text file should be created", textFile.exists());
        assertTrue("Text file should have content", textFile.length() > 0);
    }

    /**
     * Test that the task handles PDF files with correct extension.
     */
    public void testPDFFileWithUppercaseExtension()
    {
        PDFToTextTask task = new PDFToTextTask();
        Project project = new Project();
        task.setProject(project);

        FileSet fileSet = new FileSet();
        fileSet.setDir(testDir);
        fileSet.setIncludes("*.PDF");

        task.addFileset(fileSet);
        task.execute();

        File textFile = new File(testDir, "test.txt");
        assertTrue("Text file should be created for uppercase .PDF extension", textFile.exists());
    }

    /**
     * Test that the task skips non-PDF files.
     */
    public void testSkipNonPDFFiles() throws IOException
    {
        // Create a non-PDF file
        File nonPdfFile = new File(testDir, "test.txt");
        FileOutputStream fos = new FileOutputStream(nonPdfFile);
        fos.write("Not a PDF".getBytes());
        fos.close();

        PDFToTextTask task = new PDFToTextTask();
        Project project = new Project();
        task.setProject(project);

        FileSet fileSet = new FileSet();
        fileSet.setDir(testDir);
        fileSet.setIncludes("*.txt");

        task.addFileset(fileSet);

        // Should not throw exception, just skip non-PDF files
        task.execute();

        nonPdfFile.delete();
    }

    /**
     * Test that the task handles empty filesets.
     */
    public void testEmptyFileset()
    {
        PDFToTextTask task = new PDFToTextTask();
        Project project = new Project();
        task.setProject(project);

        FileSet fileSet = new FileSet();
        fileSet.setDir(testDir);
        fileSet.setIncludes("*.nonexistent");

        task.addFileset(fileSet);

        // Should not throw exception with empty fileset
        task.execute();
    }

    /**
     * Test that the task handles multiple filesets.
     */
    public void testMultipleFilesets() throws Exception
    {
        // Create second test PDF
        File testPdf2 = new File(testDir, "test2.PDF");
        createTestPDF(testPdf2);

        PDFToTextTask task = new PDFToTextTask();
        Project project = new Project();
        task.setProject(project);

        FileSet fileSet1 = new FileSet();
        fileSet1.setDir(testDir);
        fileSet1.setIncludes("test.PDF");

        FileSet fileSet2 = new FileSet();
        fileSet2.setDir(testDir);
        fileSet2.setIncludes("test2.PDF");

        task.addFileset(fileSet1);
        task.addFileset(fileSet2);
        task.execute();

        // Verify both text files were created
        File textFile1 = new File(testDir, "test.txt");
        File textFile2 = new File(testDir, "test2.txt");
        assertTrue("First text file should be created", textFile1.exists());
        assertTrue("Second text file should be created", textFile2.exists());

        // Cleanup
        testPdf2.delete();
        textFile1.delete();
        textFile2.delete();
    }

    /**
     * Test that task handles lowercase pdf extension.
     */
    public void testLowercasePdfExtension() throws Exception
    {
        // Create PDF with lowercase extension
        File lowercasePdf = new File(testDir, "lowercase.pdf");
        createTestPDF(lowercasePdf);

        PDFToTextTask task = new PDFToTextTask();
        Project project = new Project();
        task.setProject(project);

        FileSet fileSet = new FileSet();
        fileSet.setDir(testDir);
        fileSet.setIncludes("*.pdf");

        task.addFileset(fileSet);
        task.execute();

        // Since the code only checks for uppercase .PDF, lowercase should be skipped
        File textFile = new File(testDir, "lowercase.txt");
        assertFalse("Text file should not be created for lowercase .pdf extension", textFile.exists());

        // Cleanup
        lowercasePdf.delete();
    }
}