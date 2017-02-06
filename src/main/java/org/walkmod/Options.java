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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to create Walkmos options map
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
	 * Stored options
	 */
	private Map<String, Object> options = new HashMap<String, Object>();

	/**
	 * Creates an empty set of options
	 */
	public Options() {
		this(null);
	}

	/**
	 * Creates a set of options with initialized values
	 *
	 * @param options
	 *            Already initialized options
	 */
	public Options(Map<String, Object> options) {
		if (options != null) {
			this.options.putAll(options);
			if (!options.containsKey(Options.OFFLINE))
				setOffline(false);
			if (!options.containsKey(Options.VERBOSE))
				setVerbose(true);
			if (!options.containsKey(Options.PRINT_ERRORS))
				setPrintErrors(false);
			if (!options.containsKey(Options.THROW_EXCEPTION))
				setThrowException(false);
			if (!options.containsKey(EXECUTION_DIRECTORY)) {
				setExecutionDirectory(new File(System.getProperty("user.dir")));
			}
			if (!options.containsKey(CONFIGURATION_FILE_FORMAT)) {
				setConfigurationFormat("xml");
			}
			
		} else {
			setOffline(false);
			setVerbose(true);
			setPrintErrors(false);
			setThrowException(false);
			setExecutionDirectory(new File(System.getProperty("user.dir")));
			setConfigurationFormat("xml");
		}
	}

	public void setConfigurationFormat(String format) {
		String aux = format.toLowerCase().trim();
		if(aux.equals("yaml") || aux.equals("json")){
			aux = "yml";
		}
		if (aux.equals("xml") || aux.equals("yml")) {
			this.options.put(CONFIGURATION_FILE_FORMAT, aux);
		} else {
			throw new IllegalArgumentException("The configuration format "+aux+" is not supported");
		}
	}

	public String getConfigurationFormat() {
		Object value = this.options.get(CONFIGURATION_FILE_FORMAT);
		if (value == null) {
			value = "xml";
		}
		return value.toString();
	}

	public void setOffline(boolean offline) {
		this.options.put(OFFLINE, Boolean.valueOf(offline));
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
	
	public void setPath(String path){
	    this.options.put(CHAIN_PATH, path);
	}

	public void setVerbose(boolean verbose) {
		this.options.put(VERBOSE, Boolean.valueOf(verbose));
	}

	public boolean isVerbose() {
		Object value = this.options.get(VERBOSE);
		return value != null && (Boolean) value;
	}

	public void setPrintErrors(boolean printErrors) {
		this.options.put(PRINT_ERRORS, Boolean.valueOf(printErrors));
	}

	public void setExecutionDirectory(File executionDirectory) {
		options.put(EXECUTION_DIRECTORY, executionDirectory);
	}
	
	public void setDynamicArgs(Map<String, Object> dynamicArgs){
	    options.put(DYNAMIC_ARGS, dynamicArgs);
	}
	
	public Map<String, Object> getDynamicArgs(){
	    Object value = options.get(DYNAMIC_ARGS);
	    return  (Map<String, Object>) value;
	}

	public File getExecutionDirectory() {
		return (File) options.get(EXECUTION_DIRECTORY);
	}

	public boolean isPrintErrors() {
		Object value = this.options.get(PRINT_ERRORS);
		return value != null && (Boolean) value;
	}

	public void setThrowException(boolean throwException) {
		this.options.put(THROW_EXCEPTION, Boolean.valueOf(throwException));
	}

	public boolean isThrowException() {
		Object value = this.options.get(THROW_EXCEPTION);
		return value != null && (Boolean) value;
	}

	@SuppressWarnings("unchecked")
	public void setIncludes(String... includes) {

		if (includes != null) {
			if (!this.options.containsKey(INCLUDES)) {
				this.options.put(INCLUDES, new ArrayList<Object>());
			}

			List<Object> allIncludes = (List<Object>) this.options.get(INCLUDES);
			allIncludes.addAll(Arrays.asList(includes));
		}
	}

	@SuppressWarnings("unchecked")
	public void setExcludes(String... excludes) {

		if (excludes != null) {
			if (!this.options.containsKey(EXCLUDES)) {
				this.options.put(EXCLUDES, new ArrayList<Object>());
			}

			List<Object> allIncludes = (List<Object>) this.options.get(EXCLUDES);
			allIncludes.addAll(Arrays.asList(excludes));
		}
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
	 * Returns the stored options as a Map&lt;String,Object&gt;
	 *
	 * @return map with options
	 */
	public Map<String, Object> asMap() {
		return this.options;
	}
}
