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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.ScriptInjector;

import org.kie.workbench.common.forms.adf.rendering.Renderer;
import org.kie.workbench.common.forms.dynamic.client.rendering.FieldRenderer;
import org.kie.workbench.common.forms.dynamic.client.rendering.FormFieldImpl;
import org.kie.workbench.common.forms.dynamic.client.rendering.formGroups.FormGroup;
import org.kie.workbench.common.forms.dynamic.client.rendering.formGroups.impl.def.DefaultFormGroup;
import org.kie.workbench.common.forms.dynamic.service.shared.RenderMode;
import org.kie.workbench.common.forms.jbpm.client.rendering.document.config.ManagePreferencesConfigService;
import org.kie.workbench.common.forms.jbpm.model.authoring.document.definition.DocumentFieldDefinition;
import org.kie.workbench.common.forms.jbpm.model.authoring.document.type.DocumentFieldType;
import org.kie.workbench.common.forms.jbpm.model.document.DocumentData;
import org.kie.workbench.common.forms.processing.engine.handling.CustomFieldValidator;
import org.kie.workbench.common.forms.processing.engine.handling.ValidationResult;

@Dependent
@Renderer(type = DocumentFieldType.class)
public class DocumentFieldRenderer extends FieldRenderer<DocumentFieldDefinition, DefaultFormGroup> {

    private DocumentFieldRendererView view;

    @Inject
    public DocumentFieldRenderer(DocumentFieldRendererView view) {
        this.view = view;
    }

    @PostConstruct
    protected void doInit() {
        view.setRenderer(this);
    }

    @Override
    public String getName() {
        return "Document";
    }

    @Override
    protected FormGroup getFormGroup(RenderMode renderMode) {
        DefaultFormGroup formGroup = formGroupsInstance.get();

        view.setReadOnly(field.getReadOnly() || !renderingContext.getRenderMode().equals(RenderMode.EDIT_MODE));

        formGroup.render(view.asWidget(), field);

        return formGroup;
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        view.setReadOnly(readOnly);
    }

    /**
     * Shows an error message below the field using the proper form validation system.
     * 
     * @param errorMessage the error message to display
     */
    public void showError(String errorMessage) {
        if (formField != null) {
            formField.showError(errorMessage);
        }
    }

    @Override
    protected void registerCustomFieldValidators(FormFieldImpl formField) {
        super.registerCustomFieldValidators(formField);
        
        CustomFieldValidator<DocumentData> fileExtensionValidator = documentData -> {
            if (documentData != null && documentData.getFileName() != null && !documentData.getFileName().trim().isEmpty()) {
                String allowedExtensions = getField().getEnabledFileExtensions();
                String source = "form field";
                
                // Tier 1: Form Field (highest priority)
                if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
                    // Tier 2: Manage Preferences
                    if (ManagePreferencesConfigService.isConfigurationAvailable()) {
                        List<String> managePrefsExtensions = ManagePreferencesConfigService.getAllowedExtensionsList();
                        if (!managePrefsExtensions.isEmpty()) {
                            allowedExtensions = String.join(",", managePrefsExtensions);
                            source = "Manage Preferences";
                        }
                    }
                    
                    // Tier 3: Default configuration
                    if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
                        String rawConfig = ManagePreferencesConfigService.getRawConfiguration();
                        if (rawConfig != null && !rawConfig.trim().isEmpty()) {
                            allowedExtensions = rawConfig;
                            source = "default configuration";
                        }
                    }
                    
                    // If still no extensions configured, show helpful error
                    if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
                        return ValidationResult.error("No file types are configured. Please configure allowed file extensions in Admin > Process Administration > Manage Preferences.");
                    }
                }
                
                // Validate against configured extensions
                if (!isValidFileExtension(documentData.getFileName(), allowedExtensions)) {
                    String extension = getFileExtension(documentData.getFileName());
                    String errorMessage = "File extension '" + extension + "' is not allowed. Allowed types (" + source + "): " + allowedExtensions;
                    return ValidationResult.error(errorMessage);
                }
            }
            
            return ValidationResult.valid();
        };
        
        formField.getCustomValidators().add(fileExtensionValidator);
        
        initializeClientSideValidation();
    }
    
    /**
     * Initialize client-side validation for the document field.
     * This method sets up JavaScript validation to provide immediate feedback
     * to users when they select files with invalid extensions.
     */
    private void initializeClientSideValidation() {
        // Get the allowed extensions for this field
        String formAllowedExtensions = getField().getEnabledFileExtensions();
        String globalAllowedExtensions = getGlobalAllowedExtensions();
        
        // Create JavaScript code to initialize validation
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
        if (ManagePreferencesConfigService.isConfigurationAvailable()) {
            List<String> managePrefsExtensions = ManagePreferencesConfigService.getAllowedExtensionsList();
            if (!managePrefsExtensions.isEmpty()) {
                return String.join(",", managePrefsExtensions);
            }
        }
        
        // Fallback to default configuration
        String rawConfig = ManagePreferencesConfigService.getRawConfiguration();
        if (rawConfig != null && !rawConfig.trim().isEmpty()) {
            return rawConfig;
        }
        
        // Final fallback to hardcoded defaults
        return "pdf,docx,xlsx,txt,jpg,png";
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
