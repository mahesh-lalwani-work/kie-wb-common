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

package org.kie.workbench.common.forms.jbpm.model.authoring.documents.definition;

import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.i18n.I18nSettings;
import org.kie.workbench.common.forms.fields.shared.AbstractFieldDefinition;
import org.kie.workbench.common.forms.jbpm.model.authoring.document.validation.ValidFileExtensions;
import org.kie.workbench.common.forms.jbpm.model.authoring.documents.type.DocumentCollectionFieldType;
import org.kie.workbench.common.forms.model.FieldDefinition;

/**
 * Field definition for Document Collection fields in the Form Designer.
 * 
 * <p>This class defines the properties and validation rules for Document Collection fields
 * that can be added to forms. Document Collection fields allow users to upload multiple
 * files with specific file type restrictions and quantity limits.
 * 
 * <p>Key features:
 * <ul>
 *   <li><strong>Multiple file uploads:</strong> Allows users to upload multiple documents</li>
 *   <li><strong>Quantity limits:</strong> The {@code maxDocuments} field controls how many
 *       files can be uploaded</li>
 *   <li><strong>File type restrictions:</strong> The {@code enabledFileExtensions} field
 *       allows specifying which file types are allowed for upload</li>
 *   <li><strong>Validation:</strong> Uses {@link ValidFileExtensions} annotation to ensure
 *       only valid file extensions are configured</li>
 *   <li><strong>Form integration:</strong> Integrates with the Form Designer UI for
 *       property configuration</li>
 * </ul>
 * 
 * <p>The {@code enabledFileExtensions} field accepts comma-separated file extensions
 * (e.g., "pdf,txt,jpg") and validates them against the allowed list configured
 * in Manage Preferences.
 * 
 * @since 7.74.1
 * @see ValidFileExtensions
 * @see org.jbpm.workbench.common.preferences.FileExtensionsValidationUtil
 */
@Portable
@Bindable
@FormDefinition(
        i18n = @I18nSettings(keyPreffix = "FieldProperties"),
        startElement = "label"
)
public class DocumentCollectionFieldDefinition extends AbstractFieldDefinition {

    public static final DocumentCollectionFieldType FIELD_TYPE = new DocumentCollectionFieldType();

    @FormField(
            labelKey = "maxDocuments",
            helpMessageKey = "maxDocuments.helpMessage",
            afterElement = "label"
    )
    private Integer maxDocuments = 0;

    /**
     * Comma-separated list of allowed file extensions for document collection uploads.
     * 
     * <p>This field specifies which file types are allowed when users upload
     * documents through this collection field. The value should be a comma-separated list
     * of file extensions without dots (e.g., "pdf,txt,jpg").
     * 
     * <p>If this field is empty or null, the global file type restrictions
     * from Manage Preferences will be used instead.
     * 
     * <p>The validation is performed by the {@link ValidFileExtensions} annotation
     * which ensures only valid extensions from the allowed list are specified.
     */
    @FormField(
            labelKey = "enabledFileExtensions",
            helpMessageKey = "enabledFileExtensions.helpMessage",
            afterElement = "maxDocuments",
            required = false
    )
    @ValidFileExtensions
    private String enabledFileExtensions;

    public DocumentCollectionFieldDefinition() {
        super(DocumentCollectionFieldType.DOCUMENT_COLLECTION_TYPE);
    }

    public Integer getMaxDocuments() {
        return maxDocuments;
    }

    public void setMaxDocuments(Integer maxDocuments) {
        this.maxDocuments = maxDocuments;
    }

    public String getEnabledFileExtensions() {
        return enabledFileExtensions;
    }

    public void setEnabledFileExtensions(String enabledFileExtensions) {
        this.enabledFileExtensions = enabledFileExtensions;
    }

    @Override
    public DocumentCollectionFieldType getFieldType() {
        return FIELD_TYPE;
    }

    @Override
    protected void doCopyFrom(FieldDefinition other) {
        if (other instanceof DocumentCollectionFieldDefinition) {
            DocumentCollectionFieldDefinition otherDoc = (DocumentCollectionFieldDefinition) other;
            this.maxDocuments = otherDoc.maxDocuments;
            this.enabledFileExtensions = otherDoc.enabledFileExtensions;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DocumentCollectionFieldDefinition that = (DocumentCollectionFieldDefinition) o;
        return Objects.equals(maxDocuments, that.maxDocuments) &&
                Objects.equals(enabledFileExtensions, that.enabledFileExtensions);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (maxDocuments != null ? maxDocuments.hashCode() : 0);
        result = 31 * result + (enabledFileExtensions != null ? enabledFileExtensions.hashCode() : 0);
        return result;
    }
}
