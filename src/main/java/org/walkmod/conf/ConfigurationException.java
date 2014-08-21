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
package org.walkmod.conf;

public class ConfigurationException extends RuntimeException {

	/**
	 * serial id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>ConfigurationException</code> with the specified
	 * detail message.
	 * 
	 * @param s
	 *            the detail message.
	 */
	public ConfigurationException(String string) {
		super(string);
	}

	/**
	 * Constructs a <code>ConfigurationException</code> with the specified
	 * detail message and exception cause.
	 * 
	 * @param s
	 *            the detail message.
	 * @param cause
	 *            the wrapped exception
	 */
	public ConfigurationException(String s, Throwable cause) {
		super(s, cause);
		if (cause != null) {
			this.setStackTrace(cause.getStackTrace());
		}
	}
}
