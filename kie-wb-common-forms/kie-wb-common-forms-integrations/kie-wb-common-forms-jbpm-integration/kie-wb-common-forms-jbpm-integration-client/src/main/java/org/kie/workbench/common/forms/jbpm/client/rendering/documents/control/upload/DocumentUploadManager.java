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

package org.kie.workbench.common.forms.jbpm.client.rendering.documents.control.upload;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.Blob;
import elemental2.dom.File;
import elemental2.dom.FileReader;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.forms.jbpm.client.rendering.document.config.ManagePreferencesConfigService;
import org.kie.workbench.common.forms.jbpm.service.shared.documents.DocumentUploadChunk;
import org.kie.workbench.common.forms.jbpm.service.shared.documents.DocumentUploadResponse;
import org.kie.workbench.common.forms.jbpm.service.shared.documents.UploadedDocumentService;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class DocumentUploadManager {

    public static final int MAX_CHUNK_SIZE = 1024 * 1024;

    private final Caller<UploadedDocumentService> uploadService;

    private FileReader fileReader;
    private List<UploaderSession> sessions = new ArrayList<>();
    private UploaderSession activeSession;

    @Inject
    public DocumentUploadManager(Caller<UploadedDocumentService> uploadService) {
        this.uploadService = uploadService;
    }

    public void upload(String documentId, File file, Command onUploadStart,
            ParameterizedCommand<Boolean> onUploadFinish) {
        upload(documentId, file, onUploadStart, onUploadFinish, null);
    }

    public void upload(String documentId, File file, Command onUploadStart,
            ParameterizedCommand<Boolean> onUploadFinish, String allowedExtensions) {
        upload(documentId, file, onUploadStart, onUploadFinish, allowedExtensions, null);
    }

    public void upload(String documentId, File file, Command onUploadStart,
            ParameterizedCommand<Boolean> onUploadFinish, String allowedExtensions, ParameterizedCommand<String> onValidationError) {
        // 3-Tier Hierarchical validation: Form Field -> Manage Preferences -> Default Configuration
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
                // Try to get the raw configuration which includes defaults
                String rawConfig = ManagePreferencesConfigService.getRawConfiguration();
                if (rawConfig != null && !rawConfig.trim().isEmpty()) {
                    allowedExtensions = rawConfig;
                    source = "default configuration";
                }
            }
            
            // If still no extensions configured, reject the file
            if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
                String errorMessage = "No file types are configured. Please configure allowed file extensions in Admin > Process Administration > Manage Preferences.";
                if (onValidationError != null) {
                    onValidationError.execute(errorMessage);
                }
                onUploadFinish.execute(false);
                return;
            }
        }
        
        // Validate against configured extensions
        if (!isValidFileExtension(file.name, allowedExtensions)) {
            String errorMessage = getValidationErrorMessage(file.name, allowedExtensions, source);
            if (onValidationError != null) {
                onValidationError.execute(errorMessage);
            }
            onUploadFinish.execute(false);
            return;
        }

        long fileSize = (long) file.size;

        int maxChunks = (int) (fileSize / MAX_CHUNK_SIZE);

        if (fileSize % MAX_CHUNK_SIZE > 0) {
            maxChunks++;
        }

        UploaderSession session = new UploaderSession(documentId, file, maxChunks, onUploadStart, onUploadFinish);
        sessions.add(session);

        startUpload();
    }

    public void remove(String documentId, Command callback) {
        Optional<UploaderSession> optional = sessions.stream()
                .filter(session -> session.documentId.equals(documentId))
                .findAny();
        if (optional.isPresent()) {
            sessions.remove(optional.get());
            callback.execute();
        } else {
            if (activeSession != null && activeSession.documentId.equals(documentId)) {
                fileReader.abort();
                activeSession = null;
                startUpload();
            }
            uploadService.call((RemoteCallback<Void>) response -> callback.execute())
                    .removeContent(documentId);
        }
    }

    private void startUpload() {

        if (activeSession != null || sessions.isEmpty()) {
            return;
        }

        initFileReader();

        activeSession = sessions.remove(0);
        activeSession.onUploadStart.execute();

        upload(0);
    }

    public void initFileReader() {
        fileReader = new FileReader();

        fileReader.onload = event -> {
            if (fileReader.readyState == FileReader.DONE) {
                String[] split = fileReader.result.asString().split(",");

                String content = "";

                if (split.length == 2) {
                    content = split[1];
                }

                if (activeSession == null) {
                    return null;
                }

                DocumentUploadChunk newChunk = new DocumentUploadChunk(activeSession.documentId, activeSession.file.name, activeSession.chunk, activeSession.maxChunks, content);

                uploadService.call((RemoteCallback<DocumentUploadResponse>) response -> {
                    if (response.getState().equals(DocumentUploadResponse.DocumentUploadState.FINISH)) {
                        activeSession.onUploadEnd.execute(response.isSuccess());
                        activeSession = null;
                        startUpload();
                    } else if (!response.isSuccess()) {
                        activeSession.onUploadEnd.execute(response.isSuccess());
                        activeSession = null;
                        startUpload();
                    }
                }, (ErrorCallback<Message>) (message, throwable) -> {
                    activeSession.onUploadEnd.execute(false);
                    activeSession = null;
                    startUpload();
                    return false;
                }).uploadContent(newChunk);

                if (activeSession.nextChunk <= activeSession.file.size) {
                    upload(activeSession.nextChunk);
                }
            }
            return null;
        };
    }

    private void upload(final int sliceStart) {

        activeSession.chunk = sliceStart;

        activeSession.nextChunk = sliceStart + MAX_CHUNK_SIZE + 1;

        Blob fileSlice = activeSession.file.slice(activeSession.chunk, activeSession.nextChunk);

        fileReader.readAsDataURL(fileSlice);
    }

    /**
     * Validates if a file extension is allowed.
     * This is a basic client-side validation that checks against common file
     * extensions.
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

    /**
     * Generates an error message for file extension validation failures.
     * 
     * @param fileName the name of the file that failed validation
     * @param allowedExtensions comma-separated list of allowed extensions
     * @return an error message
     */
    private String getValidationErrorMessage(String fileName, String allowedExtensions) {
        return getValidationErrorMessage(fileName, allowedExtensions, "configuration");
    }
    
    /**
     * Generates an error message for file extension validation failures with source information.
     * 
     * @param fileName the name of the file that failed validation
     * @param allowedExtensions comma-separated list of allowed extensions
     * @param source the source of the allowed extensions configuration
     * @return an error message
     */
    private String getValidationErrorMessage(String fileName, String allowedExtensions, String source) {
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return "File must have a valid extension";
        }
        
        return "File extension '" + extension + "' is not allowed. Allowed types (" + source + "): " + allowedExtensions;
    }

    private class UploaderSession {

        private final String documentId;
        private final File file;
        private final int maxChunks;
        private final Command onUploadStart;
        private final ParameterizedCommand<Boolean> onUploadEnd;

        private int chunk = 0;
        private int nextChunk;

        public UploaderSession(String documentId, File file, int maxChunks, Command onUploadStart, ParameterizedCommand<Boolean> onUploadEnd) {
            this.documentId = documentId;
            this.file = file;
            this.maxChunks = maxChunks;
            this.onUploadStart = onUploadStart;
            this.onUploadEnd = onUploadEnd;
        }
    }
    
}
