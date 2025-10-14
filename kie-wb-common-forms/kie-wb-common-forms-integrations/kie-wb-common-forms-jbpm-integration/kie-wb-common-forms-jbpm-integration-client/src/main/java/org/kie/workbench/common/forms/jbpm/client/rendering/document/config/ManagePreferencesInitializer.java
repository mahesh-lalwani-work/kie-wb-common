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

package org.kie.workbench.common.forms.jbpm.client.rendering.document.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.kie.workbench.common.services.shared.preferences.ApplicationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.services.cdi.Startup;

/**
 * Initializer for Manage Preferences configuration in Business Central.
 * This class loads the Manage Preferences configuration and initializes the
 * configuration service.
 * 
 * This initializer runs at application startup and loads the configuration
 * from ApplicationPreferences which is populated by ManagePreferencesLoader
 * on the server side.
 */
@ApplicationScoped
@Startup
public class ManagePreferencesInitializer {

    private static final Logger log = LoggerFactory.getLogger(ManagePreferencesInitializer.class);
    private static final String MANAGE_PREFERENCES_ALLOWED_FILE_TYPES = "org.jbpm.workbench.common.preferences.ManagePreferences.allowedFileTypes";
    private static final String DEFAULT_EXTENSIONS = "pdf,docx,xlsx,txt,jpg,png";

    @PostConstruct
    public void initialize() {
        try {
            // Read the Manage Preferences data from ApplicationPreferences
            // This is populated by ManagePreferencesLoader on the server side
            // ManagePreferencesLoader always sets a value (either user config or defaults)
            String allowedFileTypes = ApplicationPreferences.getStringPref(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES);
            
            if (allowedFileTypes != null && !allowedFileTypes.trim().isEmpty()) {
                allowedFileTypes = allowedFileTypes.trim();
                log.info("Initialized document upload validation with allowed file types: {}", allowedFileTypes);
                ManagePreferencesConfigService.initialize(allowedFileTypes);
            } else {
                // Fallback - should not normally happen since ManagePreferencesLoader sets defaults
                log.warn("No allowed file types in ApplicationPreferences, using hardcoded defaults: {}", DEFAULT_EXTENSIONS);
                ManagePreferencesConfigService.initialize(DEFAULT_EXTENSIONS);
            }
            
        } catch (Exception e) {
            log.error("Failed to initialize document upload validation", e);
            // On error, fall back to defaults
            ManagePreferencesConfigService.initialize(DEFAULT_EXTENSIONS);
        }
    }
    
    /**
     * Refresh the configuration from ApplicationPreferences.
     * This method can be called when preferences are updated to reload the configuration.
     */
    public void refresh() {
        log.info("Refreshing document upload validation configuration");
        initialize();
    }
}
