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

package org.kie.workbench.common.services.backend.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Backend service for managing file extension configuration from Manage Preferences.
 * This service provides functionality to read allowed file extensions
 * from the Business Central Manage Preferences configuration.
 */
public class ManagePreferencesConfigService {

    private static Set<String> allowedExtensions;

    /**
     * Initialize the service with a specific configuration value.
     * This method can be called when the configuration is read from Manage
     * Preferences.
     * 
     * @param allowedFileTypes the comma-separated list of allowed file types
     */
    public static void initialize(String allowedFileTypes) {
        if (allowedFileTypes == null || allowedFileTypes.trim().isEmpty()) {
            allowedExtensions = Collections.emptySet();
            return;
        }

        allowedExtensions = new HashSet<>();
        String[] extensions = allowedFileTypes.split(",");
        for (String ext : extensions) {
            allowedExtensions.add(ext.trim().toLowerCase());
        }
    }

    /**
     * Get the set of allowed file extensions from Manage Preferences.
     * 
     * @return set of allowed file extensions (lowercase)
     */
    public static Set<String> getAllowedExtensions() {
        return allowedExtensions != null ? allowedExtensions : Collections.emptySet();
    }

    /**
     * Check if a file extension is allowed according to Manage Preferences.
     * 
     * @param extension the file extension to check (case insensitive)
     * @return true if the extension is allowed, false otherwise
     */
    public static boolean isExtensionAllowed(String extension) {
        if (extension == null) {
            return false;
        }
        return getAllowedExtensions().contains(extension.toLowerCase().trim());
    }

    /**
     * Get the list of allowed file extensions as a list.
     * 
     * @return list of allowed file extensions
     */
    public static List<String> getAllowedExtensionsList() {
        return Arrays.asList(getAllowedExtensions().toArray(new String[0]));
    }

    /**
     * Check if Manage Preferences configuration is available.
     * 
     * @return true if configuration is available, false otherwise
     */
    public static boolean isConfigurationAvailable() {
        return allowedExtensions != null && !allowedExtensions.isEmpty();
    }
}
