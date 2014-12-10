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
package org.walkmod.util.location;

import java.util.List;

public interface Location {

	/**
	 * Get the description of this location
	 * @return the description (can be <code>null</code>)
	 */
	public String getDescription();

	/**
	 * Get the URI of this location
	 * @return the URI (<code>null</code> if unknown).
	 */
	public String getURI();

	/**
	 * Get the line number of this location
	 * @return the line number (<code>-1</code> if unknown)
	 */
	public int getLineNumber();

	/**
	 * Get the column number of this location
	 * @return the column number (<code>-1</code> if unknown)
	 */
	public int getColumnNumber();

	/**
	 * Gets a source code snippet with the default padding
	 * @param padding padding The amount of lines before and after the error to include
	 * @return A list of source lines
	 */
	public List<String> getSnippet(int padding);
}
