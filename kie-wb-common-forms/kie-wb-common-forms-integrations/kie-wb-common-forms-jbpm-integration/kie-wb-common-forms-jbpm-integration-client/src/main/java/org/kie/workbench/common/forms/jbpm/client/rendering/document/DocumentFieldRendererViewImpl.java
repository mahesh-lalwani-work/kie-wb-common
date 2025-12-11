/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.common.preferences.ManagePreferences;
import org.kie.workbench.common.forms.jbpm.model.authoring.document.definition.DocumentFieldDefinition;
import org.kie.workbench.common.forms.jbpm.model.document.DocumentData;
import org.kie.workbench.common.forms.jbpm.model.document.DocumentStatus;
import org.uberfire.ext.widgets.common.client.common.FileUpload;
import org.uberfire.ext.widgets.common.client.common.FileUploadFormEncoder;

import static org.kie.workbench.common.forms.jbpm.client.rendering.util.DocumentSizeHelper.getFormattedDocumentSize;

@Templated
public class DocumentFieldRendererViewImpl extends Composite implements DocumentFieldRendererView {

    public static String UPLOAD_FILE_SERVLET_URL_PATTERN = "documentUploadServlet";

    @Inject
    @DataField
    protected FlowPanel linkContainer;

    @DataField
    protected Form documentForm = GWT.create(Form.class);

    private FileUploadFormEncoder formEncoder = GWT.create(FileUploadFormEncoder.class);

    protected FileUpload uploader;

    protected DocumentData value;

    protected DocumentFieldRenderer renderer;

    @Inject
    protected ManagePreferences managePreferences;

    private String cachedAllowedFileTypes = null;

    @PostConstruct
    protected void init() {
        // Initialize form immediately (cannot be delayed)
        initForm();
        
        // Load ManagePreferences in the background for validation
        if (managePreferences != null) {
            managePreferences.load(prefs -> {
                cachedAllowedFileTypes = prefs.getAllowedFileTypes();
            }, error -> {
                cachedAllowedFileTypes = null;
            });
        }
    }

    protected void initForm() {
        documentForm.clear();
        uploader = new FileUpload(() -> {
            if (uploader.getFilename() != null && !uploader.getFilename().isEmpty()) {
                if (validateFileExtension(uploader.getFilename())) {
                    documentForm.submit();
                } else {
                    showFileExtensionError(uploader.getFilename());
                }
            }
        });
        uploader.setName("document");
        documentForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        documentForm.setMethod(FormPanel.METHOD_POST);
        documentForm.setAction(UPLOAD_FILE_SERVLET_URL_PATTERN);
        formEncoder.addUtf8Charset(documentForm);
        documentForm.add(uploader);
        documentForm.addSubmitCompleteHandler(event -> {
            onSubmit(event.getResults());
        });
    }

    @Override
    public void setRenderer(DocumentFieldRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Validates if a file extension is allowed using 3-tier hierarchy:
     * Tier 1: Form field configuration (highest priority)
     * Tier 2: Manage Preferences (loaded via ManagePreferences.load())
     * Tier 3: Default allowed file types (fallback)
     * 
     * @param fileName the name of the file to validate
     * @return true if the file extension is allowed, false otherwise
     */
    private boolean validateFileExtension(String fileName) {
        if (renderer == null || renderer.getField() == null) {
            return false;
        }
        
        // Tier 1: Form field configuration (highest priority)
        DocumentFieldDefinition field = renderer.getField();
        String allowedExtensions = field.getEnabledFileExtensions();
        
        // Tier 2: Manage Preferences (from cached value loaded at init)
        if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
            if (cachedAllowedFileTypes != null && !cachedAllowedFileTypes.trim().isEmpty()) {
                allowedExtensions = cachedAllowedFileTypes;
            }
        }
        
        // Tier 3: Default allowed file types (fallback)
        if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
            allowedExtensions = ManagePreferences.DEFAULT_ALLOWED_FILE_TYPES;
        }
        
        return isValidFileExtension(fileName, allowedExtensions);
    }

    /**
     * Validates if a file extension is allowed.
     * 
     * @param fileName          the name of the file to validate
     * @param allowedExtensions comma-separated list of allowed extensions
     * @return true if the file extension is allowed, false otherwise
     */
    private boolean isValidFileExtension(String fileName, String allowedExtensions) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        String extension = getFileExtension(fileName);
        if (extension == null) {
            return false;
        }

        // Basic validation against allowed extensions
        if (allowedExtensions != null && !allowedExtensions.trim().isEmpty()) {
            String[] allowedExts = allowedExtensions.split(",");
            for (String allowedExt : allowedExts) {
                if (allowedExt.trim().equalsIgnoreCase(extension)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Extracts the file extension from a filename.
     * 
     * @param fileName the filename to extract extension from
     * @return the file extension (lowercase) or null if no valid extension found
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Shows an error message for invalid file extensions.
     * 
     * @param fileName the name of the invalid file
     */
    private void showFileExtensionError(String fileName) {
        // Determine which tier was used (same logic as validateFileExtension)
        String allowedExtensions = renderer != null && renderer.getField() != null ? 
            renderer.getField().getEnabledFileExtensions() : "";
        String source = "form field";
        
        if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
            if (cachedAllowedFileTypes != null && !cachedAllowedFileTypes.trim().isEmpty()) {
                allowedExtensions = cachedAllowedFileTypes;
                source = "Manage Preferences";
            } else {
                allowedExtensions = ManagePreferences.DEFAULT_ALLOWED_FILE_TYPES;
                source = "default configuration";
            }
        }
        
        String extension = getFileExtension(fileName);
        String errorMessage = "File extension '" + extension + "' is not allowed. Allowed types (" + source + "): " + allowedExtensions;
        
        if (renderer != null) {
            renderer.showError(errorMessage);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        uploader.setEnabled(!readOnly);
    }

    @Override
    public void setValue(DocumentData value) {
        setValue(value,
                 false);
    }

    @Override
    public void setValue(DocumentData value,
                         boolean fireEvents) {
        if (this.value == value) {
            return;
        }
        if (value == null || value.getStatus().equals(DocumentStatus.NEW) || !value.equals(this.value)) {

            this.value = value;

            linkContainer.clear();
            if (this.value != null) {

                Anchor link = new Anchor();
                link.setText(value.getFileName() + " (" + calculateSize() + ")");

                if (value.getLink() == null) {
                    link.setEnabled(false);
                    link.setHref("#");
                    link.addClickHandler(event -> {
                        return;
                    });
                } else {
                    link.setHref(value.getLink());
                    link.setTarget("_blank");
                }
                linkContainer.add(link);
            }

            if (fireEvents) {
                ValueChangeEvent.fire(this,
                                      this.value);
            }
        }
    }

    protected String calculateSize() {
        return getFormattedDocumentSize(value.getSize());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DocumentData> valueChangeHandler) {
        return this.addHandler(valueChangeHandler,
                               ValueChangeEvent.getType());
    }

    @Override
    public DocumentData getValue() {
        return value;
    }

    public void onSubmit(String results) {
        initForm();
        JavaScriptObject jsResponse = JsonUtils.safeEval(results);

        if (jsResponse != null) {
            JSONObject response = new JSONObject(jsResponse);
            if (response.get("document") != null) {
                JSONObject document = response.get("document").isObject();

                DocumentData data = new DocumentData(document.get("contentId").isString().stringValue(),
                                                     document.get("fileName").isString().stringValue(),
                                                     new Double(document.get("size").isNumber().doubleValue()).longValue(),
                                                     null,
                                                     new Double(document.get("lastModified").isNumber().doubleValue()).longValue());

                setValue(data, true);
            } else if (response.get("error").isNull() != null) {
                setValue(null, true);
            }
        }
    }
}
