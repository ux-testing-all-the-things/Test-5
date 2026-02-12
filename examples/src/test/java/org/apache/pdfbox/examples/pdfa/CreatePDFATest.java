/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.pdfa;

import junit.framework.TestCase;

import java.io.File;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;

/**
 *
 * @author Tilman Hausherr
 */
public class CreatePDFATest extends TestCase
{
    private final String outDir = "target/test-output/";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        new File(outDir).mkdirs();
    }

    /**
     * Test of doIt method of class CreatePDFA.
     */
    public void testCreatePDFA() throws Exception
    {
        System.out.println("testCreatePDFA");
        String pdfaFilename = outDir + "/PDFA.pdf";
        String message = "The quick brown fox jumps over the lazy dog ������� @�^�� {[]}";
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        PreflightParser preflightParser = new PreflightParser(new File(pdfaFilename));
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        for (ValidationError ve : result.getErrorsList())
        {
            System.err.println(ve.getErrorCode() + ": " + ve.getDetails());
        }
        assertTrue("PDF file created with CreatePDFA is not valid PDF/A-1b", result.isValid());
        preflightDocument.close();
        preflightParser.clearResources();
    }

    /**
     * Test creating PDF/A with simple ASCII message.
     */
    public void testCreatePDFAWithSimpleMessage() throws Exception
    {
        System.out.println("testCreatePDFAWithSimpleMessage");
        String pdfaFilename = outDir + "/PDFA-simple.pdf";
        String message = "Simple test message";
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file should be created", pdfFile.exists());
        assertTrue("PDF file should have content", pdfFile.length() > 0);

        PreflightParser preflightParser = new PreflightParser(pdfFile);
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        assertTrue("Simple PDF/A should be valid", result.isValid());
        preflightDocument.close();
        preflightParser.clearResources();
    }

    /**
     * Test creating PDF/A with empty message.
     */
    public void testCreatePDFAWithEmptyMessage() throws Exception
    {
        System.out.println("testCreatePDFAWithEmptyMessage");
        String pdfaFilename = outDir + "/PDFA-empty.pdf";
        String message = "";
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file should be created with empty message", pdfFile.exists());

        PreflightParser preflightParser = new PreflightParser(pdfFile);
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        assertTrue("Empty message PDF/A should be valid", result.isValid());
        preflightDocument.close();
        preflightParser.clearResources();
    }

    /**
     * Test creating PDF/A with long message.
     */
    public void testCreatePDFAWithLongMessage() throws Exception
    {
        System.out.println("testCreatePDFAWithLongMessage");
        String pdfaFilename = outDir + "/PDFA-long.pdf";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++)
        {
            sb.append("This is a long message repeated multiple times. ");
        }
        String message = sb.toString();
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file should be created with long message", pdfFile.exists());

        PreflightParser preflightParser = new PreflightParser(pdfFile);
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        assertTrue("Long message PDF/A should be valid", result.isValid());
        preflightDocument.close();
        preflightParser.clearResources();
    }

    /**
     * Test that main method works with correct arguments.
     */
    public void testMain() throws Exception
    {
        System.out.println("testMain");
        String pdfaFilename = outDir + "/PDFA-main.pdf";
        String message = "Test via main method";
        String[] args = new String[] { pdfaFilename, message };

        CreatePDFA.main(args);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file should be created via main", pdfFile.exists());
    }

    /**
     * Test constructor instantiation.
     */
    public void testConstructor()
    {
        CreatePDFA instance = new CreatePDFA();
        assertNotNull("CreatePDFA instance should be created", instance);
    }

    /**
     * Test creating PDF/A with special characters.
     */
    public void testCreatePDFAWithSpecialCharacters() throws Exception
    {
        System.out.println("testCreatePDFAWithSpecialCharacters");
        String pdfaFilename = outDir + "/PDFA-special.pdf";
        String message = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file should handle special characters", pdfFile.exists());

        PreflightParser preflightParser = new PreflightParser(pdfFile);
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        assertTrue("Special chars PDF/A should be valid", result.isValid());
        preflightDocument.close();
        preflightParser.clearResources();
    }

    /**
     * Test creating PDF/A with numbers and mixed content.
     */
    public void testCreatePDFAWithMixedContent() throws Exception
    {
        System.out.println("testCreatePDFAWithMixedContent");
        String pdfaFilename = outDir + "/PDFA-mixed.pdf";
        String message = "Mixed: 123 ABC xyz !@# 456";
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file should be created with mixed content", pdfFile.exists());

        PreflightParser preflightParser = new PreflightParser(pdfFile);
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        assertTrue("Mixed content PDF/A should be valid", result.isValid());
        preflightDocument.close();
        preflightParser.clearResources();
    }

    /**
     * Test that output file size is reasonable.
     */
    public void testOutputFileSize() throws Exception
    {
        System.out.println("testOutputFileSize");
        String pdfaFilename = outDir + "/PDFA-size.pdf";
        String message = "Test file size";
        CreatePDFA instance = new CreatePDFA();
        instance.doIt(pdfaFilename, message);

        File pdfFile = new File(pdfaFilename);
        assertTrue("PDF file size should be at least 10KB", pdfFile.length() > 10000);
    }

}