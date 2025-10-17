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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.jbpm.workbench.common.preferences.ManagePreferences;
import org.uberfire.workbench.events.NotificationEvent;

/**
 * Error handler for file extension validation errors with user-friendly messages.
 * 
 * <p>This service provides comprehensive error handling for file extension validation,
 * converting technical error codes into user-friendly messages and displaying them
 * through the appropriate UI channels.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Error code to user message mapping</li>
 *   <li>Context-aware error messages</li>
 *   <li>Multiple display channels (notifications, inline messages, alerts)</li>
 *   <li>Localized error messages</li>
 *   <li>Integration with Manage Preferences for configuration context</li>
 * </ul>
 * 
 * @author jBPM Team
 * @since 7.74.1
 */
@ApplicationScoped
public class FileExtensionErrorHandler {
    
    private final ManagePreferences managePreferences;
    private final Map<String, String> errorCodeMessages;
    
    @Inject
    public FileExtensionErrorHandler(final ManagePreferences managePreferences) {
        this.managePreferences = managePreferences;
        this.errorCodeMessages = initializeErrorMessages();
    }
    
    /**
     * Initialize error code to user message mapping.
     * 
     * @return map of error codes to user-friendly messages
     */
    private Map<String, String> initializeErrorMessages() {
        Map<String, String> messages = new HashMap<>();
        
        // Design-time errors
        messages.put("EXT_VAL_001", "Invalid format for file extensions. Use comma-separated values without spaces or dots (e.g., 'pdf,docx,xlsx')");
        messages.put("EXT_VAL_002", "Some extensions are not in the master allowed list. Please check your configuration.");
        messages.put("EXT_VAL_003", "No master allowed extensions configured. Please contact your administrator.");
        messages.put("EXT_VAL_004", "Duplicate extensions found. Please remove duplicates.");
        
        // Runtime errors
        messages.put("EXT_VAL_101", "File extension is not allowed. Please select a different file.");
        messages.put("EXT_VAL_102", "Some files have invalid extensions. Please check all selected files.");
        messages.put("EXT_VAL_103", "File type configuration is not available. Please contact your administrator.");
        messages.put("EXT_VAL_104", "Maximum number of files exceeded. Please select fewer files.");
        messages.put("EXT_VAL_105", "File must have a valid extension.");
        messages.put("EXT_VAL_106", "Case mismatch in file extension. Please use lowercase extensions.");
        
        // Process execution errors
        messages.put("EXT_VAL_201", "Process start failed due to invalid file extensions.");
        messages.put("EXT_VAL_202", "Task completion failed due to invalid file extensions.");
        messages.put("EXT_VAL_203", "Error during process start validation. Please try again.");
        messages.put("EXT_VAL_204", "Error during task completion validation. Please try again.");
        
        // API errors
        messages.put("EXT_VAL_301", "Form content validation failed. Please check your file selections.");
        messages.put("EXT_VAL_302", "System error during validation. Please contact your administrator.");
        messages.put("EXT_VAL_303", "Configuration refresh error. Please try again later.");
        
        return messages;
    }
    
    /**
     * Handle file extension validation error with user-friendly message.
     * 
     * @param errorCode the error code from validation
     * @param technicalMessage the technical error message
     * @param context additional context information
     */
    public void handleError(String errorCode, String technicalMessage, ErrorContext context) {
        String userMessage = getUserFriendlyMessage(errorCode, technicalMessage, context);
        
        // Determine notification type based on error severity
        NotificationEvent.NotificationType notificationType = getNotificationType(errorCode);
        
        // Show notification
        showNotification(userMessage, notificationType);
        
        // Log technical details for debugging
        logTechnicalError(errorCode, technicalMessage, context);
    }
    
    /**
     * Handle file extension validation error with default context.
     * 
     * @param errorCode the error code from validation
     * @param technicalMessage the technical error message
     */
    public void handleError(String errorCode, String technicalMessage) {
        handleError(errorCode, technicalMessage, new ErrorContext());
    }
    
    /**
     * Get user-friendly message for error code.
     * 
     * @param errorCode the error code
     * @param technicalMessage the technical message
     * @param context the error context
     * @return user-friendly message
     */
    private String getUserFriendlyMessage(String errorCode, String technicalMessage, ErrorContext context) {
        String baseMessage = errorCodeMessages.get(errorCode);
        
        if (baseMessage == null) {
            baseMessage = "An error occurred during file validation. Please try again.";
        }
        
        // Add context-specific information
        if (context.hasAllowedExtensions()) {
            baseMessage += " Allowed file types: " + context.getAllowedExtensions();
        }
        
        if (context.hasFileName()) {
            baseMessage += " File: " + context.getFileName();
        }
        
        return baseMessage;
    }
    
    /**
     * Determine notification type based on error code.
     * 
     * @param errorCode the error code
     * @return appropriate notification type
     */
    private NotificationEvent.NotificationType getNotificationType(String errorCode) {
        if (errorCode.startsWith("EXT_VAL_001") || errorCode.startsWith("EXT_VAL_002")) {
            return NotificationEvent.NotificationType.ERROR;
        } else if (errorCode.startsWith("EXT_VAL_004") || errorCode.startsWith("EXT_VAL_106")) {
            return NotificationEvent.NotificationType.WARNING;
        } else if (errorCode.startsWith("EXT_VAL_003") || errorCode.startsWith("EXT_VAL_103")) {
            return NotificationEvent.NotificationType.INFO;
        } else {
            return NotificationEvent.NotificationType.ERROR;
        }
    }
    
    /**
     * Show notification to user.
     * 
     * @param message the message to display
     * @param type the notification type
     */
    private void showNotification(String message, NotificationEvent.NotificationType type) {
        // Log the notification message
        GWT.log("[" + type + "] " + message);
        
        // Display alert for user feedback
        showAlert(message);
    }
    
    /**
     * Native JavaScript method to show alert.
     * 
     * @param message the message to display
     */
    private native void showAlert(String message) /*-{
        if (typeof $wnd.alert !== 'undefined') {
            $wnd.alert(message);
        }
    }-*/;
    
    /**
     * Log technical error details for debugging.
     * 
     * @param errorCode the error code
     * @param technicalMessage the technical message
     * @param context the error context
     */
    private void logTechnicalError(String errorCode, String technicalMessage, ErrorContext context) {
        // Log technical details for debugging
        System.err.println("File Extension Validation Error:");
        System.err.println("  Code: " + errorCode);
        System.err.println("  Technical Message: " + technicalMessage);
        System.err.println("  Context: " + context.toString());
    }
    
    /**
     * Show success message for valid file uploads.
     * 
     * @param fileName the name of the successfully validated file
     * @param allowedExtensions the allowed extensions for context
     */
    public void showSuccess(String fileName, String allowedExtensions) {
        String message = "File '" + fileName + "' uploaded successfully.";
        if (allowedExtensions != null && !allowedExtensions.trim().isEmpty()) {
            message += " Allowed types: " + allowedExtensions;
        }
        
        GWT.log("[SUCCESS] " + message);
    }
    
    /**
     * Show warning message for validation warnings.
     * 
     * @param warningCode the warning code
     * @param warningMessage the warning message
     */
    public void showWarning(String warningCode, String warningMessage) {
        String userMessage = errorCodeMessages.getOrDefault(warningCode, warningMessage);
        showNotification(userMessage, NotificationEvent.NotificationType.WARNING);
    }
    
    /**
     * Show info message for validation information.
     * 
     * @param infoCode the info code
     * @param infoMessage the info message
     */
    public void showInfo(String infoCode, String infoMessage) {
        String userMessage = errorCodeMessages.getOrDefault(infoCode, infoMessage);
        showNotification(userMessage, NotificationEvent.NotificationType.INFO);
    }
    
    /**
     * Error context class for providing additional context information.
     */
    public static class ErrorContext {
        private String fileName;
        private String allowedExtensions;
        private String formFieldName;
        private String containerId;
        private String processId;
        private String taskId;
        
        public ErrorContext() {
            // Default constructor
        }
        
        public ErrorContext withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public ErrorContext withAllowedExtensions(String allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
            return this;
        }
        
        public ErrorContext withFormFieldName(String formFieldName) {
            this.formFieldName = formFieldName;
            return this;
        }
        
        public ErrorContext withContainerId(String containerId) {
            this.containerId = containerId;
            return this;
        }
        
        public ErrorContext withProcessId(String processId) {
            this.processId = processId;
            return this;
        }
        
        public ErrorContext withTaskId(String taskId) {
            this.taskId = taskId;
            return this;
        }
        
        public boolean hasFileName() {
            return fileName != null && !fileName.trim().isEmpty();
        }
        
        public boolean hasAllowedExtensions() {
            return allowedExtensions != null && !allowedExtensions.trim().isEmpty();
        }
        
        public boolean hasFormFieldName() {
            return formFieldName != null && !formFieldName.trim().isEmpty();
        }
        
        public boolean hasContainerId() {
            return containerId != null && !containerId.trim().isEmpty();
        }
        
        public boolean hasProcessId() {
            return processId != null && !processId.trim().isEmpty();
        }
        
        public boolean hasTaskId() {
            return taskId != null && !taskId.trim().isEmpty();
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getAllowedExtensions() {
            return allowedExtensions;
        }
        
        public String getFormFieldName() {
            return formFieldName;
        }
        
        public String getContainerId() {
            return containerId;
        }
        
        public String getProcessId() {
            return processId;
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ErrorContext{");
            if (hasFileName()) {
                sb.append("fileName='").append(fileName).append("', ");
            }
            if (hasAllowedExtensions()) {
                sb.append("allowedExtensions='").append(allowedExtensions).append("', ");
            }
            if (hasFormFieldName()) {
                sb.append("formFieldName='").append(formFieldName).append("', ");
            }
            if (hasContainerId()) {
                sb.append("containerId='").append(containerId).append("', ");
            }
            if (hasProcessId()) {
                sb.append("processId='").append(processId).append("', ");
            }
            if (hasTaskId()) {
                sb.append("taskId='").append(taskId).append("', ");
            }
            sb.append("}");
            return sb.toString();
        }
    }
}
