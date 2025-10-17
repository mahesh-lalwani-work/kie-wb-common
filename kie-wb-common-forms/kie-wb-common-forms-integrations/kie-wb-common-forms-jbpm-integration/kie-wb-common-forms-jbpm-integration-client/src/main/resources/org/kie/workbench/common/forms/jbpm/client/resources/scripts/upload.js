appformer = {
    forms: {
        Documents: function Documents(autoUpload) {
            this.autoUpload = autoUpload;
            this.fileExtensionValidator = null;
            this.validationEnabled = true;
        },
        Document: function Document(id, name, url, size, lastModified) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.size = size;
            this.lastModified = lastModified;
        }
    }
};

appformer.forms.Documents.get = function() {
    return new appformer.forms.Documents(false);
};

appformer.forms.Documents.get = function(autoUpload) {
    if (autoUpload == true) {
        return new appformer.forms.Documents(true);
    }

    return new appformer.forms.Documents(false);
};

appformer.forms.Documents.prototype.preventEvents = function (event) {
    event.preventDefault();
    event.stopPropagation();
};

appformer.forms.Documents.prototype.dropFiles = function (event) {
    this.preventEvents(event);

    if (event.dataTransfer.items) {
        for (var i = 0; i < event.dataTransfer.items.length; i++) {
            if (event.dataTransfer.items[i].kind === 'file') {
                var item = event.dataTransfer.items[i];
                var isFile = true;
                if (typeof (item.webkitGetAsEntry) == "function") {
                    isFile = item.webkitGetAsEntry().isFile;
                } else if (typeof (item.getAsEntry) == "function") {
                    isFile =  item.getAsEntry().isFile;
                }

                if (isFile) {
                    this.dropFile(item.getAsFile());
                }
            }
        }
    } else {
        this.dropFilesList(event.dataTransfer.files);
    }
};

appformer.forms.Documents.prototype.dropFilesList = function(fileList) {
    // Validate all files before processing
    if (!this.validateFiles(fileList)) {
        return; // Stop processing if validation fails
    }
    
    for (var i = 0; i < fileList.length; i++) {
        this.dropFile(fileList[i]);
    }
};

appformer.forms.Documents.prototype.dropFile = function (file) {
    // Validate file extension if validation is enabled
    if (this.validationEnabled && this.fileExtensionValidator) {
        var validationResult = this.fileExtensionValidator.validateFile(file.name);
        if (!validationResult.isValid) {
            this.showValidationError(validationResult.message);
            return false;
        }
    }
    
    var id = Number(Math.random().toString().slice(2,11)).toString();

    var document = new appformer.forms.Document(id, file.name, '', file.size, file.lastModified);

    if (this.autoUpload) {
        var callback =  this.onDropCallback;
        var fileReader = new FileReader();
        fileReader.onload = function (event) {
            if (event.target.readyState == FileReader.DONE) {
                document.url = event.target.result
                callback(document);
            }
        };
        fileReader.readAsDataURL(file);
    } else {
        this.onDropCallback(document, file);
    }
    
    return true;
};

appformer.forms.Documents.prototype.bind = function(element) {
    if (!element) {
        throw "Cannot bind documents upload to a null element";
    }
    if (!element.tagName) {
        throw "Cannot bind documents upload to a non html element";
    }

    var tag = element.tagName.toUpperCase();

    if (tag === "DIV") {
        this.divElement = element;
        ['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave'].forEach(eventName => this.divElement.addEventListener(eventName, event => this.preventEvents(event)));
        this.divElement.addEventListener('drop', event => this.dropFiles(event));
        return this;
    } else if (tag === "INPUT" && element.type.toUpperCase() == "FILE") {
        this.inputElement = element;
        this.inputElement.addEventListener('change', event => this.dropFilesList(event.target.files));
        return this;
    }

    throw "Cannot bind documents to " + element.tagName + " elements";
};

appformer.forms.Documents.prototype.onDrop = function(callback) {
    this.onDropCallback = callback;
    return this;
};

/**
 * Initialize file extension validation
 * @param {string} formAllowedExtensions - Comma-separated list of form-specific allowed extensions
 * @param {string} globalAllowedExtensions - Comma-separated list of global allowed extensions
 */
appformer.forms.Documents.prototype.initializeValidation = function(formAllowedExtensions, globalAllowedExtensions) {
    if (typeof appformer.forms.FileExtensionValidator !== 'undefined') {
        this.fileExtensionValidator = appformer.forms.FileExtensionValidator.create(formAllowedExtensions, globalAllowedExtensions);
        console.log('File extension validation initialized for Documents upload');
    } else {
        console.warn('FileExtensionValidator not available - validation disabled');
        this.validationEnabled = false;
    }
    return this;
};

/**
 * Enable or disable file extension validation
 * @param {boolean} enabled - Whether to enable validation
 */
appformer.forms.Documents.prototype.setValidationEnabled = function(enabled) {
    this.validationEnabled = enabled;
    return this;
};

/**
 * Show validation error message to user
 * @param {string} message - Error message to display
 */
appformer.forms.Documents.prototype.showValidationError = function(message) {
    // Try to show error in a user-friendly way
    if (typeof alert !== 'undefined') {
        alert('File Upload Error: ' + message);
    } else if (typeof console !== 'undefined') {
        console.error('File Upload Error:', message);
    }
    
    // If we have a bound element, try to show error there
    if (this.divElement) {
        this.showErrorOnElement(this.divElement, message);
    } else if (this.inputElement) {
        this.showErrorOnElement(this.inputElement, message);
    }
};

/**
 * Show error message on a specific element
 * @param {Element} element - DOM element to show error on
 * @param {string} message - Error message to display
 */
appformer.forms.Documents.prototype.showErrorOnElement = function(element, message) {
    if (!element) return;
    
    // Remove existing error messages
    this.clearErrorOnElement(element);
    
    // Create error message element
    var errorElement = document.createElement('div');
    errorElement.className = 'document-upload-error';
    errorElement.style.color = '#d32f2f';
    errorElement.style.fontSize = '12px';
    errorElement.style.marginTop = '4px';
    errorElement.style.display = 'block';
    errorElement.style.padding = '4px 8px';
    errorElement.style.backgroundColor = '#ffebee';
    errorElement.style.border = '1px solid #d32f2f';
    errorElement.style.borderRadius = '4px';
    errorElement.textContent = message;
    
    // Insert error message after element
    element.parentNode.insertBefore(errorElement, element.nextSibling);
    
    // Add error styling to element
    element.style.borderColor = '#d32f2f';
    element.style.borderWidth = '2px';
};

/**
 * Clear error message from element
 * @param {Element} element - DOM element to clear error from
 */
appformer.forms.Documents.prototype.clearErrorOnElement = function(element) {
    if (!element) return;
    
    // Remove error message elements
    var errorElements = element.parentNode.querySelectorAll('.document-upload-error');
    for (var i = 0; i < errorElements.length; i++) {
        errorElements[i].remove();
    }
    
    // Remove error styling
    element.style.borderColor = '';
    element.style.borderWidth = '';
};

/**
 * Validate multiple files before processing
 * @param {FileList} fileList - List of files to validate
 * @returns {boolean} True if all files are valid, false otherwise
 */
appformer.forms.Documents.prototype.validateFiles = function(fileList) {
    if (!this.validationEnabled || !this.fileExtensionValidator) {
        return true; // Validation disabled or not initialized
    }
    
    var fileNames = [];
    for (var i = 0; i < fileList.length; i++) {
        fileNames.push(fileList[i].name);
    }
    
    var validationResult = this.fileExtensionValidator.validateFiles(fileNames);
    
    if (!validationResult.isValid) {
        this.showValidationError(validationResult.message);
        return false;
    }
    
    return true;
};

/**
 * Get allowed file types message
 * @returns {string} User-friendly message about allowed file types
 */
appformer.forms.Documents.prototype.getAllowedTypesMessage = function() {
    if (this.fileExtensionValidator) {
        return this.fileExtensionValidator.getAllowedTypesMessage();
    }
    return 'File type validation not configured';
};