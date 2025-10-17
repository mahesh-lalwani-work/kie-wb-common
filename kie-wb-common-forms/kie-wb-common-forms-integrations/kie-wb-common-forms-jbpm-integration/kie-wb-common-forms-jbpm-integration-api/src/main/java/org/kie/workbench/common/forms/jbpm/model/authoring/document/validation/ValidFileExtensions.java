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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation for file extensions that provides user-friendly error messages.
 * 
 * <p>This annotation validates that file extension strings contain only valid extensions
 * from the allowed list configured in Manage Preferences. It provides structured error
 * messages with typo suggestions and helpful guidance.
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * @ValidFileExtensions
 * private String enabledFileExtensions;
 * }
 * </pre>
 * 
 * <p>The validation is performed by {@link FileExtensionsValidator} which uses
 * the shared validation logic from {@link org.jbpm.workbench.common.preferences.FileExtensionsValidationUtil}.
 * 
 * @since 7.74.1
 * @see FileExtensionsValidator
 * @see org.jbpm.workbench.common.preferences.FileExtensionsValidationUtil
 */
@Documented
@Constraint(validatedBy = FileExtensionsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileExtensions {
    
    String message() default "Invalid file extension(s). Please use only allowed extensions.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
