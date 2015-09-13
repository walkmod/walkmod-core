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
		}
		else{
			setOffline(false);
			setVerbose(true);
			setPrintErrors(false);
			setThrowException(false);
		}
	}

	public void setOffline(boolean offline) {
		this.options.put(OFFLINE, Boolean.valueOf(offline));
	}

	public boolean isOffline() {
		Object value = this.options.get(OFFLINE);
		return value != null && (Boolean) value;

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

		if (!this.options.containsKey(INCLUDES)) {
			this.options.put(INCLUDES, new ArrayList<Object>());
		}

		List<Object> allIncludes = (List<Object>) this.options.get(INCLUDES);
		allIncludes.addAll(Arrays.asList(includes));
	}

	@SuppressWarnings("unchecked")
	public void setExcludes(String... excludes) {

		if (!this.options.containsKey(EXCLUDES)) {
			this.options.put(EXCLUDES, new ArrayList<Object>());
		}

		List<Object> allIncludes = (List<Object>) this.options.get(EXCLUDES);
		allIncludes.addAll(Arrays.asList(excludes));
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
