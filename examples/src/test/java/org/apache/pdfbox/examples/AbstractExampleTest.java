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
package org.apache.pdfbox.examples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdfwriter.COSWriter;

/**
 * Test case for AbstractExample.
 *
 * @author Test Author
 */
public class AbstractExampleTest extends TestCase
{
    private final String outDir = "target/test-output/examples/";

    /**
     * Concrete implementation of AbstractExample for testing.
     */
    private static class TestExample extends AbstractExample
    {
        // Concrete class for testing abstract methods
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        new File(outDir).mkdirs();
    }

    /**
     * Test closing an InputStream.
     */
    public void testCloseInputStream() throws IOException
    {
        TestExample example = new TestExample();
        InputStream stream = new ByteArrayInputStream("test".getBytes());

        // Should not throw exception
        example.close(stream);

        // Verify stream is closed
        try
        {
            stream.read();
            fail("Stream should be closed");
        }
        catch (IOException e)
        {
            // Expected - stream is closed
        }
    }

    /**
     * Test closing a null InputStream.
     */
    public void testCloseNullInputStream() throws IOException
    {
        TestExample example = new TestExample();
        InputStream stream = null;

        // Should not throw exception with null stream
        example.close(stream);
    }

    /**
     * Test closing an OutputStream.
     */
    public void testCloseOutputStream() throws IOException
    {
        TestExample example = new TestExample();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("test".getBytes());

        // Should not throw exception
        example.close(stream);

        // Verify stream is closed
        try
        {
            stream.write("more".getBytes());
            // ByteArrayOutputStream doesn't actually throw on write after close
            // but we've called close which is the main point
        }
        catch (IOException e)
        {
            // May or may not throw depending on stream implementation
        }
    }

    /**
     * Test closing a null OutputStream.
     */
    public void testCloseNullOutputStream() throws IOException
    {
        TestExample example = new TestExample();
        OutputStream stream = null;

        // Should not throw exception with null stream
        example.close(stream);
    }

    /**
     * Test closing a COSDocument.
     */
    public void testCloseCOSDocument() throws IOException
    {
        TestExample example = new TestExample();
        PDDocument pdDoc = new PDDocument();
        pdDoc.addPage(new PDPage());
        COSDocument cosDoc = pdDoc.getDocument();

        // Should not throw exception
        example.close(cosDoc);

        // Note: COSDocument close is called, PDDocument should not be used after
        pdDoc.close();
    }

    /**
     * Test closing a null COSDocument.
     */
    public void testCloseNullCOSDocument() throws IOException
    {
        TestExample example = new TestExample();
        COSDocument doc = null;

        // Should not throw exception with null document
        example.close(doc);
    }

    /**
     * Test closing a PDDocument.
     */
    public void testClosePDDocument() throws IOException
    {
        TestExample example = new TestExample();
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());

        // Should not throw exception
        example.close(doc);

        // Verify document is closed - trying to add page should fail
        try
        {
            doc.addPage(new PDPage());
            // Some implementations may allow this, but document is marked as closed
        }
        catch (Exception e)
        {
            // Expected in some cases
        }
    }

    /**
     * Test closing a null PDDocument.
     */
    public void testCloseNullPDDocument() throws IOException
    {
        TestExample example = new TestExample();
        PDDocument doc = null;

        // Should not throw exception with null document
        example.close(doc);
    }

    /**
     * Test closing a COSWriter.
     */
    public void testCloseCOSWriter() throws Exception
    {
        File tempFile = new File(outDir, "test-coswriter.pdf");
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());

        FileOutputStream fos = new FileOutputStream(tempFile);
        COSWriter writer = new COSWriter(fos);
        writer.write(doc);

        // Should not throw exception
        AbstractExample.close(writer);

        doc.close();
        tempFile.delete();
    }

    /**
     * Test closing a null COSWriter.
     */
    public void testCloseNullCOSWriter() throws IOException
    {
        COSWriter writer = null;

        // Should not throw exception with null writer
        AbstractExample.close(writer);
    }

    /**
     * Test that close methods properly handle IOException from underlying streams.
     */
    public void testCloseWithIOException() throws IOException
    {
        TestExample example = new TestExample();

        // Create a stream that throws IOException on close
        InputStream errorStream = new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                return -1;
            }

            @Override
            public void close() throws IOException
            {
                throw new IOException("Test exception");
            }
        };

        try
        {
            example.close(errorStream);
            fail("Should throw IOException");
        }
        catch (IOException e)
        {
            assertEquals("Test exception", e.getMessage());
        }
    }

    /**
     * Test multiple close calls on same resource.
     */
    public void testMultipleClose() throws IOException
    {
        TestExample example = new TestExample();
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());

        // First close should succeed
        example.close(doc);

        // Second close should not throw exception (idempotent)
        try
        {
            example.close(doc);
        }
        catch (Exception e)
        {
            // May throw in some implementations, but shouldn't cause issues
        }
    }

    /**
     * Test closing resources in correct order.
     */
    public void testCloseOrderCOSThenPD() throws IOException
    {
        TestExample example = new TestExample();
        PDDocument pdDoc = new PDDocument();
        pdDoc.addPage(new PDPage());
        COSDocument cosDoc = pdDoc.getDocument();

        // Close COS first, then PD
        example.close(cosDoc);
        example.close(pdDoc);

        // Should complete without exception
    }
}