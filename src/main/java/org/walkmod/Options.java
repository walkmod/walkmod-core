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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to create Walkmod options map
 *
 * Inspired by Asciidoctor's Options
 *
 * @author abelsromero
 */
public class Options {

	/**
	 * (Boolean) Disables/enables remote fetching of plugins
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
	 * (Boolean) Disables/enables the capture of original exceptions (requires
	 * verbose = true)
	 */
	public static final String THROW_EXCEPTION = "throw_exception";
	/**
	 * (List&lt;String&gt;) Overwrites the include rules in the chain's reader
	 */
	public static final String INCLUDES = "includes";
	/**
	 * (List&lt;String&gt;) Overwrites the exclude rules in the chain's reader
	 */
	public static final String EXCLUDES = "excludes";

	/**
	 * (File) Sets an execution directory. The default value is the current
	 * user.dir
	 */
	public static final String EXECUTION_DIRECTORY = "execution_directory";

	/**
	 * (File, optional) Name of the walkmod configuration file
	 */
	public static final String CONFIGURATION_FILE = "config_file";

	/**
	 * (String) File extension of the walkmod configuration
	 */
	public static final String CONFIGURATION_FILE_FORMAT = "format";

	/**
     * (Map) Extra arguments for the executed components
     */
    public static final String DYNAMIC_ARGS = "dynamic_args";

    /**
     * (String) Selected path value for the reader and writer
     */
    public static final String CHAIN_PATH = "chain_path";

	/**
	 * Stored options as immutable map.
	 */
	private final Map<String, Object> options;

	/**
	 * Creates a set of options with initialized values
	 *
	 * @param optionsArg
	 *            Already initialized options
	 */
	Options(Map<String, Object> optionsArg) {
		final Map<String, Object> options = new HashMap<String, Object>(optionsArg);
		makeImmutableListCopy(options, EXCLUDES);
		makeImmutableListCopy(options, INCLUDES);
		makeImmutableMapCopy(options, DYNAMIC_ARGS);
		checkPresent(options, OFFLINE);
		checkPresent(options, VERBOSE);
		checkPresent(options, PRINT_ERRORS);
		checkPresent(options, THROW_EXCEPTION);
		checkPresent(options, EXECUTION_DIRECTORY);
		checkPresent(options, CONFIGURATION_FILE_FORMAT);
		this.options = Collections.unmodifiableMap(options);
	}

	private static void makeImmutableListCopy(Map<String, Object> options, final String parameterKey) {
		final Object o = options.get(parameterKey);
		if (o != null) {
			options.put(parameterKey, Collections.unmodifiableList(new ArrayList<String>((List<String>) o)));
		}
	}

	private static void makeImmutableMapCopy(Map<String, Object> options, final String parameterKey) {
		final Object o = options.get(parameterKey);
		if (o != null) {
			options.put(parameterKey, Collections.unmodifiableMap(new HashMap<String, Object>((Map<String, Object>) o)));
		}
	}

	private void checkPresent(Map<String, Object> options, final String parameterKey) {
		if (!options.containsKey(parameterKey)) {
			throw new IllegalArgumentException("missing configuration parameter: " + parameterKey);
		}
	}

	/* @Nullable */ public File getConfigurationFile() {
		return (File) this.options.get(CONFIGURATION_FILE);
	}

	public String getConfigurationFormat() {
		return this.options.get(CONFIGURATION_FILE_FORMAT).toString();
	}

	public boolean isOffline() {
		Object value = this.options.get(OFFLINE);
		return value != null && (Boolean) value;

	}

	public String getPath(){
	    Object path = this.options.get(CHAIN_PATH);
	    if(path != null){
	        return path.toString();
	    }
	    return null;
	}

	public boolean isVerbose() {
		Object value = this.options.get(VERBOSE);
		return value != null && (Boolean) value;
	}

	/**
	 * @return immutable map or null
	 */
	public Map<String, Object> getDynamicArgs(){
		return  (Map<String, Object>) options.get(DYNAMIC_ARGS);
	}

	/**
	 * @return mutable map or null
	 */
	public Map<String, Object> getMutableCopyOfDynamicArgs() {
		final Map<String, Object> m = (Map<String, Object>) options.get(DYNAMIC_ARGS);
		return m != null ? new HashMap<String, Object>(m) : null;
	}

	public File getExecutionDirectory() {
		return (File) options.get(EXECUTION_DIRECTORY);
	}

	public boolean isPrintErrors() {
		Object value = this.options.get(PRINT_ERRORS);
		return value != null && (Boolean) value;
	}

	public boolean isThrowException() {
		Object value = this.options.get(THROW_EXCEPTION);
		return value != null && (Boolean) value;
	}

	@SuppressWarnings("unchecked")
	public List<String> getExcludes() {
		if (options.containsKey(EXCLUDES)) {
			return (List<String>) options.get(EXCLUDES);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<String> getIncludes() {
		if (options.containsKey(INCLUDES)) {
			return (List<String>) options.get(INCLUDES);
		}
		return null;
	}

	/**
	 * Returns the stored options as a immutable Map&lt;String,Object&gt;
	 *
	 * @return immutable map with options
	 */
	public Map<String, Object> asMap() {
		return this.options;
	}
}
