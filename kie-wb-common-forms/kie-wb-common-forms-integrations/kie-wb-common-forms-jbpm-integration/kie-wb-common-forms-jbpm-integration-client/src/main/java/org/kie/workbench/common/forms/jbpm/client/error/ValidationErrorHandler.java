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

package org.kie.workbench.common.forms.jbpm.client.error;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.common.client.resources.i18n.Constants;
import org.kie.workbench.common.forms.jbpm.client.rendering.document.config.ManagePreferencesConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced validation error handler for jBPM document uploads.
 * 
 * <p>This component provides comprehensive error handling and user feedback
 * for file extension validation errors. It translates backend validation
 * results into user-friendly messages and displays them appropriately
 * in the UI.
 * 
 * <p>Key features:
 * <ul>
 *   <li><strong>Error code translation:</strong> Converts backend error codes to user-friendly messages</li>
 *   <li><strong>Contextual feedback:</strong> Provides specific guidance based on error type</li>
 *   <li><strong>Visual error display:</strong> Shows errors with appropriate styling and icons</li>
 *   <li><strong>Recovery suggestions:</strong> Offers actionable solutions for common errors</li>
 *   <li><strong>Multi-language support:</strong> Supports internationalization of error messages</li>
 * </ul>
 * 
 * <p>Usage examples:
 * <pre>
 * // Handle validation error
 * ValidationErrorHandler.handleError("EXT_VAL_101", "document.exe", "pdf,docx");
 * 
 * // Show success message
 * ValidationErrorHandler.showSuccess("File uploaded successfully");
 * 
 * // Clear all errors
 * ValidationErrorHandler.clearErrors();
 * </pre>
 * 
 * @author jBPM Team
 * @since 7.74.1
 */
@ApplicationScoped
@Templated
public class ValidationErrorHandler extends Composite {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationErrorHandler.class);
    
    @Inject
    private ManagePreferencesConfigService managePreferencesConfigService;
    
    private FlowPanel errorContainer;
    private Map<String, String> errorMessages;
    private Map<String, String> recoverySuggestions;
    
    @PostConstruct
    protected void init() {
        initializeErrorMessages();
        initializeRecoverySuggestions();
    }
    
    /**
     * Initialize error message mappings for different error codes.
     * Uses i18n Constants for internationalized messages.
     */
    private void initializeErrorMessages() {
        errorMessages = new HashMap<>();
        
        // Design-time validation errors (001-099)
        errorMessages.put("EXT_VAL_001", Constants.INSTANCE.EXT_VAL_001());
        errorMessages.put("EXT_VAL_002", Constants.INSTANCE.EXT_VAL_002());
        errorMessages.put("EXT_VAL_003", Constants.INSTANCE.EXT_VAL_003());
        errorMessages.put("EXT_VAL_004", Constants.INSTANCE.EXT_VAL_004());
        
        // Runtime validation errors (101-199)
        errorMessages.put("EXT_VAL_101", Constants.INSTANCE.EXT_VAL_101());
        errorMessages.put("EXT_VAL_102", Constants.INSTANCE.EXT_VAL_102());
        errorMessages.put("EXT_VAL_103", Constants.INSTANCE.EXT_VAL_103());
        errorMessages.put("EXT_VAL_104", Constants.INSTANCE.EXT_VAL_104());
        errorMessages.put("EXT_VAL_105", Constants.INSTANCE.EXT_VAL_105());
        errorMessages.put("EXT_VAL_106", Constants.INSTANCE.EXT_VAL_106());
        
        // Process execution errors (201-299)
        errorMessages.put("EXT_VAL_201", Constants.INSTANCE.EXT_VAL_201());
        errorMessages.put("EXT_VAL_202", Constants.INSTANCE.EXT_VAL_202());
        errorMessages.put("EXT_VAL_203", Constants.INSTANCE.EXT_VAL_203());
        errorMessages.put("EXT_VAL_204", Constants.INSTANCE.EXT_VAL_204());
        
        // API errors (301-399)
        errorMessages.put("EXT_VAL_301", Constants.INSTANCE.EXT_VAL_301());
        errorMessages.put("EXT_VAL_302", Constants.INSTANCE.EXT_VAL_302());
        errorMessages.put("EXT_VAL_303", Constants.INSTANCE.EXT_VAL_303());
    }
    
    /**
     * Initialize recovery suggestions for different error types.
     */
    private void initializeRecoverySuggestions() {
        recoverySuggestions = new HashMap<>();
        
        recoverySuggestions.put("EXT_VAL_001", "Enter file extensions separated by commas, without spaces or special characters");
        recoverySuggestions.put("EXT_VAL_002", "Check the master allowed file types in Admin > Process Administration > Manage Preferences");
        recoverySuggestions.put("EXT_VAL_003", "Remove duplicate extensions from the list");
        
        recoverySuggestions.put("EXT_VAL_101", "Choose a file with an allowed extension or contact your administrator to add this extension");
        recoverySuggestions.put("EXT_VAL_102", "Ensure your file has a valid extension (e.g., .pdf, .docx)");
        recoverySuggestions.put("EXT_VAL_103", "Use a file with a standard extension format");
        recoverySuggestions.put("EXT_VAL_104", "Select a file with one of the allowed extensions or request access to this file type");
        
        recoverySuggestions.put("EXT_VAL_201", "Check all document fields in your form and ensure they contain valid files");
        recoverySuggestions.put("EXT_VAL_202", "Verify all uploaded files meet the extension requirements");
        recoverySuggestions.put("EXT_VAL_203", "Review each file in your upload and ensure they are all valid");
        
        recoverySuggestions.put("EXT_VAL_301", "Contact your system administrator to check the configuration service");
        recoverySuggestions.put("EXT_VAL_302", "Try again in a few moments or contact support if the problem persists");
        recoverySuggestions.put("EXT_VAL_303", "Check your network connection and try uploading again");
        
        recoverySuggestions.put("EXT_VAL_401", "Refresh the page and try again, or contact support if the problem continues");
        recoverySuggestions.put("EXT_VAL_402", "Check your internet connection and try again");
        recoverySuggestions.put("EXT_VAL_403", "Contact your system administrator to verify server configuration");
    }
    
    /**
     * Handle a validation error with error code and context.
     * 
     * @param errorCode the error code from backend validation
     * @param context additional context information (file name, extensions, etc.)
     * @return formatted error message for display
     */
    public String handleError(String errorCode, Map<String, String> context) {
        logger.warn("Handling validation error: {} with context: {}", errorCode, context);
        
        String baseMessage = errorMessages.get(errorCode);
        if (baseMessage == null) {
            baseMessage = "Unknown validation error: " + errorCode;
        }
        
        // Replace placeholders with context values
        String formattedMessage = formatMessage(baseMessage, context);
        
        // Display the error
        displayError(errorCode, formattedMessage, context);
        
        return formattedMessage;
    }
    
    /**
     * Handle a validation error with simple parameters.
     * 
     * @param errorCode the error code from backend validation
     * @param fileName the name of the file that caused the error
     * @param allowedExtensions the allowed extensions for context
     * @return formatted error message for display
     */
    public String handleError(String errorCode, String fileName, String allowedExtensions) {
        Map<String, String> context = new HashMap<>();
        context.put("fileName", fileName);
        context.put("extension", extractExtension(fileName));
        context.put("allowedExtensions", allowedExtensions);
        
        return handleError(errorCode, context);
    }
    
    /**
     * Show a success message.
     * 
     * @param message the success message to display
     */
    public void showSuccess(String message) {
        logger.info("Showing success message: {}", message);
        displayMessage("success", message, "fa-check-circle");
    }
    
    /**
     * Show a warning message.
     * 
     * @param message the warning message to display
     */
    public void showWarning(String message) {
        logger.warn("Showing warning message: {}", message);
        displayMessage("warning", message, "fa-exclamation-triangle");
    }
    
    /**
     * Show an info message.
     * 
     * @param message the info message to display
     */
    public void showInfo(String message) {
        logger.info("Showing info message: {}", message);
        displayMessage("info", message, "fa-info-circle");
    }
    
    /**
     * Clear all error messages.
     */
    public void clearErrors() {
        if (errorContainer != null) {
            errorContainer.clear();
        }
    }
    
    /**
     * Format a message by replacing placeholders with context values.
     */
    private String formatMessage(String message, Map<String, String> context) {
        String formattedMessage = message;
        
        for (Map.Entry<String, String> entry : context.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            formattedMessage = formattedMessage.replace(placeholder, entry.getValue());
        }
        
        return formattedMessage;
    }
    
    /**
     * Extract file extension from filename.
     */
    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "none";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Display an error message with appropriate styling.
     */
    private void displayError(String errorCode, String message, Map<String, String> context) {
        displayMessage("error", message, "fa-exclamation-circle");
        
        // Add recovery suggestion if available
        String suggestion = recoverySuggestions.get(errorCode);
        if (suggestion != null) {
            displayMessage("suggestion", suggestion, "fa-lightbulb-o");
        }
        
        // Add context-specific help
        addContextualHelp(errorCode, context);
    }
    
    /**
     * Display a message with specified type and icon.
     */
    private void displayMessage(String type, String message, String iconClass) {
        if (errorContainer == null) {
            errorContainer = new FlowPanel();
            errorContainer.addStyleName("validation-messages");
        }
        
        FlowPanel messagePanel = new FlowPanel();
        messagePanel.addStyleName("validation-message");
        messagePanel.addStyleName("validation-message-" + type);
        
        // Add icon
        HTML icon = new HTML("<i class=\"fa " + iconClass + "\"></i>");
        icon.addStyleName("message-icon");
        messagePanel.add(icon);
        
        // Add message text
        Label messageLabel = new Label(message);
        messageLabel.addStyleName("message-text");
        messagePanel.add(messageLabel);
        
        errorContainer.add(messagePanel);
    }
    
    /**
     * Add contextual help based on error type and context.
     */
    private void addContextualHelp(String errorCode, Map<String, String> context) {
        // Add specific help based on error type
        if (errorCode.startsWith("EXT_VAL_1")) { // Runtime validation errors
            addFileTypeHelp(context);
        } else if (errorCode.startsWith("EXT_VAL_2")) { // Process execution errors
            addProcessHelp(context);
        } else if (errorCode.startsWith("EXT_VAL_3")) { // API errors
            addSystemHelp(context);
        }
    }
    
    /**
     * Add file type specific help.
     */
    private void addFileTypeHelp(Map<String, String> context) {
        String allowedExtensions = context.get("allowedExtensions");
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            String helpMessage = "Allowed file types: " + allowedExtensions;
            displayMessage("help", helpMessage, "fa-question-circle");
        }
        
        // Add link to manage preferences if available
        if (managePreferencesConfigService != null && ManagePreferencesConfigService.isConfigurationAvailable()) {
            String managePrefsMessage = "You can configure allowed file types in Admin > Process Administration > Manage Preferences";
            displayMessage("help", managePrefsMessage, "fa-cog");
        }
    }
    
    /**
     * Add process execution specific help.
     */
    private void addProcessHelp(Map<String, String> context) {
        String helpMessage = "Check all document fields in your form to ensure they contain valid files";
        displayMessage("help", helpMessage, "fa-list");
    }
    
    /**
     * Add system error specific help.
     */
    private void addSystemHelp(Map<String, String> context) {
        String helpMessage = "If this problem persists, please contact your system administrator";
        displayMessage("help", helpMessage, "fa-support");
    }
    
    /**
     * Get the error container for external use.
     */
    public FlowPanel getErrorContainer() {
        return errorContainer;
    }
    
    /**
     * Check if there are any current errors.
     */
    public boolean hasErrors() {
        return errorContainer != null && errorContainer.getWidgetCount() > 0;
    }
    
    /**
     * Get the number of current error messages.
     */
    public int getErrorCount() {
        return errorContainer != null ? errorContainer.getWidgetCount() : 0;
    }
}
