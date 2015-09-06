package org.walkmod;

import java.util.Map;

/**
 * Helper class to create Walkmod options map.
 *
 * Usage: OptionsBuilder.options.offline(true).includes("src/main","src/test").asMap()
 *
 * @author abelsromero
 */
public class OptionsBuilder {

    /**
     * Options instance
     */
    private Options options;

    /**
     * Creates an OptionBuilder with the default options
     */
    private OptionsBuilder() {
        options = new Options();
        options.setOffline(false);
        options.setVerbose(true);
        options.setPrintErrors(false);
        options.setThrowException(false);
    }

    /**
     * Creates an OptionBuilder with the initialization options
     */
    private OptionsBuilder(Map<String, Object> options) {
        this.options = new Options(options);
        // Check if default values are set
        if (!options.containsKey(Options.OFFLINE))
            this.options.setOffline(false);
        if (!options.containsKey(Options.VERBOSE))
            this.options.setVerbose(true);
        if (!options.containsKey(Options.PRINT_ERRORS))
            this.options.setPrintErrors(false);
        if (!options.containsKey(Options.THROW_EXCEPTION))
            this.options.setThrowException(false);
    }

    /**
     * Creates an OptionBuilder instance with the default options:
     *
     * <ul>
     *   <li>offline = false
     *   <li>verbose = true
     *   <li>printErrors = false
     *   <li>throwException = false
     * </ul>
     *
     * @return options builder instance
     */
    public static OptionsBuilder options() {
        return new OptionsBuilder();
    }

    /**
     * Creates an OptionBuilder instance with the default options:
     *
     * @param options Map of starting options. See {@link Options} for available
     *                keys
     *
     * @return options builder instance
     */
    public static OptionsBuilder options(Map<String, Object> options) {
        return new OptionsBuilder(options);
    }

    /**
     * Sets the offline option
     *
     * @param offline true to disable resolution of plugin from remote repositories
     * @return updated OptionBuilder instance
     * @see Options#OFFLINE
     */
    public OptionsBuilder offline(boolean offline) {
        options.setOffline(offline);
        return this;
    }

    /**
     * Sets the verbose option
     *
     * @param verbose true to enable info messages in the console
     * @return updated OptionBuilder instance
     *
     * @see Options#VERBOSE
     */
    public OptionsBuilder verbose(boolean verbose) {
        options.setVerbose(verbose);
        return this;
    }

    /**
     * Sets the printErrors option
     *
     * @param printErrors true to enable error messages in the console
     * @return updated OptionBuilder instance
     *
     * @see Options#PRINT_ERRORS
     */
    public OptionsBuilder printErrors(boolean printErrors) {
        options.setPrintErrors(printErrors);
        return this;
    }

    /**
     * Sets the printErrors option
     *
     * @param throwException true to enable exception throwing
     * @return updated OptionBuilder instance
     *
     * @see Options#PRINT_ERRORS
     */
    public OptionsBuilder throwException(boolean throwException) {
        options.setThrowException(throwException);
        return this;
    }

    /**
     * Sets the includes option
     *
     * @param includes List of included paths
     * @return updated OptionBuilder instance
     *
     * @see Options#INCLUDES
     */
    public OptionsBuilder includes(String... includes) {
        options.setIncludes(includes);
        return this;
    }

    /**
     * Sets the excludes
     *
     * @param excludes List of excluded paths
     * @return updated OptionBuilder instance
     *
     * @see Options#EXCLUDES
     */
    public OptionsBuilder excludes(String... excludes) {
        options.setExcludes(excludes);
        return this;
    }

    /**
     * Returns the stored options as a Map&lt;String,Object&gt;
     *
     * @return map with options
     */
    public Map<String, Object> asMap() {
        return options.asMap();
    }

}
