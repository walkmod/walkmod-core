package org.walkmod;

import java.util.*;

/**
 * Helper class to create Walkmos options map
 *
 * Inspired by Asciidoctor's Options
 *
 * @author abelsromero
 */
public class Options {

    /**
     * (Boolean) Disables/enables remote fetching of dependencies and plugins
     */
    public static final String OFFLINE = "offline";
    /**
     * (Boolean) Disables/enables info messages in the console
     */
    public static final String VERBOSE = "verbose";
    /**
     * (Boolean) Disables/enables error messages in the console
     */
    public static final String PRINT_ERRORS = "print_errors";
    /**
     * (Boolean) Disables/enables the capture of original exceptions (requires verbose = true)
     */
    public static final String THROW_EXCEPTION = "throw_exception";
    /**
     * (List<String>) Overwrites the include rules in the chain's reader
     */
    public static final String INCLUDES = "includes";
    /**
     * (List<String>) Overwrites the exclude rules in the chain's reader
     */
    public static final String EXCLUDES = "excludes";

    /**
     * Stored options
     */
    private Map<String, Object> options = new HashMap<String, Object>();

    /**
     * Creates an empty set of options
     */
    public Options() {
    }

    public void setOffline(boolean offline) {
        this.options.put(OFFLINE, Boolean.valueOf(offline));
    }

    public void setVerbose(boolean verbose) {
        this.options.put(VERBOSE, Boolean.valueOf(verbose));
    }

    public void setPrintErrors(boolean printErrors) {
        this.options.put(PRINT_ERRORS, Boolean.valueOf(printErrors));
    }

    public void setThrowException(boolean throwException) {
        this.options.put(THROW_EXCEPTION, Boolean.valueOf(throwException));
    }

    public void setIncludes(String... includes) {

        if (!this.options.containsKey(INCLUDES)) {
            this.options.put(INCLUDES, new ArrayList<Object>());
        }

        List<Object> allIncludes = (List<Object>) this.options.get(INCLUDES);
        allIncludes.addAll(Arrays.asList(includes));
    }

    public void setExcludes(String... excludes) {

        if (!this.options.containsKey(EXCLUDES)) {
            this.options.put(EXCLUDES, new ArrayList<Object>());
        }

        List<Object> allIncludes = (List<Object>) this.options.get(EXCLUDES);
        allIncludes.addAll(Arrays.asList(excludes));
    }

    /**
     * Returns the stored options as a Map<String,Object>
     *
     * @return map with options
     */
    public Map<String, Object> asMap() {
        return this.options;
    }
}
