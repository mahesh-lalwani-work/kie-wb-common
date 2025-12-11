/**
 * Enhanced File Extension Validator for jBPM Documents
 * 
 * This JavaScript module provides comprehensive client-side validation for file extensions
 * in jBPM document upload forms. It supports both single file and multiple file validation
 * with real-time feedback and user-friendly error messages.
 * 
 * @author jBPM Team
 * @since 7.74.1
 */

appformer.forms.FileExtensionValidator = function() {
    this.allowedExtensions = [];
    this.formAllowedExtensions = [];
    this.globalAllowedExtensions = [];
    this.validationMessages = {
        invalidExtension: "File extension '{extension}' is not allowed.",
        noExtension: "File must have a valid extension.",
        emptyFile: "Please select a file to upload.",
        multipleInvalidFiles: "Some files have invalid extensions: {files}",
        allowedTypes: "Allowed file types: {types}",
        configurationError: "File type configuration is not available. Please contact your administrator."
    };
};

/**
 * Initialize the validator with allowed extensions
 * @param {string} formAllowedExtensions - Comma-separated list of form-specific allowed extensions
 * @param {string} globalAllowedExtensions - Comma-separated list of global allowed extensions
 */
appformer.forms.FileExtensionValidator.prototype.initialize = function(formAllowedExtensions, globalAllowedExtensions) {
    this.formAllowedExtensions = this.parseExtensions(formAllowedExtensions);
    this.globalAllowedExtensions = this.parseExtensions(globalAllowedExtensions);
    
    // Determine the effective allowed extensions (form-specific takes precedence)
    if (this.formAllowedExtensions.length > 0) {
        this.allowedExtensions = this.formAllowedExtensions;
    } else if (this.globalAllowedExtensions.length > 0) {
        this.allowedExtensions = this.globalAllowedExtensions;
    } else {
        // Fallback to default extensions if no configuration is available
        this.allowedExtensions = ['pdf', 'docx', 'xlsx', 'txt', 'jpg', 'png'];
        console.warn('FileExtensionValidator: No allowed extensions configured, using defaults');
    }
    
    console.log('FileExtensionValidator initialized with extensions:', this.allowedExtensions);
};

/**
 * Parse comma-separated extensions string into array
 * @param {string} extensionsString - Comma-separated extensions
 * @returns {Array} Array of normalized extensions
 */
appformer.forms.FileExtensionValidator.prototype.parseExtensions = function(extensionsString) {
    if (!extensionsString || extensionsString.trim() === '') {
        return [];
    }
    
    return extensionsString.split(',')
        .map(function(ext) { return ext.trim().toLowerCase(); })
        .filter(function(ext) { return ext.length > 0; });
};

/**
 * Extract file extension from filename
 * @param {string} fileName - The filename to extract extension from
 * @returns {string|null} The file extension (lowercase) or null if not found
 */
appformer.forms.FileExtensionValidator.prototype.getFileExtension = function(fileName) {
    if (!fileName || typeof fileName !== 'string') {
        return null;
    }
    
    var lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex === -1 || lastDotIndex === fileName.length - 1) {
        return null;
    }
    
    return fileName.substring(lastDotIndex + 1).toLowerCase();
};

/**
 * Validate a single file extension
 * @param {string} fileName - The filename to validate
 * @returns {Object} Validation result with isValid, message, and extension properties
 */
appformer.forms.FileExtensionValidator.prototype.validateFile = function(fileName) {
    var result = {
        isValid: false,
        message: '',
        extension: null,
        fileName: fileName
    };
    
    // Check if file name is provided
    if (!fileName || fileName.trim() === '') {
        result.message = this.validationMessages.emptyFile;
        return result;
    }
    
    // Extract extension
    var extension = this.getFileExtension(fileName);
    result.extension = extension;
    
    if (!extension) {
        result.message = this.validationMessages.noExtension;
        return result;
    }
    
    // Check if extension is allowed
    if (this.allowedExtensions.indexOf(extension) === -1) {
        result.message = this.validationMessages.invalidExtension.replace('{extension}', extension);
        return result;
    }
    
    result.isValid = true;
    return result;
};

/**
 * Validate multiple files
 * @param {Array} fileNames - Array of filenames to validate
 * @returns {Object} Validation result with overall status and individual file results
 */
appformer.forms.FileExtensionValidator.prototype.validateFiles = function(fileNames) {
    var result = {
        isValid: true,
        message: '',
        totalFiles: fileNames ? fileNames.length : 0,
        validFiles: 0,
        invalidFiles: 0,
        fileResults: [],
        invalidFileNames: []
    };
    
    if (!fileNames || fileNames.length === 0) {
        result.isValid = true;
        result.message = '';
        return result;
    }
    
    var invalidFiles = [];
    
    for (var i = 0; i < fileNames.length; i++) {
        var fileResult = this.validateFile(fileNames[i]);
        result.fileResults.push(fileResult);
        
        if (fileResult.isValid) {
            result.validFiles++;
        } else {
            result.invalidFiles++;
            invalidFiles.push(fileResult.fileName);
            result.invalidFileNames.push(fileResult.fileName);
        }
    }
    
    if (result.invalidFiles > 0) {
        result.isValid = false;
        if (result.invalidFiles === 1) {
            result.message = result.fileResults.find(function(fr) { return !fr.isValid; }).message;
        } else {
            result.message = this.validationMessages.multipleInvalidFiles.replace('{files}', invalidFiles.join(', '));
        }
    }
    
    return result;
};

/**
 * Get user-friendly allowed extensions message
 * @returns {string} Formatted message showing allowed file types
 */
appformer.forms.FileExtensionValidator.prototype.getAllowedTypesMessage = function() {
    if (this.allowedExtensions.length === 0) {
        return this.validationMessages.configurationError;
    }
    
    return this.validationMessages.allowedTypes.replace('{types}', this.allowedExtensions.join(', '));
};

/**
 * Show validation error message to user
 * @param {string} message - Error message to display
 * @param {Element} targetElement - DOM element to show error on
 */
appformer.forms.FileExtensionValidator.prototype.showError = function(message, targetElement) {
    if (!targetElement) {
        console.error('FileExtensionValidator: Cannot show error - target element is null');
        return;
    }
    
    // Remove existing error messages
    this.clearError(targetElement);
    
    // Create error message element
    var errorElement = document.createElement('div');
    errorElement.className = 'file-extension-error';
    errorElement.style.color = '#d32f2f';
    errorElement.style.fontSize = '12px';
    errorElement.style.marginTop = '4px';
    errorElement.style.display = 'block';
    errorElement.textContent = message;
    
    // Insert error message after target element
    targetElement.parentNode.insertBefore(errorElement, targetElement.nextSibling);
    
    // Add error styling to target element
    targetElement.classList.add('file-extension-error-field');
    targetElement.style.borderColor = '#d32f2f';
};

/**
 * Show validation warning message to user
 * @param {string} message - Warning message to display
 * @param {Element} targetElement - DOM element to show warning on
 */
appformer.forms.FileExtensionValidator.prototype.showWarning = function(message, targetElement) {
    if (!targetElement) {
        console.error('FileExtensionValidator: Cannot show warning - target element is null');
        return;
    }
    
    // Remove existing warning messages
    this.clearWarning(targetElement);
    
    // Create warning message element
    var warningElement = document.createElement('div');
    warningElement.className = 'file-extension-warning';
    warningElement.style.color = '#f57c00';
    warningElement.style.fontSize = '12px';
    warningElement.style.marginTop = '4px';
    warningElement.style.display = 'block';
    warningElement.textContent = message;
    
    // Insert warning message after target element
    targetElement.parentNode.insertBefore(warningElement, targetElement.nextSibling);
    
    // Add warning styling to target element
    targetElement.classList.add('file-extension-warning-field');
    targetElement.style.borderColor = '#f57c00';
};

/**
 * Clear error message and styling
 * @param {Element} targetElement - DOM element to clear error from
 */
appformer.forms.FileExtensionValidator.prototype.clearError = function(targetElement) {
    if (!targetElement) return;
    
    // Remove error message elements
    var errorElements = targetElement.parentNode.querySelectorAll('.file-extension-error');
    for (var i = 0; i < errorElements.length; i++) {
        errorElements[i].remove();
    }
    
    // Remove error styling
    targetElement.classList.remove('file-extension-error-field');
    targetElement.style.borderColor = '';
};

/**
 * Clear warning message and styling
 * @param {Element} targetElement - DOM element to clear warning from
 */
appformer.forms.FileExtensionValidator.prototype.clearWarning = function(targetElement) {
    if (!targetElement) return;
    
    // Remove warning message elements
    var warningElements = targetElement.parentNode.querySelectorAll('.file-extension-warning');
    for (var i = 0; i < warningElements.length; i++) {
        warningElements[i].remove();
    }
    
    // Remove warning styling
    targetElement.classList.remove('file-extension-warning-field');
    targetElement.style.borderColor = '';
};

/**
 * Clear all validation messages and styling
 * @param {Element} targetElement - DOM element to clear all validation from
 */
appformer.forms.FileExtensionValidator.prototype.clearValidation = function(targetElement) {
    this.clearError(targetElement);
    this.clearWarning(targetElement);
    this.clearSuccess(targetElement);
};

/**
 * Show basic alert for immediate user feedback
 * @param {string} message - Error message to display in alert
 */
appformer.forms.FileExtensionValidator.prototype.showBasicAlert = function(message) {
    if (typeof alert !== 'undefined') {
        alert('File Validation Error: ' + message);
    } else {
        console.error('FileExtensionValidator: Alert not available, error message:', message);
    }
};

/**
 * Show success message to user
 * @param {string} message - Success message to display
 * @param {Element} targetElement - DOM element to show success on
 */
appformer.forms.FileExtensionValidator.prototype.showSuccess = function(message, targetElement) {
    if (!targetElement) {
        console.log('FileExtensionValidator: Success -', message);
        return;
    }
    
    // Remove existing success messages
    this.clearSuccess(targetElement);
    
    // Create success message element
    var successElement = document.createElement('div');
    successElement.className = 'file-extension-success';
    successElement.style.color = '#2e7d32';
    successElement.style.fontSize = '12px';
    successElement.style.marginTop = '4px';
    successElement.style.display = 'block';
    successElement.textContent = message;
    
    // Insert success message after target element
    targetElement.parentNode.insertBefore(successElement, targetElement.nextSibling);
    
    // Add success styling to target element
    targetElement.classList.add('file-extension-success-field');
    targetElement.style.borderColor = '#2e7d32';
};

/**
 * Clear success message and styling
 * @param {Element} targetElement - DOM element to clear success from
 */
appformer.forms.FileExtensionValidator.prototype.clearSuccess = function(targetElement) {
    if (!targetElement) return;
    
    // Remove success message elements
    var successElements = targetElement.parentNode.querySelectorAll('.file-extension-success');
    for (var i = 0; i < successElements.length; i++) {
        successElements[i].remove();
    }
    
    // Remove success styling
    targetElement.classList.remove('file-extension-success-field');
    targetElement.style.borderColor = '';
};

/**
 * Validate file input element and show feedback
 * @param {Element} fileInput - File input element to validate
 * @returns {boolean} True if validation passed, false otherwise
 */
appformer.forms.FileExtensionValidator.prototype.validateFileInput = function(fileInput) {
    if (!fileInput || fileInput.type !== 'file') {
        console.error('FileExtensionValidator: Invalid file input element');
        return false;
    }
    
    this.clearValidation(fileInput);
    
    var files = fileInput.files;
    if (!files || files.length === 0) {
        return true; // No files selected is valid
    }
    
    var fileNames = [];
    for (var i = 0; i < files.length; i++) {
        fileNames.push(files[i].name);
    }
    
    var validationResult = this.validateFiles(fileNames);
    
    if (!validationResult.isValid) {
        this.showError(validationResult.message, fileInput);
        // Show basic alert for immediate user feedback
        this.showBasicAlert(validationResult.message);
        return false;
    } else {
        // Show success message for valid files
        var successMessage = 'File validation successful. Allowed types: ' + this.allowedExtensions.join(', ');
        this.showSuccess(successMessage, fileInput);
    }
    
    return true;
};

/**
 * Add real-time validation to file input element
 * @param {Element} fileInput - File input element to add validation to
 */
appformer.forms.FileExtensionValidator.prototype.addRealTimeValidation = function(fileInput) {
    if (!fileInput || fileInput.type !== 'file') {
        console.error('FileExtensionValidator: Cannot add validation to invalid file input');
        return;
    }
    
    var self = this;
    
    // Add change event listener for real-time validation
    fileInput.addEventListener('change', function(event) {
        self.validateFileInput(event.target);
    });
    
    // Add drag and drop validation if supported
    if (fileInput.parentNode) {
        fileInput.parentNode.addEventListener('dragover', function(event) {
            event.preventDefault();
        });
        
        fileInput.parentNode.addEventListener('drop', function(event) {
            event.preventDefault();
            // Validation will be triggered by the change event
        });
    }
};

/**
 * Create a new validator instance
 * @param {string} formAllowedExtensions - Form-specific allowed extensions
 * @param {string} globalAllowedExtensions - Global allowed extensions
 * @returns {appformer.forms.FileExtensionValidator} New validator instance
 */
appformer.forms.FileExtensionValidator.create = function(formAllowedExtensions, globalAllowedExtensions) {
    var validator = new appformer.forms.FileExtensionValidator();
    validator.initialize(formAllowedExtensions, globalAllowedExtensions);
    return validator;
};

/**
 * Global utility function to validate file extensions
 * @param {string} fileName - File name to validate
 * @param {string} allowedExtensions - Comma-separated allowed extensions
 * @returns {boolean} True if file extension is allowed
 */
appformer.forms.isValidFileExtension = function(fileName, allowedExtensions) {
    var validator = new appformer.forms.FileExtensionValidator();
    validator.initialize(allowedExtensions, '');
    var result = validator.validateFile(fileName);
    return result.isValid;
};

/**
 * Global utility function to get file extension
 * @param {string} fileName - File name to extract extension from
 * @returns {string|null} File extension or null
 */
appformer.forms.getFileExtension = function(fileName) {
    var validator = new appformer.forms.FileExtensionValidator();
    return validator.getFileExtension(fileName);
};

/**
 * Global utility function to validate file input with basic alert
 * @param {Element} fileInput - File input element to validate
 * @param {string} allowedExtensions - Comma-separated allowed extensions
 * @returns {boolean} True if validation passed, false otherwise
 */
appformer.forms.validateFileInputWithAlert = function(fileInput, allowedExtensions) {
    var validator = new appformer.forms.FileExtensionValidator();
    validator.initialize(allowedExtensions, '');
    return validator.validateFileInput(fileInput);
};

/**
 * Global utility function to add real-time validation to file input
 * @param {Element} fileInput - File input element to add validation to
 * @param {string} allowedExtensions - Comma-separated allowed extensions
 */
appformer.forms.addFileInputValidation = function(fileInput, allowedExtensions) {
    var validator = new appformer.forms.FileExtensionValidator();
    validator.initialize(allowedExtensions, '');
    validator.addRealTimeValidation(fileInput);
};

/**
 * Global utility function to show file validation error alert
 * @param {string} message - Error message to display
 */
appformer.forms.showFileValidationAlert = function(message) {
    if (typeof alert !== 'undefined') {
        alert('File Validation Error: ' + message);
    } else {
        console.error('FileExtensionValidator: Alert not available, error message:', message);
    }
};
