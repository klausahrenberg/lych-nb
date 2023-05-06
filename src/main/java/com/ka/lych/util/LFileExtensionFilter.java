package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LFileExtensionFilter {
    
    private final String description;
    private final String[] extensions;
    
    public LFileExtensionFilter(final String description,
                                final String... extensions) {
        this.description = description;
        this.extensions = extensions;
    }

    public String getDescription() {
        return description;
    }

    public String[] getExtensions() {
        return extensions;
    }
    
}
