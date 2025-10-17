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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.uberfire.workbench.events.NotificationEvent;

/**
 * Integration service for connecting backend validation errors with frontend error handling.
 * 
 * <p>This service provides integration between the backend validation services and the
 * frontend error handling system, ensuring that validation errors are properly displayed
 * to users with appropriate context and user-friendly messages.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Backend error code mapping to frontend messages</li>
 *   <li>Context-aware error display</li>
 *   <li>Integration with form field validation</li>
 *   <li>Process and task execution error handling</li>
 *   <li>Real-time error feedback</li>
 * </ul>
 * 
 * @author jBPM Team
 * @since 7.74.1
 */
@ApplicationScoped
public class ValidationErrorIntegrationService {
    
    private final FileExtensionErrorHandler errorHandler;
    
    @Inject
    public ValidationErrorIntegrationService(final FileExtensionErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    /**
     * Handle validation error from backend validation service.
     * 
     * @param errorCode the error code from backend validation
     * @param technicalMessage the technical error message
     * @param context the error context
     */
    public void handleBackendValidationError(String errorCode, String technicalMessage, FileExtensionErrorHandler.ErrorContext context) {
        // Map backend error codes to frontend error codes
        String frontendErrorCode = mapBackendToFrontendErrorCode(errorCode);
        
        // Handle the error with appropriate user-friendly message
        errorHandler.handleError(frontendErrorCode, technicalMessage, context);
    }
    
    /**
     * Handle validation error from backend validation service with default context.
     * 
     * @param errorCode the error code from backend validation
     * @param technicalMessage the technical error message
     */
    public void handleBackendValidationError(String errorCode, String technicalMessage) {
        handleBackendValidationError(errorCode, technicalMessage, new FileExtensionErrorHandler.ErrorContext());
    }
    
    /**
     * Handle form field validation error.
     * 
     * @param fieldName the name of the form field
     * @param errorCode the error code
     * @param technicalMessage the technical error message
     * @param allowedExtensions the allowed extensions for context
     */
    public void handleFormFieldValidationError(String fieldName, String errorCode, String technicalMessage, String allowedExtensions) {
        FileExtensionErrorHandler.ErrorContext context = new FileExtensionErrorHandler.ErrorContext()
            .withFormFieldName(fieldName)
            .withAllowedExtensions(allowedExtensions);
        
        handleBackendValidationError(errorCode, technicalMessage, context);
    }
    
    /**
     * Handle process start validation error.
     * 
     * @param processId the process ID
     * @param containerId the container ID
     * @param errorCode the error code
     * @param technicalMessage the technical error message
     */
    public void handleProcessStartValidationError(String processId, String containerId, String errorCode, String technicalMessage) {
        FileExtensionErrorHandler.ErrorContext context = new FileExtensionErrorHandler.ErrorContext()
            .withProcessId(processId)
            .withContainerId(containerId);
        
        handleBackendValidationError(errorCode, technicalMessage, context);
    }
    
    /**
     * Handle task completion validation error.
     * 
     * @param taskId the task ID
     * @param containerId the container ID
     * @param errorCode the error code
     * @param technicalMessage the technical error message
     */
    public void handleTaskCompletionValidationError(String taskId, String containerId, String errorCode, String technicalMessage) {
        FileExtensionErrorHandler.ErrorContext context = new FileExtensionErrorHandler.ErrorContext()
            .withTaskId(taskId)
            .withContainerId(containerId);
        
        handleBackendValidationError(errorCode, technicalMessage, context);
    }
    
    /**
     * Handle file upload validation error.
     * 
     * @param fileName the name of the file that failed validation
     * @param errorCode the error code
     * @param technicalMessage the technical error message
     * @param allowedExtensions the allowed extensions for context
     */
    public void handleFileUploadValidationError(String fileName, String errorCode, String technicalMessage, String allowedExtensions) {
        FileExtensionErrorHandler.ErrorContext context = new FileExtensionErrorHandler.ErrorContext()
            .withFileName(fileName)
            .withAllowedExtensions(allowedExtensions);
        
        handleBackendValidationError(errorCode, technicalMessage, context);
    }
    
    /**
     * Handle design-time validation error in form designer.
     * 
     * @param errorCode the error code
     * @param technicalMessage the technical error message
     * @param allowedExtensions the allowed extensions for context
     */
    public void handleDesignTimeValidationError(String errorCode, String technicalMessage, String allowedExtensions) {
        FileExtensionErrorHandler.ErrorContext context = new FileExtensionErrorHandler.ErrorContext()
            .withAllowedExtensions(allowedExtensions);
        
        handleBackendValidationError(errorCode, technicalMessage, context);
    }
    
    /**
     * Show validation success message.
     * 
     * @param fileName the name of the successfully validated file
     * @param allowedExtensions the allowed extensions for context
     */
    public void showValidationSuccess(String fileName, String allowedExtensions) {
        errorHandler.showSuccess(fileName, allowedExtensions);
    }
    
    /**
     * Show validation warning message.
     * 
     * @param warningCode the warning code
     * @param warningMessage the warning message
     */
    public void showValidationWarning(String warningCode, String warningMessage) {
        errorHandler.showWarning(warningCode, warningMessage);
    }
    
    /**
     * Show validation info message.
     * 
     * @param infoCode the info code
     * @param infoMessage the info message
     */
    public void showValidationInfo(String infoCode, String infoMessage) {
        errorHandler.showInfo(infoCode, infoMessage);
    }
    
    /**
     * Map backend error codes to frontend error codes.
     * 
     * @param backendErrorCode the backend error code
     * @return corresponding frontend error code
     */
    private String mapBackendToFrontendErrorCode(String backendErrorCode) {
        // Backend error codes should already be in the correct format
        // This method can be used for any necessary mapping or transformation
        return backendErrorCode;
    }
    
    /**
     * Handle generic validation error with custom message.
     * 
     * @param message the custom error message
     * @param type the notification type
     */
    public void handleGenericValidationError(String message, NotificationEvent.NotificationType type) {
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
     * Handle validation error with custom context.
     * 
     * @param errorCode the error code
     * @param technicalMessage the technical error message
     * @param customContext the custom error context
     */
    public void handleValidationErrorWithCustomContext(String errorCode, String technicalMessage, FileExtensionErrorHandler.ErrorContext customContext) {
        handleBackendValidationError(errorCode, technicalMessage, customContext);
    }
}