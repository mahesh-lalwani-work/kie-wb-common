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

package org.kie.workbench.common.forms.jbpm.client.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jbpm.workbench.common.preferences.ManagePreferences;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.workbench.model.CompassPosition;

/**
 * Service for form designer validation integration.
 * 
 * <p>This service provides integration between the form designer UI and the
 * backend validation services, enabling real-time validation of file extension
 * configurations in the form designer.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Real-time validation of enabledFileExtensions input</li>
 *   <li>Integration with Manage Preferences for master allowed list</li>
 *   <li>Client-side validation with immediate feedback</li>
 *   <li>Error message localization</li>
 * </ul>
 * 
 * @author jBPM Team
 * @since 7.74.1
 */
@ApplicationScoped
@WorkbenchScreen(identifier = "FormDesignerValidationService")
public class FormDesignerValidationService {
    
    private final View view;
    private final ManagePreferences managePreferences;
    
    @Inject
    public FormDesignerValidationService(final View view,
                                        final ManagePreferences managePreferences) {
        this.view = view;
        this.managePreferences = managePreferences;
    }
    
    @WorkbenchPartView
    public View getView() {
        return view;
    }
    
    @WorkbenchPartTitle
    public String getTitle() {
        return "Form Designer Validation";
    }
    
    @DefaultPosition
    public CompassPosition getDefaultPosition() {
        return CompassPosition.EAST;
    }
    
    /**
     * Initialize form designer validation with current configuration.
     * 
     * <p>This method loads the current Manage Preferences configuration
     * and initializes the client-side validation system.
     */
    public void initializeValidation() {
        managePreferences.load(prefs -> {
            String allowedFileTypes = prefs.getAllowedFileTypes();
            view.initializeValidation(allowedFileTypes);
        }, error -> {
            view.showError("Failed to load validation configuration: " + error.getMessage());
        });
    }
    
    /**
     * Validate enabledFileExtensions input in real-time.
     * 
     * @param extensions the extensions string to validate
     * @return validation result with status and message
     */
    public ValidationResult validateExtensions(String extensions) {
        if (extensions == null || extensions.trim().isEmpty()) {
            return ValidationResult.valid("Empty extensions - will use fallback configuration");
        }
        
        // Basic format validation
        if (extensions.contains(". ")) {
            return ValidationResult.error("Invalid format: Remove spaces around commas");
        }
        
        if (extensions.contains(".")) {
            return ValidationResult.error("Invalid format: Remove dots from extensions (use 'pdf' not '.pdf')");
        }
        
        // Check for empty extensions
        String[] extArray = extensions.split(",");
        for (String ext : extArray) {
            if (ext.trim().isEmpty()) {
                return ValidationResult.error("Invalid format: Remove empty entries");
            }
        }
        
        return ValidationResult.valid("Valid format");
    }
    
    /**
     * Get the current master allowed extensions for validation.
     * 
     * @return comma-separated list of master allowed extensions
     */
    public String getMasterAllowedExtensions() {
        // This would typically be called from the client-side JavaScript
        // after the ManagePreferences has been loaded
        return managePreferences.getAllowedFileTypes();
    }
    
    /**
     * Validation result class for form designer validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String severity;
        
        private ValidationResult(boolean valid, String message, String severity) {
            this.valid = valid;
            this.message = message;
            this.severity = severity;
        }
        
        public static ValidationResult valid(String message) {
            return new ValidationResult(true, message, "success");
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, "error");
        }
        
        public static ValidationResult warning(String message) {
            return new ValidationResult(true, message, "warning");
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getSeverity() {
            return severity;
        }
    }
    
    /**
     * View interface for the form designer validation service.
     */
    public interface View extends UberElement<FormDesignerValidationService> {
        
        /**
         * Initialize client-side validation with master allowed extensions.
         * 
         * @param masterAllowedExtensions comma-separated list of master allowed extensions
         */
        void initializeValidation(String masterAllowedExtensions);
        
        /**
         * Show error message to user.
         * 
         * @param message error message to display
         */
        void showError(String message);
        
        /**
         * Show success message to user.
         * 
         * @param message success message to display
         */
        void showSuccess(String message);
    }
}
