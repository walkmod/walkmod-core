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
package org.walkmod.patches;

public enum PatchFormat {

	JSON("json", new JsonPatchFormatter()), RAW("raw", new RawPatchFormatter());

	private final String name;

	private final PatchFormatter formatter;

	private PatchFormat(String s, PatchFormatter f) {
		name = s;
		formatter = f;
	}

	@Override
	public String toString() {
		return name;
	}

	public PatchFormatter getFormatter() {
		return formatter;
	}

}
