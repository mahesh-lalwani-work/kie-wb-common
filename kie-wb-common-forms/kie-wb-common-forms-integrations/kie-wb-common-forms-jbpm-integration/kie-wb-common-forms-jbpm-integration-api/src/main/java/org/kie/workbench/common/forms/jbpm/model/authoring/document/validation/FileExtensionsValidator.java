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

package org.kie.workbench.common.forms.jbpm.model.authoring.document.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * JSR-303 validator for file extensions that provides user-friendly error messages
 * instead of throwing exceptions during property binding.
 * 
 * <p>This validator implements the {@link ConstraintValidator} interface to provide
 * form validation for file extension fields. It validates extensions against a
 * comprehensive allowed list.
 * 
 * <p>The validator:
 * <ul>
 *   <li>Allows null or empty values (optional fields)</li>
 *   <li>Validates file extensions against the allowed list</li>
 *   <li>Provides detailed error messages</li>
 *   <li>Integrates with the form framework's validation system</li>
 * </ul>
 * 
 * <p>This validator is used by the {@link ValidFileExtensions} annotation to validate
 * the {@code enabledFileExtensions} field in Document field definitions.
 * 
 * @since 7.74.1
 * @see ValidFileExtensions
 */
public class FileExtensionsValidator implements ConstraintValidator<ValidFileExtensions, String> {

    /**
     * Comprehensive list of allowed file extensions for document upload.
     * This list is synchronized with the validation used in runtime file upload.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        // Document formats
        "pdf", "doc", "docx", "rtf", "txt", "odt", "pages",
        // Spreadsheet formats
        "xls", "xlsx", "csv", "ods", "numbers",
        // Presentation formats
        "ppt", "pptx", "odp", "key",
        // Image formats
        "jpg", "jpeg", "png", "gif", "bmp", "svg", "tiff", "tif", "webp", "ico",
        // Archive formats
        "zip", "rar", "7z", "tar", "gz", "bz2", "xz",
        // Code/markup formats
        "html", "htm", "xml", "json", "yaml", "yml", "md", "css", "js", "java", "py", "cpp", "c", "h",
        // Data formats
        "sql", "db", "mdb", "accdb",
        // Media formats
        "mp3", "mp4", "avi", "mov", "wmv", "flv", "wav", "ogg", "webm",
        // Other common formats
        "log", "ini", "cfg", "conf", "properties"
    ));

    @Override
    public void initialize(ValidFileExtensions constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null or empty values
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Split and validate each extension
        String[] extensions = value.split(",");
        StringBuilder invalidExtensions = new StringBuilder();
        
        for (String ext : extensions) {
            String trimmedExt = ext.trim().toLowerCase();
            if (!trimmedExt.isEmpty() && !ALLOWED_EXTENSIONS.contains(trimmedExt)) {
                if (invalidExtensions.length() > 0) {
                    invalidExtensions.append(", ");
                }
                invalidExtensions.append(trimmedExt);
            }
        }

        if (invalidExtensions.length() > 0) {
            // Disable default constraint violation
            context.disableDefaultConstraintViolation();
            
            // Build error message (GWT-compatible - no String.format)
            String errorMessage = "Invalid file extension(s): " + invalidExtensions.toString() + 
                                  ". Valid extensions include: " + getValidExtensionsSample();
            
            // Add custom error message
            context.buildConstraintViolationWithTemplate(errorMessage)
                   .addConstraintViolation();
            
            return false;
        }

        return true;
    }

    /**
     * Returns a sample of valid extensions for error messages.
     */
    private String getValidExtensionsSample() {
        return "pdf, doc, docx, rtf, txt, odt, xls, xlsx, csv, jpg, png, and " + 
               (ALLOWED_EXTENSIONS.size() - 11) + " more";
    }
}
