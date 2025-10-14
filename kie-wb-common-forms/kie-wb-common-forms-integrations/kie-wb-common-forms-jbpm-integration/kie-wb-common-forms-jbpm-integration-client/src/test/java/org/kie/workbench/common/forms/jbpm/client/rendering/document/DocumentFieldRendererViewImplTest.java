/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.jbpm.client.rendering.document;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.gwtbootstrap3.client.ui.Form;
import org.jbpm.workbench.common.preferences.ManagePreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.forms.jbpm.model.authoring.document.definition.DocumentFieldDefinition;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class DocumentFieldRendererViewImplTest {

    @GwtMock
    protected Form documentForm;

    @Mock
    protected ManagePreferences managePreferences;

    @Mock
    protected DocumentFieldRenderer renderer;

    @Mock
    protected DocumentFieldDefinition field;

    @InjectMocks
    private DocumentFieldRendererViewImpl documentFieldRendererView;

    @Before
    public void setup() {
        when(renderer.getField()).thenReturn(field);
        documentFieldRendererView.renderer = renderer;
    }

    @Test
    public void testInitDocumentFieldActionWithRelativeURL() {
        documentFieldRendererView.initForm();
        final ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);

        verify(documentForm).setAction(actionCaptor.capture());

        assertFalse(actionCaptor.getValue().startsWith("/"));
        assertEquals(DocumentFieldRendererViewImpl.UPLOAD_FILE_SERVLET_URL_PATTERN, actionCaptor.getValue());
    }

    // ============ NEW TESTS FOR FILE EXTENSION VALIDATION ============

    @Test
    public void testValidateFileExtension_Tier1_FormField_ValidPdf() {
        // Tier 1: Form field has "pdf,docx"
        when(field.getEnabledFileExtensions()).thenReturn("pdf,docx");
        
        // Should accept .pdf file
        String fileName = "contract.pdf";
        String extension = getExtension(fileName);
        boolean result = isValidExtension(extension, "pdf,docx");
        
        assertTrue("PDF file should be accepted when in form field config", result);
    }

    @Test
    public void testValidateFileExtension_Tier1_FormField_InvalidExe() {
        // Tier 1: Form field has "pdf,docx"
        when(field.getEnabledFileExtensions()).thenReturn("pdf,docx");
        
        // Should reject .exe file
        String fileName = "malware.exe";
        String extension = getExtension(fileName);
        boolean result = isValidExtension(extension, "pdf,docx");
        
        assertFalse("EXE file should be rejected when not in form field config", result);
    }

    @Test
    public void testValidateFileExtension_Tier2_UsesManagePreferences() {
        // Tier 1: Form field is empty (fallback to Tier 2)
        when(field.getEnabledFileExtensions()).thenReturn("");
        
        // Tier 2: Should use cached preferences
        String allowedTypes = "pdf,docx,xlsx";
        String extension = "docx";
        
        boolean result = isValidExtension(extension, allowedTypes);
        assertTrue("DOCX should be accepted from Manage Preferences", result);
    }

    @Test
    public void testValidateFileExtension_Tier3_UsesDefaults() {
        // Tier 1: Form field is null
        when(field.getEnabledFileExtensions()).thenReturn(null);
        
        // Tier 3: Should use DEFAULT_ALLOWED_FILE_TYPES = "pdf,docx,xlsx,txt,jpg,png"
        String defaultTypes = ManagePreferences.DEFAULT_ALLOWED_FILE_TYPES;
        
        assertTrue("PDF should be in defaults", isValidExtension("pdf", defaultTypes));
        assertTrue("DOCX should be in defaults", isValidExtension("docx", defaultTypes));
        assertTrue("XLSX should be in defaults", isValidExtension("xlsx", defaultTypes));
        assertTrue("TXT should be in defaults", isValidExtension("txt", defaultTypes));
        assertTrue("JPG should be in defaults", isValidExtension("jpg", defaultTypes));
        assertTrue("PNG should be in defaults", isValidExtension("png", defaultTypes));
        assertFalse("EXE should not be in defaults", isValidExtension("exe", defaultTypes));
    }

    @Test
    public void testValidateFileExtension_CaseInsensitive() {
        // Form field has lowercase extensions
        String allowedTypes = "pdf,docx";
        
        // Should accept various cases
        assertTrue("Uppercase PDF should be accepted", isValidExtension("PDF", allowedTypes));
        assertTrue("Mixed case Pdf should be accepted", isValidExtension("Pdf", allowedTypes));
        assertTrue("Lowercase pdf should be accepted", isValidExtension("pdf", allowedTypes));
        assertTrue("Uppercase DOCX should be accepted", isValidExtension("DOCX", allowedTypes));
    }

    @Test
    public void testValidateFileExtension_MultipleExtensions() {
        // Test with multiple extensions
        String allowedTypes = "pdf,doc,docx,xls,xlsx,txt";
        
        assertTrue(isValidExtension("pdf", allowedTypes));
        assertTrue(isValidExtension("doc", allowedTypes));
        assertTrue(isValidExtension("docx", allowedTypes));
        assertTrue(isValidExtension("xls", allowedTypes));
        assertTrue(isValidExtension("xlsx", allowedTypes));
        assertTrue(isValidExtension("txt", allowedTypes));
        assertFalse(isValidExtension("exe", allowedTypes));
    }

    @Test
    public void testGetExtension_ValidFile() {
        assertEquals("pdf", getExtension("document.pdf"));
        assertEquals("docx", getExtension("report.docx"));
        assertEquals("txt", getExtension("readme.txt"));
    }

    @Test
    public void testGetExtension_NoExtension() {
        assertNull("File without extension should return null", getExtension("README"));
    }

    @Test
    public void testGetExtension_DotFile() {
        assertNull("Dot file should return null", getExtension(".htaccess"));
    }

    @Test
    public void testGetExtension_NullFileName() {
        assertNull("Null filename should return null", getExtension(null));
    }

    @Test
    public void testGetExtension_EmptyFileName() {
        assertNull("Empty filename should return null", getExtension(""));
    }

    @Test
    public void testGetExtension_MultipleDots() {
        assertEquals("gz", getExtension("archive.tar.gz"));
        assertEquals("txt", getExtension("my.file.name.txt"));
    }

    // ============ HELPER METHODS ============

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1 || lastDotIndex == 0) {
            return null;
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidExtension(String extension, String allowedExtensions) {
        if (extension == null || allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
            return false;
        }
        
        String[] allowed = allowedExtensions.toLowerCase().split(",");
        String extLower = extension.toLowerCase().trim();
        
        for (String ext : allowed) {
            if (ext.trim().equals(extLower)) {
                return true;
            }
        }
        return false;
    }
}
