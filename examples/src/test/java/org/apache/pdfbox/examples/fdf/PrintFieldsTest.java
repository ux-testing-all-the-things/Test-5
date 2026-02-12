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
package org.apache.pdfbox.examples.fdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * Test case for PrintFields.
 *
 * @author Test Author
 */
public class PrintFieldsTest extends TestCase
{
    private final String outDir = "target/test-output/examples/fdf/";
    private File testPdfWithFields;
    private PrintStream originalOut;
    private ByteArrayOutputStream testOut;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        File dir = new File(outDir);
        dir.mkdirs();

        // Create a test PDF with form fields
        testPdfWithFields = new File(outDir, "formtest.pdf");
        createPDFWithFields(testPdfWithFields);

        // Capture System.out for testing
        originalOut = System.out;
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        // Restore System.out
        System.setOut(originalOut);

        if (testPdfWithFields != null && testPdfWithFields.exists())
        {
            testPdfWithFields.delete();
        }
    }

    /**
     * Create a PDF with form fields for testing.
     */
    private void createPDFWithFields(File file) throws Exception
    {
        PDDocument document = new PDDocument();
        try
        {
            PDPage page = new PDPage();
            document.addPage(page);

            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Add text field
            PDTextField textField = new PDTextField(acroForm);
            textField.setPartialName("testField");
            textField.setValue("Test Value");
            acroForm.getFields().add(textField);

            // Add another text field
            PDTextField textField2 = new PDTextField(acroForm);
            textField2.setPartialName("testField2");
            textField2.setValue("Another Value");
            acroForm.getFields().add(textField2);

            document.save(file);
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Test printFields method with a document containing fields.
     */
    public void testPrintFields() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        PrintFields printer = new PrintFields();
        printer.printFields(document);

        document.close();

        String output = testOut.toString();
        assertTrue("Output should contain field count", output.contains("2 top-level fields"));
        assertTrue("Output should contain field name", output.contains("testField"));
    }

    /**
     * Test main method with valid PDF.
     */
    public void testMainWithValidPDF() throws Exception
    {
        String[] args = new String[] { testPdfWithFields.getAbsolutePath() };

        PrintFields.main(args);

        String output = testOut.toString();
        assertTrue("Output should show fields were processed", output.contains("top-level fields"));
    }

    /**
     * Test main method with no arguments.
     */
    public void testMainWithNoArguments() throws Exception
    {
        String[] args = new String[] {};

        // Capture System.err
        PrintStream originalErr = System.err;
        ByteArrayOutputStream testErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(testErr));

        try
        {
            PrintFields.main(args);

            String errOutput = testErr.toString();
            assertTrue("Should print usage message", errOutput.contains("usage:"));
        }
        finally
        {
            System.setErr(originalErr);
        }
    }

    /**
     * Test main method with too many arguments.
     */
    public void testMainWithTooManyArguments() throws Exception
    {
        String[] args = new String[] { "file1.pdf", "file2.pdf" };

        // Capture System.err
        PrintStream originalErr = System.err;
        ByteArrayOutputStream testErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(testErr));

        try
        {
            PrintFields.main(args);

            String errOutput = testErr.toString();
            assertTrue("Should print usage message", errOutput.contains("usage:"));
        }
        finally
        {
            System.setErr(originalErr);
        }
    }

    /**
     * Test with encrypted PDF.
     */
    public void testEncryptedPDF() throws Exception
    {
        File encryptedPdf = new File(outDir, "encrypted.pdf");

        // Create encrypted PDF
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);

        PDTextField field = new PDTextField(acroForm);
        field.setPartialName("encryptedField");
        field.setValue("Encrypted Value");
        acroForm.getFields().add(field);

        doc.encrypt("", "", null);
        doc.save(encryptedPdf);
        doc.close();

        String[] args = new String[] { encryptedPdf.getAbsolutePath() };

        PrintFields.main(args);

        String output = testOut.toString();
        assertTrue("Should handle encrypted PDF", output.length() > 0);

        // Cleanup
        encryptedPdf.delete();
    }

    /**
     * Test with PDF containing nested fields.
     */
    public void testNestedFields() throws Exception
    {
        File nestedPdf = new File(outDir, "nested.pdf");

        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());

        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);

        // Create parent field
        PDTextField parentField = new PDTextField(acroForm);
        parentField.setPartialName("parent");
        acroForm.getFields().add(parentField);

        // Create child field
        PDTextField childField = new PDTextField(acroForm);
        childField.setPartialName("child");
        childField.setValue("Child Value");
        parentField.getKids().add(childField);

        doc.save(nestedPdf);
        doc.close();

        PrintFields printer = new PrintFields();
        PDDocument loadedDoc = PDDocument.load(nestedPdf);
        printer.printFields(loadedDoc);
        loadedDoc.close();

        String output = testOut.toString();
        assertTrue("Should process nested fields", output.contains("parent"));

        // Cleanup
        nestedPdf.delete();
    }

    /**
     * Test with PDF containing no fields.
     */
    public void testPDFWithNoFields() throws Exception
    {
        File noFieldsPdf = new File(outDir, "nofields.pdf");

        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);
        doc.save(noFieldsPdf);
        doc.close();

        PrintFields printer = new PrintFields();
        PDDocument loadedDoc = PDDocument.load(noFieldsPdf);
        printer.printFields(loadedDoc);
        loadedDoc.close();

        String output = testOut.toString();
        assertTrue("Should show 0 fields", output.contains("0 top-level fields"));

        // Cleanup
        noFieldsPdf.delete();
    }

    /**
     * Test with non-existent file.
     */
    public void testNonExistentFile()
    {
        String[] args = new String[] { outDir + "nonexistent.pdf" };

        try
        {
            PrintFields.main(args);
            fail("Should throw exception for non-existent file");
        }
        catch (Exception e)
        {
            // Expected
        }
    }

    /**
     * Test with field containing null value.
     */
    public void testFieldWithNullValue() throws Exception
    {
        File nullValuePdf = new File(outDir, "nullvalue.pdf");

        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());

        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);

        PDTextField field = new PDTextField(acroForm);
        field.setPartialName("nullField");
        // Don't set value - leave it null
        acroForm.getFields().add(field);

        doc.save(nullValuePdf);
        doc.close();

        PrintFields printer = new PrintFields();
        PDDocument loadedDoc = PDDocument.load(nullValuePdf);
        printer.printFields(loadedDoc);
        loadedDoc.close();

        String output = testOut.toString();
        assertTrue("Should handle null field value", output.contains("no value available") || output.contains("nullField"));

        // Cleanup
        nullValuePdf.delete();
    }
}