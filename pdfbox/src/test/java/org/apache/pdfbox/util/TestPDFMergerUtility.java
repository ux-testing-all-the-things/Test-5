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
package org.apache.pdfbox.util;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Test suite for PDFMergerUtility.
 *
 * @version $Revision: 1.0 $
 */
public class TestPDFMergerUtility extends TestCase
{
    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     */
    public TestPDFMergerUtility( String name )
    {
        super( name );
    }

    /**
     * Test merging two PDF documents.
     * This verifies the loop optimization doesn't break merging.
     *
     * @throws Exception when there is an exception
     */
    public void testMergeDocuments() throws Exception
    {
        File file1 = new File("src/test/resources/input/cweb.pdf");
        File file2 = new File("src/test/resources/input/cweb.pdf");

        // Skip test if files don't exist
        if (!file1.exists())
        {
            return;
        }

        PDDocument doc1 = null;
        PDDocument doc2 = null;
        PDDocument merged = null;

        try
        {
            doc1 = PDDocument.load(file1);
            doc2 = PDDocument.load(file2);

            int doc1Pages = doc1.getNumberOfPages();
            int doc2Pages = doc2.getNumberOfPages();

            // Create merger
            PDFMergerUtility merger = new PDFMergerUtility();
            merged = new PDDocument();

            // Merge documents
            merger.appendDocument(merged, doc1);
            merger.appendDocument(merged, doc2);

            // Verify page count
            int mergedPages = merged.getNumberOfPages();
            assertEquals("Merged document should have correct page count",
                        doc1Pages + doc2Pages, mergedPages);
        }
        finally
        {
            if (doc1 != null) doc1.close();
            if (doc2 != null) doc2.close();
            if (merged != null) merged.close();
        }
    }

    /**
     * Test merging documents with complex structure.
     * This exercises the cloneForNewDocument recursive method.
     *
     * @throws Exception when there is an exception
     */
    public void testMergeComplexDocuments() throws Exception
    {
        // Use a PDF that likely has more complex structure
        File file1 = new File("src/test/resources/input/cweb.pdf");

        if (!file1.exists())
        {
            return;
        }

        PDDocument doc1 = null;
        PDDocument doc2 = null;
        PDDocument merged = null;

        try
        {
            doc1 = PDDocument.load(file1);
            doc2 = PDDocument.load(file1); // Same file twice

            int originalPages = doc1.getNumberOfPages();

            // Create merger
            PDFMergerUtility merger = new PDFMergerUtility();
            merged = new PDDocument();

            // Merge same document twice
            merger.appendDocument(merged, doc1);
            merger.appendDocument(merged, doc2);

            // Verify
            assertEquals("Merged document should have double pages",
                        originalPages * 2, merged.getNumberOfPages());

            // Verify we can extract text (tests deep cloning worked)
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(merged);
            assertNotNull("Should be able to extract text", text);
            assertTrue("Text should not be empty", text.length() > 0);
        }
        finally
        {
            if (doc1 != null) doc1.close();
            if (doc2 != null) doc2.close();
            if (merged != null) merged.close();
        }
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite( TestPDFMergerUtility.class );
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestPDFMergerUtility.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
