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
    private Options options = new Options();

    /**
     * Creates an OptionBuilder with the default options
     */
    private OptionsBuilder() {
        options.setOffline(false);
        options.setVerbose(true);
        options.setPrintErrors(false);
        options.setThrowException(false);
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
     * Sets the offline option
     *
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
     * @return updated OptionBuilder instance
     * @see Options#VERBOSE
     */
    public OptionsBuilder verbose(boolean verbose) {
        options.setVerbose(verbose);
        return this;
    }

    /**
     * Sets the printErrors option
     *
     * @return updated OptionBuilder instance
     * @see Options#PRINT_ERRORS
     */
    public OptionsBuilder printErrors(boolean printErrors) {
        options.setPrintErrors(printErrors);
        return this;
    }

    /**
     * Sets the printErrors option
     *
     * @return updated OptionBuilder instance
     * @see Options#PRINT_ERRORS
     */
    public OptionsBuilder throwException(boolean throwException) {
        options.setThrowException(throwException);
        return this;
    }

    /**
     * Sets the includes option
     *
     * @return updated OptionBuilder instance
     * @see Options#INCLUDES
     */
    public OptionsBuilder includes(String... includes) {
        options.setIncludes(includes);
        return this;
    }

    /**
     * Sets the excludes option
     *
     * @return updated OptionBuilder instance
     * @see Options#EXCLUDES
     */
    public OptionsBuilder excludes(String... excludes) {
        options.setExcludes(excludes);
        return this;
    }

    /**
     * Returns the stored options as a Map<String,Object>
     *
     * @return map with options
     */
    public Map<String, Object> asMap() {
        return options.asMap();
    }

}
