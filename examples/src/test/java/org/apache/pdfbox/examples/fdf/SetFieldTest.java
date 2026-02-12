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
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * Test case for SetField.
 *
 * @author Test Author
 */
public class SetFieldTest extends TestCase
{
    private final String outDir = "target/test-output/examples/fdf/";
    private File testPdfWithFields;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        File dir = new File(outDir);
        dir.mkdirs();

        // Create a test PDF with form fields
        testPdfWithFields = new File(outDir, "setfieldtest.pdf");
        createPDFWithFields(testPdfWithFields);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

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
            textField.setValue("Original Value");
            acroForm.getFields().add(textField);

            // Add another text field
            PDTextField textField2 = new PDTextField(acroForm);
            textField2.setPartialName("anotherField");
            textField2.setValue("Another Original Value");
            acroForm.getFields().add(textField2);

            document.save(file);
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Test setField method successfully sets a field value.
     */
    public void testSetField() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        SetField setter = new SetField();
        setter.setField(document, "testField", "New Value");

        // Verify the field was set
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField("testField");
        assertEquals("Field value should be updated", "New Value", field.getValue());

        document.close();
    }

    /**
     * Test setField with non-existent field.
     */
    public void testSetNonExistentField() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        // Capture System.err
        PrintStream originalErr = System.err;
        ByteArrayOutputStream testErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(testErr));

        try
        {
            SetField setter = new SetField();
            setter.setField(document, "nonExistentField", "Some Value");

            String errOutput = testErr.toString();
            assertTrue("Should print error for non-existent field",
                      errOutput.contains("No field found") || errOutput.contains("nonExistentField"));
        }
        finally
        {
            System.setErr(originalErr);
            document.close();
        }
    }

    /**
     * Test setField with null field name.
     */
    public void testSetFieldWithNullName() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        SetField setter = new SetField();
        // Should handle null gracefully
        try
        {
            setter.setField(document, null, "Some Value");
        }
        catch (Exception e)
        {
            // May throw NullPointerException or similar
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Test setField with empty value.
     */
    public void testSetFieldWithEmptyValue() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        SetField setter = new SetField();
        setter.setField(document, "testField", "");

        // Verify the field was set to empty
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField("testField");
        assertEquals("Field value should be empty string", "", field.getValue());

        document.close();
    }

    /**
     * Test main method with valid arguments.
     */
    public void testMainWithValidArguments() throws Exception
    {
        String[] args = new String[] {
            testPdfWithFields.getAbsolutePath(),
            "testField",
            "Updated via Main"
        };

        SetField.main(args);

        // Verify the field was updated in the saved file
        PDDocument doc = PDDocument.load(testPdfWithFields);
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField("testField");
        assertEquals("Field should be updated", "Updated via Main", field.getValue());
        doc.close();
    }

    /**
     * Test main method with incorrect number of arguments.
     */
    public void testMainWithIncorrectArguments() throws Exception
    {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream testErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(testErr));

        try
        {
            // Test with too few arguments
            String[] args = new String[] { testPdfWithFields.getAbsolutePath() };
            SetField.main(args);

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
        PrintStream originalErr = System.err;
        ByteArrayOutputStream testErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(testErr));

        try
        {
            String[] args = new String[] {
                testPdfWithFields.getAbsolutePath(),
                "field",
                "value",
                "extra"
            };
            SetField.main(args);

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
    public void testSetFieldEncryptedPDF() throws Exception
    {
        File encryptedPdf = new File(outDir, "encrypted-setfield.pdf");

        // Create encrypted PDF
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);

        PDTextField field = new PDTextField(acroForm);
        field.setPartialName("encryptedField");
        field.setValue("Original Encrypted Value");
        acroForm.getFields().add(field);

        doc.encrypt("", "", null);
        doc.save(encryptedPdf);
        doc.close();

        String[] args = new String[] {
            encryptedPdf.getAbsolutePath(),
            "encryptedField",
            "New Encrypted Value"
        };

        SetField.main(args);

        // Verify the field was updated
        PDDocument loadedDoc = PDDocument.load(encryptedPdf);
        loadedDoc.decrypt("");
        PDAcroForm loadedForm = loadedDoc.getDocumentCatalog().getAcroForm();
        PDField loadedField = loadedForm.getField("encryptedField");
        assertEquals("Encrypted field should be updated", "New Encrypted Value", loadedField.getValue());
        loadedDoc.close();

        // Cleanup
        encryptedPdf.delete();
    }

    /**
     * Test setting multiple fields in sequence.
     */
    public void testSetMultipleFields() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        SetField setter = new SetField();
        setter.setField(document, "testField", "First Update");
        setter.setField(document, "anotherField", "Second Update");

        // Verify both fields were set
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field1 = acroForm.getField("testField");
        PDField field2 = acroForm.getField("anotherField");

        assertEquals("First field should be updated", "First Update", field1.getValue());
        assertEquals("Second field should be updated", "Second Update", field2.getValue());

        document.close();
    }

    /**
     * Test setting field with special characters.
     */
    public void testSetFieldWithSpecialCharacters() throws Exception
    {
        PDDocument document = PDDocument.load(testPdfWithFields);

        String specialValue = "Special: <>&\"' @#$%";
        SetField setter = new SetField();
        setter.setField(document, "testField", specialValue);

        // Verify the field was set with special characters
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField("testField");
        assertEquals("Field should handle special characters", specialValue, field.getValue());

        document.close();
    }

    /**
     * Test with non-existent PDF file.
     */
    public void testMainWithNonExistentFile()
    {
        String[] args = new String[] {
            outDir + "nonexistent.pdf",
            "field",
            "value"
        };

        try
        {
            SetField.main(args);
            fail("Should throw exception for non-existent file");
        }
        catch (Exception e)
        {
            // Expected
        }
    }
}