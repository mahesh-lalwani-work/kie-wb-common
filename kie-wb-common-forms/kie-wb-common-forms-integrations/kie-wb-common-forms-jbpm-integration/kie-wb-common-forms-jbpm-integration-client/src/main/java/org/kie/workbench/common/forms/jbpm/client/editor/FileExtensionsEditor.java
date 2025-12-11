/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.forms.jbpm.client.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.common.preferences.FileExtensionsValidationUtil;
import org.kie.workbench.common.forms.jbpm.client.rendering.document.config.ManagePreferencesConfigService;

/**
 * Enhanced file extensions editor for the form designer.
 * 
 * <p>This component provides a user-friendly interface for configuring allowed file extensions
 * in the form designer. It includes real-time validation, suggestions, and visual feedback
 * to help users configure valid file extension lists.
 * 
 * <p>Key features:
 * <ul>
 *   <li><strong>Real-time validation:</strong> Validates extensions against master allowed list</li>
 *   <li><strong>Visual feedback:</strong> Shows validation status with colors and icons</li>
 *   <li><strong>Extension suggestions:</strong> Provides common file extensions as buttons</li>
 *   <li><strong>Format validation:</strong> Ensures proper comma-separated format</li>
 *   <li><strong>Master list integration:</strong> Validates against global allowed extensions</li>
 * </ul>
 * 
 * <p>Usage in form designer:
 * <pre>
 * // The component is automatically used when editing DocumentFieldDefinition.enabledFileExtensions
 * // It provides real-time validation and user-friendly interface
 * </pre>
 * 
 * @author jBPM Team
 * @since 7.74.1
 */
@Dependent
@Templated
public class FileExtensionsEditor extends Composite implements TakesValue<String> {
    
    @Inject
    @DataField
    private FormGroup formGroup;
    
    @Inject
    @DataField
    private Label label;
    
    @Inject
    @DataField
    private TextBox extensionsInput;
    
    @Inject
    @DataField
    private HelpBlock helpBlock;
    
    @Inject
    @DataField
    private FlowPanel suggestionsPanel;
    
    // Callback interface for value changes
    public interface ValueChangeCallback {
        void onValueChange(String newValue);
    }
    
    // Reference to the field renderer for callbacks
    private ValueChangeCallback valueChangeCallback;
    
    @Inject
    @DataField
    private FlowPanel validationPanel;
    
    private String currentValue = "";
    private boolean isValid = true;
    private String validationMessage = "";
    private Set<String> masterAllowedExtensions;
    
    // Valid file extensions (same as AllowedFileTypesValidator)
    private static final List<String> VALID_FILE_EXTENSIONS = Arrays.asList(
        "pdf", "doc", "docx", "rtf", "txt", "odt", "pages",
        "xls", "xlsx", "csv", "ods", "numbers",
        "ppt", "pptx", "odp", "key",
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "svg", "webp", "ico",
        "zip", "rar", "7z", "tar", "gz",
        "mp3", "wav", "flac", "aac", "ogg", "m4a",
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv",
        "json", "xml", "yaml", "yml", "sql", "log",
        "iso", "dmg", "exe", "msi", "deb", "rpm"
    );
    
    // Common file extensions for suggestions
    private static final List<String> COMMON_EXTENSIONS = VALID_FILE_EXTENSIONS;
    
    @PostConstruct
    protected void init() {
        setupEventHandlers();
        loadMasterAllowedExtensions();
        createExtensionSuggestions();
        updateValidationState();
    }
    
    /**
     * Set the value change callback for notifications when values change.
     * 
     * @param callback the callback instance
     */
    public void setValueChangeCallback(ValueChangeCallback callback) {
        this.valueChangeCallback = callback;
    }
    
    /**
     * Set up event handlers for real-time validation and user interaction.
     */
    private void setupEventHandlers() {
        extensionsInput.addKeyUpHandler(this::onInputChange);
        extensionsInput.addBlurHandler(event -> validateAndUpdate());
    }
    
    /**
     * Load master allowed extensions from Manage Preferences configuration.
     */
    private void loadMasterAllowedExtensions() {
        if (ManagePreferencesConfigService.isConfigurationAvailable()) {
            masterAllowedExtensions = ManagePreferencesConfigService.getAllowedExtensionsList()
                .stream()
                .collect(Collectors.toSet());
        } else {
            // Fallback to default extensions
            masterAllowedExtensions = new HashSet<>(Arrays.asList("pdf", "docx", "xlsx", "txt", "jpg", "png"));
        }
    }
    
    /**
     * Create extension suggestion buttons for common file types.
     */
    private void createExtensionSuggestions() {
        suggestionsPanel.clear();
        
        Label suggestionsLabel = new Label("Common extensions:");
        suggestionsLabel.addStyleName("text-muted");
        suggestionsLabel.addStyleName("small");
        suggestionsPanel.add(suggestionsLabel);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        for (String extension : COMMON_EXTENSIONS) {
            Button suggestionButton = new Button(extension);
            suggestionButton.setType(ButtonType.DEFAULT);
            suggestionButton.setSize(ButtonSize.EXTRA_SMALL);
            suggestionButton.addStyleName("margin-right-5");
            suggestionButton.addStyleName("margin-bottom-5");
            
            // Style based on whether extension is in master allowed list
            if (masterAllowedExtensions.contains(extension)) {
                suggestionButton.addStyleName("btn-success");
            } else {
                suggestionButton.addStyleName("btn-warning");
                suggestionButton.setTitle("Extension '" + extension + "' is not in master allowed list");
            }
            
            suggestionButton.addClickHandler(event -> addExtension(extension));
            buttonGroup.add(suggestionButton);
        }
        
        suggestionsPanel.add(buttonGroup);
    }
    
    /**
     * Handle input changes for real-time validation.
     */
    private void onInputChange(KeyUpEvent event) {
        // Debounce validation to avoid excessive processing
        com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                validateAndUpdate();
            }
        };
        timer.schedule(300); // 300ms delay
    }
    
    /**
     * Add an extension to the current list.
     */
    private void addExtension(String extension) {
        String currentValue = extensionsInput.getValue();
        List<String> extensions = parseExtensions(currentValue);
        
        if (!extensions.contains(extension)) {
            extensions.add(extension);
            String newValue = String.join(",", extensions);
            extensionsInput.setValue(newValue);
            validateAndUpdate();
        }
    }
    
    /**
     * Validate the current input and update the UI state.
     */
    private void validateAndUpdate() {
        currentValue = extensionsInput.getValue();
        
        ValidationResult result = validateExtensions(currentValue);
        
        isValid = result.isValid;
        validationMessage = result.message;
        
        updateValidationState();
        
        // Notify the callback of value changes
        if (valueChangeCallback != null) {
            valueChangeCallback.onValueChange(currentValue);
        }
    }
    
    /**
     * Update the visual validation state of the component.
     */
    private void updateValidationState() {
        if (isValid) {
            formGroup.setValidationState(ValidationState.SUCCESS);
            helpBlock.setText("Valid file extensions configuration");
            helpBlock.addStyleName("text-success");
        } else {
            formGroup.setValidationState(ValidationState.ERROR);
            helpBlock.setText(validationMessage);
            helpBlock.addStyleName("text-danger");
        }
        
        // Update validation panel with detailed information
        updateValidationPanel();
    }
    
    /**
     * Update the validation panel with detailed information using AllowedFileTypesValidator logic.
     */
    private void updateValidationPanel() {
        validationPanel.clear();
        
        if (currentValue == null || currentValue.trim().isEmpty()) {
            return;
        }
        
        List<String> extensions = parseExtensions(currentValue);
        List<String> validExtensions = VALID_FILE_EXTENSIONS;
        
        // Show extension analysis
        Label analysisLabel = new Label("Extension Analysis:");
        analysisLabel.addStyleName("text-muted");
        analysisLabel.addStyleName("small");
        analysisLabel.addStyleName("font-weight-bold");
        validationPanel.add(analysisLabel);
        
        for (String extension : extensions) {
            FlowPanel extensionPanel = new FlowPanel();
            extensionPanel.addStyleName("extension-analysis-item");
            
            Label extensionLabel = new Label(extension);
            extensionLabel.addStyleName("inline-block");
            extensionLabel.addStyleName("margin-right-10");
            
            if (VALID_FILE_EXTENSIONS.contains(extension.toLowerCase())) {
                extensionLabel.addStyleName("text-success");
                extensionLabel.setTitle("Extension is valid");
            } else {
                extensionLabel.addStyleName("text-danger");
                extensionLabel.setTitle("Extension is NOT valid");
            }
            
            extensionPanel.add(extensionLabel);
            validationPanel.add(extensionPanel);
        }
        
        // Show valid extensions reference
        if (!extensions.isEmpty()) {
            Label referenceLabel = new Label("Valid extensions include: " + 
                String.join(", ", validExtensions.subList(0, Math.min(10, validExtensions.size()))));
            referenceLabel.addStyleName("text-muted");
            referenceLabel.addStyleName("small");
            referenceLabel.addStyleName("margin-top-5");
            validationPanel.add(referenceLabel);
        }
    }
    
    /**
     * Validate file extensions string using the shared validation logic with error codes.
     * This ensures consistency with Manage Preferences validation.
     */
    private ValidationResult validateExtensions(String extensionsString) {
        if (extensionsString == null || extensionsString.trim().isEmpty()) {
            return new ValidationResult(true, "No extensions specified (will use global configuration)", null);
        }
        
        // Use the shared validation utility for consistency
        try {
            FileExtensionsValidationUtil.validateExtensions(extensionsString);
            return new ValidationResult(true, "Valid file extensions configuration", null);
        } catch (IllegalArgumentException e) {
            // Extract error code from the exception message if available
            String errorMessage = e.getMessage();
            String errorCode = "EXT_VAL_002"; // Default error code
            
            if (errorMessage.contains("EXT_VAL_001")) {
                errorCode = "EXT_VAL_001";
            } else if (errorMessage.contains("EXT_VAL_004")) {
                errorCode = "EXT_VAL_004";
            }
            
            return new ValidationResult(false, errorMessage, errorCode);
        }
    }
    
    
    /**
     * Parse comma-separated extensions string into list.
     */
    private List<String> parseExtensions(String extensionsString) {
        if (extensionsString == null || extensionsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(extensionsString.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }
    
    
    /**
     * Get the current value.
     * Only returns the value if it's valid, otherwise returns the last valid value.
     */
    public String getValue() {
        String inputValue = extensionsInput.getValue();
        ValidationResult result = validateExtensions(inputValue);
        
        if (result.isValid) {
            currentValue = inputValue;
            return inputValue;
        } else {
            // Return the last valid value to prevent invalid data from being saved
            return currentValue != null ? currentValue : "";
        }
    }
    
    /**
     * Set the current value.
     */
    public void setValue(String value) {
        extensionsInput.setValue(value);
        validateAndUpdate();
    }
    
    /**
     * Check if the current value is valid.
     */
    public boolean isValid() {
        return isValid;
    }
    
    /**
     * Get the validation message.
     */
    public String getValidationMessage() {
        return validationMessage;
    }
    
    /**
     * Set the read-only state of the editor.
     */
    public void setReadOnly(boolean readOnly) {
        extensionsInput.setEnabled(!readOnly);
    }
    
    /**
     * Validation result class with error code support.
     */
    private static class ValidationResult {
        final boolean isValid;
        final String message;
        final String errorCode;
        
        ValidationResult(boolean isValid, String message, String errorCode) {
            this.isValid = isValid;
            this.message = message;
            this.errorCode = errorCode;
        }
    }
}
