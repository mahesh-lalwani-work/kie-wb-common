/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.forms.jbpm.client.rendering.documents;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.ScriptInjector;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.forms.adf.rendering.Renderer;
import org.kie.workbench.common.forms.dynamic.client.rendering.FieldRenderer;
import org.kie.workbench.common.forms.dynamic.client.rendering.FormFieldImpl;
import org.kie.workbench.common.forms.dynamic.client.rendering.formGroups.FormGroup;
import org.kie.workbench.common.forms.dynamic.client.rendering.formGroups.impl.def.DefaultFormGroup;
import org.kie.workbench.common.forms.dynamic.client.rendering.renderers.RequiresValueConverter;
import org.kie.workbench.common.forms.dynamic.client.rendering.renderers.lov.converters.ListToListConverter;
import org.kie.workbench.common.forms.dynamic.service.shared.RenderMode;
import org.kie.workbench.common.forms.jbpm.client.rendering.documents.control.DocumentUpload;
import org.kie.workbench.common.forms.jbpm.client.rendering.util.DocumentSizeHelper;
import org.kie.workbench.common.forms.jbpm.client.resources.i18n.Constants;
import org.kie.workbench.common.forms.jbpm.model.authoring.documents.definition.DocumentCollectionFieldDefinition;
import org.kie.workbench.common.forms.jbpm.model.authoring.documents.type.DocumentCollectionFieldType;
import org.kie.workbench.common.forms.jbpm.model.document.DocumentData;
import org.kie.workbench.common.forms.jbpm.model.document.DocumentStatus;
import org.kie.workbench.common.forms.processing.engine.handling.CustomFieldValidator;
import org.kie.workbench.common.forms.processing.engine.handling.ValidationResult;

@Dependent
@Renderer(type = DocumentCollectionFieldType.class)
public class DocumentCollectionFieldRenderer extends FieldRenderer<DocumentCollectionFieldDefinition, DefaultFormGroup> implements RequiresValueConverter {

    private static final Integer MAX_CONTENT_SIZE = 20 * 1024 * 1024;

    private final TranslationService translationService;
    private final DocumentUpload upload;

    @Inject
    public DocumentCollectionFieldRenderer(final TranslationService translationService, final DocumentUpload upload) {
        this.translationService = translationService;
        this.upload = upload;
    }

    @Override
    protected FormGroup getFormGroup(RenderMode renderMode) {
        DefaultFormGroup formGroup = formGroupsInstance.get();

        formGroup.render(upload.asWidget(), field);
        upload.setMaxDocuments(field.getMaxDocuments());
        upload.setAllowedExtensions(field.getEnabledFileExtensions());

        return formGroup;
    }

    @Override
    public String getName() {
        return DocumentCollectionFieldDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        upload.setEnabled(!readOnly);
    }

    @Override
    protected void registerCustomFieldValidators(FormFieldImpl formField) {
        super.registerCustomFieldValidators(formField);
        
        CustomFieldValidator<List<DocumentData>> maxContentSizeWarning = values -> {

            long contentSize = values.stream()
                    .filter(documentData -> !documentData.getStatus().equals(DocumentStatus.STORED))
                    .mapToLong(DocumentData::getSize)
                    .sum();

            if (contentSize > MAX_CONTENT_SIZE) {
                String size = DocumentSizeHelper.getFormattedDocumentSize(contentSize);
                String message = translationService.format(Constants.DocumentListFieldRendererMaxContentLengthWarning, size);
                return ValidationResult.warning(message);
            }

            return ValidationResult.valid();
        };
        CustomFieldValidator<List<DocumentData>> maxDocumentsError = values -> {

            int maxDocuments = getField().getMaxDocuments();

            if (maxDocuments > 0 && maxDocuments < values.size()) {
                String message = translationService.format(Constants.DocumentListFieldRendererMaxDocumentsReached, maxDocuments);
                return ValidationResult.error(message);
            }

            return ValidationResult.valid();
        };
        CustomFieldValidator<List<DocumentData>> fileExtensionValidator = values -> {
            String allowedExtensions = getField().getEnabledFileExtensions();
            
            // Only validate if allowedExtensions is configured
            if (allowedExtensions != null && !allowedExtensions.trim().isEmpty()) {
                if (values != null && !values.isEmpty()) {
                    for (DocumentData documentData : values) {
                        if (documentData != null && documentData.getFileName() != null && !documentData.getFileName().trim().isEmpty()) {
                            if (!isValidFileExtension(documentData.getFileName(), allowedExtensions)) {
                                String extension = getFileExtension(documentData.getFileName());
                                String errorMessage = "File extension '" + extension + "' is not in the allowed list: " + allowedExtensions;
                                return ValidationResult.error(errorMessage);
                            }
                        }
                    }
                }
            }
            
            return ValidationResult.valid();
        };
        
        formField.getCustomValidators().add(maxContentSizeWarning);
        formField.getCustomValidators().add(maxDocumentsError);
        formField.getCustomValidators().add(fileExtensionValidator);
        
        // Initialize client-side validation for document collection
        initializeClientSideValidation();
    }
    
    /**
     * Initialize client-side validation for the document collection field.
     * This method sets up JavaScript validation to provide immediate feedback
     * to users when they select files with invalid extensions.
     */
    private void initializeClientSideValidation() {
        // Get the allowed extensions for this field
        String formAllowedExtensions = getField().getEnabledFileExtensions();
        String globalAllowedExtensions = getGlobalAllowedExtensions();
        
        // Create JavaScript code to initialize validation for document collection
        String formExt = formAllowedExtensions != null ? formAllowedExtensions : "";
        String globalExt = globalAllowedExtensions != null ? globalAllowedExtensions : "";
        String fieldName = getField().getName();
        
        String jsCode = 
            "if (typeof appformer !== 'undefined' && appformer.forms && appformer.forms.Documents) {" +
            "  var uploadElement = document.querySelector('[data-field-id=\"" + fieldName + "\"]');" +
            "  if (uploadElement) {" +
            "    var documentsUpload = appformer.forms.Documents.get();" +
            "    documentsUpload.initializeValidation('" + formExt + "', '" + globalExt + "');" +
            "    documentsUpload.bind(uploadElement);" +
            "  }" +
            "}";
        
        // Execute the JavaScript code
        ScriptInjector.fromString(jsCode).inject();
    }
    
    /**
     * Get global allowed extensions from Manage Preferences or default configuration.
     * 
     * @return comma-separated string of global allowed extensions
     */
    private String getGlobalAllowedExtensions() {
        // For document collection, we can use a simpler approach
        // since the validation is primarily handled by the individual document validators
        return "pdf,docx,xlsx,txt,jpg,png";
    }

    @Override
    public Converter getConverter() {
        return new ListToListConverter();
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
}
