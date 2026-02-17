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
package org.apache.pdfbox.pdmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;

/**
 * Test resource leak fixes in PDDocument.load() methods.
 *
 * @author Apache PDFBox Team
 */
public class TestPDDocumentResourceLeak extends TestCase
{
    private File corruptPdfFile;
    private File validPdfFile;

    protected void setUp() throws Exception
    {
        super.setUp();

        // Create a corrupt PDF file that will cause parsing to fail
        corruptPdfFile = File.createTempFile("corrupt", ".pdf");
        FileOutputStream fos = new FileOutputStream(corruptPdfFile);
        try
        {
            fos.write("This is not a valid PDF file content".getBytes());
        }
        finally
        {
            fos.close();
        }

        // Create a minimal valid PDF file for regression testing
        validPdfFile = File.createTempFile("valid", ".pdf");
        fos = new FileOutputStream(validPdfFile);
        try
        {
            // Minimal valid PDF 1.4 structure
            String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [] /Count 0 >>\n" +
                "endobj\n" +
                "xref\n" +
                "0 3\n" +
                "0000000000 65535 f\n" +
                "0000000009 00000 n\n" +
                "0000000058 00000 n\n" +
                "trailer\n" +
                "<< /Size 3 /Root 1 0 R >>\n" +
                "startxref\n" +
                "110\n" +
                "%%EOF\n";
            fos.write(pdfContent.getBytes());
        }
        finally
        {
            fos.close();
        }
    }

    protected void tearDown() throws Exception
    {
        if (corruptPdfFile != null && corruptPdfFile.exists())
        {
            corruptPdfFile.delete();
        }
        if (validPdfFile != null && validPdfFile.exists())
        {
            validPdfFile.delete();
        }
        super.tearDown();
    }

    /**
     * Test that load(String) does not leak file handles when parsing fails.
     * This test verifies that the file can be deleted after IOException,
     * which would fail on Windows if a file handle was leaked.
     */
    public void testLoadFileStringThrowsIOExceptionDoesNotLeakFileHandle()
    {
        try
        {
            PDDocument.load(corruptPdfFile.getAbsolutePath());
            fail("Should have thrown IOException for corrupt PDF");
        }
        catch (IOException e)
        {
            // Expected - parsing should fail for corrupt file
        }

        // Verify file can be deleted (would fail if handle was leaked)
        assertTrue("File should be deletable - no leaked file handle",
                   corruptPdfFile.delete());

        // Recreate for other tests
        try
        {
            corruptPdfFile = File.createTempFile("corrupt", ".pdf");
            FileOutputStream fos = new FileOutputStream(corruptPdfFile);
            try
            {
                fos.write("This is not a valid PDF file content".getBytes());
            }
            finally
            {
                fos.close();
            }
        }
        catch (IOException e)
        {
            fail("Failed to recreate corrupt file: " + e.getMessage());
        }
    }

    /**
     * Test that load(String, boolean) does not leak file handles when parsing fails.
     */
    public void testLoadFileStringWithForceThrowsIOExceptionDoesNotLeakFileHandle()
    {
        try
        {
            PDDocument.load(corruptPdfFile.getAbsolutePath(), false);
            fail("Should have thrown IOException for corrupt PDF");
        }
        catch (IOException e)
        {
            // Expected - parsing should fail for corrupt file
        }

        // Verify file can be deleted (would fail if handle was leaked)
        assertTrue("File should be deletable - no leaked file handle",
                   corruptPdfFile.delete());

        // Recreate for other tests
        try
        {
            corruptPdfFile = File.createTempFile("corrupt", ".pdf");
            FileOutputStream fos = new FileOutputStream(corruptPdfFile);
            try
            {
                fos.write("This is not a valid PDF file content".getBytes());
            }
            finally
            {
                fos.close();
            }
        }
        catch (IOException e)
        {
            fail("Failed to recreate corrupt file: " + e.getMessage());
        }
    }

    /**
     * Test that load(File) does not leak file handles when parsing fails.
     */
    public void testLoadFileObjectThrowsIOExceptionDoesNotLeakFileHandle()
    {
        try
        {
            PDDocument.load(corruptPdfFile);
            fail("Should have thrown IOException for corrupt PDF");
        }
        catch (IOException e)
        {
            // Expected - parsing should fail for corrupt file
        }

        // Verify file can be deleted (would fail if handle was leaked)
        assertTrue("File should be deletable - no leaked file handle",
                   corruptPdfFile.delete());

        // Recreate for other tests
        try
        {
            corruptPdfFile = File.createTempFile("corrupt", ".pdf");
            FileOutputStream fos = new FileOutputStream(corruptPdfFile);
            try
            {
                fos.write("This is not a valid PDF file content".getBytes());
            }
            finally
            {
                fos.close();
            }
        }
        catch (IOException e)
        {
            fail("Failed to recreate corrupt file: " + e.getMessage());
        }
    }

    /**
     * Regression test: verify that valid PDF files can still be loaded successfully.
     */
    public void testLoadValidFileDoesNotLeak()
    {
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load(validPdfFile.getAbsolutePath());
            assertNotNull("Should successfully load valid PDF", doc);
        }
        catch (IOException e)
        {
            fail("Should not throw exception for valid PDF: " + e.getMessage());
        }
        finally
        {
            if (doc != null)
            {
                try
                {
                    doc.close();
                }
                catch (IOException e)
                {
                    // Ignore close exception in test
                }
            }
        }
    }

    /**
     * Test that load(File, RandomAccess) does not leak file handles
     * and properly uses the scratchFile parameter (bug fix test).
     */
    public void testLoadFileWithScratchFileDoesNotLeak()
    {
        File scratchFile = null;
        RandomAccess randomAccess = null;
        try
        {
            scratchFile = File.createTempFile("scratch", ".tmp");
            randomAccess = new RandomAccessFile(scratchFile, "rw");

            try
            {
                PDDocument.load(corruptPdfFile, randomAccess);
                fail("Should have thrown IOException for corrupt PDF");
            }
            catch (IOException e)
            {
                // Expected - parsing should fail for corrupt file
            }

            // Verify file can be deleted (would fail if handle was leaked)
            assertTrue("File should be deletable - no leaked file handle",
                       corruptPdfFile.delete());

            // Recreate for other tests
            corruptPdfFile = File.createTempFile("corrupt", ".pdf");
            FileOutputStream fos = new FileOutputStream(corruptPdfFile);
            try
            {
                fos.write("This is not a valid PDF file content".getBytes());
            }
            finally
            {
                fos.close();
            }
        }
        catch (IOException e)
        {
            fail("Test setup failed: " + e.getMessage());
        }
        finally
        {
            if (randomAccess != null)
            {
                try
                {
                    randomAccess.close();
                }
                catch (IOException e)
                {
                    // Ignore
                }
            }
            if (scratchFile != null && scratchFile.exists())
            {
                scratchFile.delete();
            }
        }
    }
}
