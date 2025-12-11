/**
 * Enhanced Form Designer Validation for File Extensions
 * 
 * This JavaScript module provides real-time validation for the form designer
 * when configuring document field properties, specifically the enabledFileExtensions
 * property. It validates against the master allowed list and provides immediate feedback.
 * 
 * @author jBPM Team
 * @since 7.74.1
 */

appformer.forms.FormDesignerValidator = function() {
    this.masterAllowedExtensions = [];
    this.validationMessages = {
        invalidFormat: "Invalid format. Use comma-separated extensions without spaces or dots (e.g., 'pdf,docx,xlsx')",
        subsetViolation: "Extensions not in master allowed list. Master list: {master}",
        emptyMasterList: "No master allowed extensions configured",
        duplicateExtensions: "Duplicate extensions found: {duplicates}",
        validFormat: "Valid format",
        validSubset: "All extensions are in master allowed list"
    };
};

/**
 * Initialize the form designer validator with master allowed extensions
 * @param {string} masterAllowedExtensions - Comma-separated list of master allowed extensions
 */
appformer.forms.FormDesignerValidator.prototype.initialize = function(masterAllowedExtensions) {
    this.masterAllowedExtensions = this.parseExtensions(masterAllowedExtensions);
    console.log('FormDesignerValidator initialized with master extensions:', this.masterAllowedExtensions);
};

/**
 * Parse comma-separated extensions string into array
 * @param {string} extensionsString - Comma-separated extensions
 * @returns {Array} Array of normalized extensions
 */
appformer.forms.FormDesignerValidator.prototype.parseExtensions = function(extensionsString) {
    if (!extensionsString || extensionsString.trim() === '') {
        return [];
    }
    
    return extensionsString.split(',')
        .map(function(ext) { return ext.trim().toLowerCase(); })
        .filter(function(ext) { return ext.length > 0; });
};

/**
 * Validate format of enabledFileExtensions input
 * @param {string} extensions - The extensions string to validate
 * @returns {Object} Validation result with isValid, message, and severity
 */
appformer.forms.FormDesignerValidator.prototype.validateFormat = function(extensions) {
    var result = {
        isValid: true,
        message: '',
        severity: 'none'
    };
    
    if (!extensions || extensions.trim() === '') {
        return result; // Empty is valid
    }
    
    // Check for spaces around commas
    if (extensions.includes(', ') || extensions.includes(' ,')) {
        result.isValid = false;
        result.message = this.validationMessages.invalidFormat;
        result.severity = 'error';
        return result;
    }
    
    // Check for dots in extensions
    if (extensions.includes('.')) {
        result.isValid = false;
        result.message = this.validationMessages.invalidFormat;
        result.severity = 'error';
        return result;
    }
    
    // Check for empty extensions
    var extArray = extensions.split(',');
    for (var i = 0; i < extArray.length; i++) {
        if (extArray[i].trim() === '') {
            result.isValid = false;
            result.message = this.validationMessages.invalidFormat;
            result.severity = 'error';
            return result;
        }
    }
    
    result.message = this.validationMessages.validFormat;
    result.severity = 'success';
    return result;
};

/**
 * Validate that extensions are subset of master allowed list
 * @param {string} extensions - The extensions string to validate
 * @returns {Object} Validation result with isValid, message, and severity
 */
appformer.forms.FormDesignerValidator.prototype.validateSubset = function(extensions) {
    var result = {
        isValid: true,
        message: '',
        severity: 'none'
    };
    
    if (!extensions || extensions.trim() === '') {
        return result; // Empty is valid
    }
    
    if (this.masterAllowedExtensions.length === 0) {
        result.isValid = false;
        result.message = this.validationMessages.emptyMasterList;
        result.severity = 'warning';
        return result;
    }
    
    var formExtensions = this.parseExtensions(extensions);
    var invalidExtensions = [];
    
    for (var i = 0; i < formExtensions.length; i++) {
        if (this.masterAllowedExtensions.indexOf(formExtensions[i]) === -1) {
            invalidExtensions.push(formExtensions[i]);
        }
    }
    
    if (invalidExtensions.length > 0) {
        result.isValid = false;
        result.message = this.validationMessages.subsetViolation.replace('{master}', this.masterAllowedExtensions.join(', '));
        result.severity = 'error';
        return result;
    }
    
    result.message = this.validationMessages.validSubset;
    result.severity = 'success';
    return result;
};

/**
 * Check for duplicate extensions
 * @param {string} extensions - The extensions string to validate
 * @returns {Object} Validation result with isValid, message, and severity
 */
appformer.forms.FormDesignerValidator.prototype.validateDuplicates = function(extensions) {
    var result = {
        isValid: true,
        message: '',
        severity: 'none'
    };
    
    if (!extensions || extensions.trim() === '') {
        return result; // Empty is valid
    }
    
    var extArray = extensions.split(',');
    var seen = {};
    var duplicates = [];
    
    for (var i = 0; i < extArray.length; i++) {
        var ext = extArray[i].trim().toLowerCase();
        if (seen[ext]) {
            if (duplicates.indexOf(ext) === -1) {
                duplicates.push(ext);
            }
        } else {
            seen[ext] = true;
        }
    }
    
    if (duplicates.length > 0) {
        result.isValid = false;
        result.message = this.validationMessages.duplicateExtensions.replace('{duplicates}', duplicates.join(', '));
        result.severity = 'warning';
        return result;
    }
    
    return result;
};

/**
 * Comprehensive validation of enabledFileExtensions input
 * @param {string} extensions - The extensions string to validate
 * @returns {Object} Overall validation result
 */
appformer.forms.FormDesignerValidator.prototype.validate = function(extensions) {
    var formatResult = this.validateFormat(extensions);
    if (!formatResult.isValid) {
        return formatResult;
    }
    
    var duplicateResult = this.validateDuplicates(extensions);
    if (!duplicateResult.isValid) {
        return duplicateResult;
    }
    
    var subsetResult = this.validateSubset(extensions);
    return subsetResult;
};

/**
 * Show validation feedback in the form designer UI
 * @param {Element} inputElement - The input element to show feedback on
 * @param {Object} validationResult - The validation result to display
 */
appformer.forms.FormDesignerValidator.prototype.showValidationFeedback = function(inputElement, validationResult) {
    if (!inputElement) return;
    
    // Clear existing feedback
    this.clearValidationFeedback(inputElement);
    
    if (validationResult.severity === 'none') {
        return;
    }
    
    // Create feedback element
    var feedbackElement = document.createElement('div');
    feedbackElement.className = 'form-designer-validation-feedback';
    
    // Set styling based on severity
    switch (validationResult.severity) {
        case 'error':
            feedbackElement.style.color = '#d32f2f';
            feedbackElement.style.borderColor = '#d32f2f';
            feedbackElement.classList.add('validation-error');
            break;
        case 'warning':
            feedbackElement.style.color = '#f57c00';
            feedbackElement.style.borderColor = '#f57c00';
            feedbackElement.classList.add('validation-warning');
            break;
        case 'success':
            feedbackElement.style.color = '#2e7d32';
            feedbackElement.style.borderColor = '#2e7d32';
            feedbackElement.classList.add('validation-success');
            break;
    }
    
    feedbackElement.style.fontSize = '12px';
    feedbackElement.style.marginTop = '4px';
    feedbackElement.style.padding = '4px 8px';
    feedbackElement.style.borderRadius = '3px';
    feedbackElement.style.border = '1px solid';
    feedbackElement.style.backgroundColor = 'rgba(255, 255, 255, 0.9)';
    feedbackElement.textContent = validationResult.message;
    
    // Insert feedback after input element
    inputElement.parentNode.insertBefore(feedbackElement, inputElement.nextSibling);
    
    // Add styling to input element
    inputElement.classList.add('form-designer-validation-' + validationResult.severity);
    inputElement.style.borderColor = feedbackElement.style.borderColor;
};

/**
 * Clear validation feedback from input element
 * @param {Element} inputElement - The input element to clear feedback from
 */
appformer.forms.FormDesignerValidator.prototype.clearValidationFeedback = function(inputElement) {
    if (!inputElement) return;
    
    // Remove feedback elements
    var feedbackElements = inputElement.parentNode.querySelectorAll('.form-designer-validation-feedback');
    for (var i = 0; i < feedbackElements.length; i++) {
        feedbackElements[i].remove();
    }
    
    // Remove input styling
    inputElement.classList.remove('form-designer-validation-error');
    inputElement.classList.remove('form-designer-validation-warning');
    inputElement.classList.remove('form-designer-validation-success');
    inputElement.style.borderColor = '';
};

/**
 * Add real-time validation to enabledFileExtensions input
 * @param {Element} inputElement - The input element to add validation to
 */
appformer.forms.FormDesignerValidator.prototype.addRealTimeValidation = function(inputElement) {
    if (!inputElement) {
        console.error('FormDesignerValidator: Cannot add validation to null input element');
        return;
    }
    
    var self = this;
    
    // Add input event listener for real-time validation
    inputElement.addEventListener('input', function(event) {
        var extensions = event.target.value;
        var validationResult = self.validate(extensions);
        self.showValidationFeedback(event.target, validationResult);
    });
    
    // Add blur event listener for final validation
    inputElement.addEventListener('blur', function(event) {
        var extensions = event.target.value;
        var validationResult = self.validate(extensions);
        self.showValidationFeedback(event.target, validationResult);
    });
};

/**
 * Create a new form designer validator instance
 * @param {string} masterAllowedExtensions - Master allowed extensions
 * @returns {appformer.forms.FormDesignerValidator} New validator instance
 */
appformer.forms.FormDesignerValidator.create = function(masterAllowedExtensions) {
    var validator = new appformer.forms.FormDesignerValidator();
    validator.initialize(masterAllowedExtensions);
    return validator;
};

/**
 * Global utility function to add validation to enabledFileExtensions input
 * @param {Element} inputElement - Input element to add validation to
 * @param {string} masterAllowedExtensions - Master allowed extensions
 */
appformer.forms.addFormDesignerValidation = function(inputElement, masterAllowedExtensions) {
    var validator = appformer.forms.FormDesignerValidator.create(masterAllowedExtensions);
    validator.addRealTimeValidation(inputElement);
};
