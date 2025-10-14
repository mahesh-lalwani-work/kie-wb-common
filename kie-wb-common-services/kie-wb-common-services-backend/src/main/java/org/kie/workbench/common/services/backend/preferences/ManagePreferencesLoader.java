/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.services.backend.preferences;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.backend.preferences.ApplicationPreferencesLoader;
import org.jbpm.workbench.common.preferences.ManagePreferences;
import org.kie.workbench.common.services.backend.preferences.ManagePreferencesConfigService;
import org.kie.workbench.common.services.shared.preferences.ApplicationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.preferences.shared.PreferenceScope;
import org.uberfire.preferences.shared.PreferenceScopeFactory;
import org.uberfire.preferences.shared.PreferenceScopeResolutionStrategy;
import org.uberfire.preferences.shared.impl.PreferenceScopeResolutionStrategyInfo;

/**
 * Loader for Manage Preferences configuration.
 * This class bridges the gap between Manage Preferences and the configuration services.
 */
@ApplicationScoped
public class ManagePreferencesLoader implements ApplicationPreferencesLoader {

    private static final Logger log = LoggerFactory.getLogger(ManagePreferencesLoader.class);
    
    private static final String MANAGE_PREFERENCES_ALLOWED_FILE_TYPES = "org.jbpm.workbench.common.preferences.ManagePreferences.allowedFileTypes";
    
    @Inject
    private PreferenceScopeFactory scopeFactory;
    
    @Inject
    private PreferenceScopeResolutionStrategy scopeResolutionStrategy;

    @Override
    public Map<String, String> load() {
        final Map<String, String> preferences = new HashMap<String, String>();
        
        try {
            // Load Manage Preferences configuration
            ManagePreferences managePreferences = loadManagePreferences();
            
            String allowedFileTypes = null;
            
            if (managePreferences != null && managePreferences.getAllowedFileTypes() != null) {
                String value = managePreferences.getAllowedFileTypes().trim();
                
                // Check if user has explicitly configured a value (not default and not empty)
                if (!value.isEmpty()) {
                    // User has configured something - use it as-is
                    allowedFileTypes = value;
                    log.info("Loaded allowed file types from Manage Preferences: {}", allowedFileTypes);
                }
            }
            
            // Store the value
            if (allowedFileTypes != null) {
                preferences.put(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES, allowedFileTypes);
                System.setProperty(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES, allowedFileTypes);
                ManagePreferencesConfigService.initialize(allowedFileTypes);
            } else {
                // No user configuration - use defaults
                log.info("No allowed file types configured in Manage Preferences, using defaults");
                String defaults = ManagePreferences.DEFAULT_ALLOWED_FILE_TYPES;
                preferences.put(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES, defaults);
                System.setProperty(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES, defaults);
                ManagePreferencesConfigService.initialize(defaults);
            }
            
        } catch (Exception e) {
            log.error("Failed to load ManagePreferences", e);
            // On error, use defaults
            String defaults = ManagePreferences.DEFAULT_ALLOWED_FILE_TYPES;
            preferences.put(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES, defaults);
            System.setProperty(MANAGE_PREFERENCES_ALLOWED_FILE_TYPES, defaults);
            ManagePreferencesConfigService.initialize(defaults);
        }
        
        return preferences;
    }
    
    private ManagePreferences loadManagePreferences() {
        try {
            // Get the scope resolution strategy info
            PreferenceScopeResolutionStrategyInfo scopeResolutionStrategyInfo = 
                scopeResolutionStrategy.getInfo();
            
            // Create a new ManagePreferences instance
            ManagePreferences preferences = new ManagePreferences();
            
            // Load the preferences using the scope resolution strategy
            preferences.load(scopeResolutionStrategyInfo);
            
            // Debug logging
            if (log.isDebugEnabled()) {
                String loadedValue = preferences.getAllowedFileTypes();
                log.debug("ManagePreferences loaded from store - allowedFileTypes: {}", 
                    loadedValue == null ? "null" : "'" + loadedValue + "'");
            }
            
            // Return what was loaded (may have null/empty allowedFileTypes if not configured)
            return preferences;
        } catch (Exception e) {
            log.debug("Could not load ManagePreferences", e);
            return null;
        }
    }
}
