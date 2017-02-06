/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
  Walkmod is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  Walkmod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod;

import java.io.File;
import java.util.Map;

/**
 * Helper class to create Walkmod options map.
 *
 * Usage:
 * OptionsBuilder.options().offline(true).includes("src/main","src/test").asMap()
 *
 * or 
 * 
 *  OptionsBuilder.options().offline(true).includes("src/main","src/test").build()
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

	}

	/**
	 * Creates an OptionBuilder with the initialization options
	 */
	private OptionsBuilder(Map<String, Object> options) {
		this.options = new Options(options);

	}

	/**
	 * Creates an OptionBuilder instance with the default options:
	 *
	 * <ul>
	 * <li>offline = false
	 * <li>verbose = true
	 * <li>printErrors = false
	 * <li>throwException = false
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
	 * @param options
	 *            Map of starting options. See {@link Options} for available
	 *            keys
	 *
	 * @return options builder instance
	 */
	public static OptionsBuilder options(Map<String, Object> options) {
		return new OptionsBuilder(options);
	}

	/**
	 * Sets the offline option
	 *
	 * @param offline
	 *            true to disable resolution of plugin from remote repositories
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
	 * @param verbose
	 *            true to enable info messages in the console
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#VERBOSE
	 */
	public OptionsBuilder verbose(boolean verbose) {
		options.setVerbose(verbose);
		return this;
	}
	
	/**
     * Sets the path option
     *
     * @param path
     *            directory to read and write from
     * @return updated OptionBuilder instance
     *
     * @see Options#VERBOSE
     */
    public OptionsBuilder path(String path) {
        options.setPath(path);
        return this;
    }

	/**
	 * Sets the printErrors option
	 *
	 * @param printErrors
	 *            true to enable error messages in the console
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
	 * @param throwException
	 *            true to enable exception throwing
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
	 * @param includes
	 *            List of included paths
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
	 * @param excludes
	 *            List of excluded paths
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#EXCLUDES
	 */
	public OptionsBuilder excludes(String... excludes) {
		options.setExcludes(excludes);
		return this;
	}
	
	/**
	 * Seths the dynamic arguments
	 * @param dynamicArgs
	 *     Map of dynamic arguments
	 * @return Options#DYNAMIC_ARGS
	 */
	public OptionsBuilder dynamicArgs(Map<String, Object> dynamicArgs){
	    options.setDynamicArgs(dynamicArgs);
	    return this;
	}

	/**
	 * Sets the execution directory
	 *
	 * @param executionDirectory
	 *            execution directory
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#EXECUTION_DIRECTORY
	 */
	public OptionsBuilder executionDirectory(File executionDirectory) {
		options.setExecutionDirectory(executionDirectory);
		return this;
	}
	
	/**
	 * Sets the configuration format (xml or yml)
	 * @param configurationFormat
	 * 			configuration format (file extension)
	 * 
	 * @return updated OptionBuilder instance
	 * 
	 * @see Options#CONFIGURATION_FILE_FORMAT
	 */
	public OptionsBuilder configurationFormat(String configurationFormat){
		options.setConfigurationFormat(configurationFormat);
		return this;
	}

	/**
	 * 
	 * @return the current built options
	 */
	public Options build() {
		return options;
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
