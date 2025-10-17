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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Implementation of the form designer validation service view.
 * 
 * <p>This view provides the UI components and JavaScript integration
 * for real-time validation in the form designer.
 * 
 * @author jBPM Team
 * @since 7.74.1
 */
@Templated
public class FormDesignerValidationServiceImpl implements FormDesignerValidationService.View, IsElement {
    
    @Inject
    @DataField
    private com.google.gwt.user.client.ui.FlowPanel validationContainer;
    
    @Inject
    @DataField
    private HTMLElement root;
    
    private FormDesignerValidationService presenter;
    
    @PostConstruct
    public void init() {
        // Initialize the validation container
        validationContainer.getElement().setId("form-designer-validation-container");
    }
    
    @Override
    public void init(final FormDesignerValidationService presenter) {
        this.presenter = presenter;
    }
    
    @Override
    public HTMLElement getElement() {
        return root;
    }
    
    @Override
    public void initializeValidation(String masterAllowedExtensions) {
        // Initialize client-side validation with master allowed extensions
        GWT.log("Initializing form designer validation with master extensions: " + masterAllowedExtensions);
        
        // Call JavaScript function to initialize validation
        initializeFormDesignerValidation(masterAllowedExtensions);
    }
    
    @Override
    public void showError(String message) {
        GWT.log("Form Designer Validation Error: " + message);
        showValidationMessage(message, "error");
    }
    
    @Override
    public void showSuccess(String message) {
        GWT.log("Form Designer Validation Success: " + message);
        showValidationMessage(message, "success");
    }
    
    /**
     * Show validation message in the UI.
     * 
     * @param message the message to display
     * @param type the message type (error, success, warning)
     */
    private void showValidationMessage(String message, String type) {
        // This would typically update the UI to show the validation message
        // For now, we'll use GWT.log for demonstration
        GWT.log("Validation Message [" + type + "]: " + message);
    }
    
    /**
     * Native JavaScript method to initialize form designer validation.
     * 
     * @param masterAllowedExtensions comma-separated list of master allowed extensions
     */
    private native void initializeFormDesignerValidation(String masterAllowedExtensions) /*-{
        if (typeof $wnd.appformer !== 'undefined' && 
            typeof $wnd.appformer.forms !== 'undefined' && 
            typeof $wnd.appformer.forms.addFormDesignerValidation !== 'undefined') {
            
            // Find all enabledFileExtensions input elements in the form designer
            var inputs = $wnd.document.querySelectorAll('input[name*="enabledFileExtensions"], input[id*="enabledFileExtensions"]');
            
            for (var i = 0; i < inputs.length; i++) {
                $wnd.appformer.forms.addFormDesignerValidation(inputs[i], masterAllowedExtensions);
            }
            
            // Also listen for dynamically added elements
            var observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) { // Element node
                            var newInputs = node.querySelectorAll ? 
                                node.querySelectorAll('input[name*="enabledFileExtensions"], input[id*="enabledFileExtensions"]') : [];
                            
                            for (var j = 0; j < newInputs.length; j++) {
                                $wnd.appformer.forms.addFormDesignerValidation(newInputs[j], masterAllowedExtensions);
                            }
                        }
                    });
                });
            });
            
            observer.observe($wnd.document.body, {
                childList: true,
                subtree: true
            });
        }
    }-*/;
}
