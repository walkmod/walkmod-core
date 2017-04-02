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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	private static final String DEFAULT_PATH = ".";

	/**
	 * Collected options
	 */
	private final Map<String, Object> options = new HashMap<String, Object>();

	/**
	 * Creates an OptionBuilder with the default options
	 */
	private OptionsBuilder() {
		this(Collections.<String, Object>emptyMap());

	}

	/**
	 * Creates an OptionBuilder with the initialization options
	 */
	private OptionsBuilder(Map<String, Object> optionsArg) {
		// default values
		options.put(Options.OFFLINE, false);
		options.put(Options.VERBOSE, true);
		options.put(Options.PRINT_ERRORS, false);
		options.put(Options.THROW_EXCEPTION, false);
		options.put(Options.EXECUTION_DIRECTORY, defaultExecutionDirectory());
		options.put(Options.CONFIGURATION_FILE_FORMAT, "xml");
		options.put(Options.CHAIN_PATH, DEFAULT_PATH);
		options.putAll(optionsArg);
	}

	private static File defaultExecutionDirectory() {
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Creates an Options instance with the default options:
	 *
	 * See {@link #options()}
	 *
	 * @return options builder instance
	 */
	public static Options defaultOptions() {
		return options().build();
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
	 * Creates an OptionBuilder instance with the given options:
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
	 * Creates an OptionBuilder instance with the given options:
	 *
	 * @param options
	 *            Map of starting options. See {@link Options} for available
	 *            keys
	 *
	 * @return options builder instance
	 */
	public static OptionsBuilder options(Options options) {
		return new OptionsBuilder(options.asMap());
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
		options.put(Options.OFFLINE, offline);
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
		options.put(Options.VERBOSE, verbose);
		return this;
	}
	
	/**
     * Sets the path option. Null value resets to default value.
     *
     * @param path
     *            directory to read and write from
     * @return updated OptionBuilder instance
     *
     * @see Options#VERBOSE
     */
    public OptionsBuilder path(String path) {
		options.put(Options.CHAIN_PATH, path != null ? path : DEFAULT_PATH);
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
		options.put(Options.PRINT_ERRORS, printErrors);
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
		this.options.put(Options.THROW_EXCEPTION, throwException);
		return this;
	}

	/**
	 * Adds to the includes option
	 *
	 * @param includes
	 *            List of included paths
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#INCLUDES
	 */
	@SuppressWarnings("unchecked")
	public OptionsBuilder includes(String... includes) {
		return includes != null ? includes(Arrays.asList(includes)) : this;
	}

	/**
	 * Adds to the includes option
	 *
	 * @param includes
	 *            List of included paths
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#INCLUDES
	 */
	@SuppressWarnings("unchecked")
	public OptionsBuilder includes(/* @Nullable */ Collection<String> includes) {

		if (includes != null && !includes.isEmpty()) {
			if (!options.containsKey(Options.INCLUDES)) {
				options.put(Options.INCLUDES, new ArrayList<Object>());
			}

			List<Object> allIncludes = (List<Object>) options.get(Options.INCLUDES);
			allIncludes.addAll(includes);
		}
		return this;
	}

	/**
	 * Adds to the excluded option
	 *
	 * @param excludes
	 *            List of excluded paths
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#EXCLUDES
	 */
	public OptionsBuilder excludes(String... excludes) {
		return excludes != null ? excludes(Arrays.asList(excludes)) : this;
	}

	/**
	 * Adds to the excluded option
	 *
	 * @param excludes
	 *            List of excluded paths
	 * @return updated OptionBuilder instance
	 *
	 * @see Options#EXCLUDES
	 */
	public OptionsBuilder excludes(/* @Nullable */ Collection<String> excludes) {

		if (excludes != null && !excludes.isEmpty()) {
			if (!options.containsKey(Options.EXCLUDES)) {
				options.put(Options.EXCLUDES, new ArrayList<Object>());
			}

			List<Object> allIncludes = (List<Object>) options.get(Options.EXCLUDES);
			allIncludes.addAll(excludes);
		}
		return this;
	}

	/**
	 * Seths the dynamic arguments
	 * @param dynamicArgs
	 *     Map of dynamic arguments
	 * @return Options#DYNAMIC_ARGS
	 */
	public OptionsBuilder dynamicArgs(Map<String, ?> dynamicArgs){
		final Map<String, Object> m = dynamicArgs != null
				? Collections.unmodifiableMap(new HashMap<String, Object>(dynamicArgs))
				: Collections.<String, Object>emptyMap();
		options.put(Options.DYNAMIC_ARGS, m);
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
		options.put(Options.EXECUTION_DIRECTORY, executionDirectory != null ? executionDirectory : defaultExecutionDirectory());
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
		String aux = configurationFormat.toLowerCase().trim();
		if(aux.equals("yaml") || aux.equals("json")){
			aux = "yml";
		}
		if (aux.equals("xml") || aux.equals("yml")) {
			options.put(Options.CONFIGURATION_FILE_FORMAT, aux);
		} else {
			throw new IllegalArgumentException("The configuration format "+aux+" is not supported");
		}
		return this;
	}

	/**
	 * 
	 * @return the current immutable built options
	 */
	public Options build() {
		return new Options(options);
	}
}
