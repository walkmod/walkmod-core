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
package org.walkmod.writers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Summary {

	private static Summary instance = null;

	private List<File> writtenFiles;

	private Summary() {
		writtenFiles = new LinkedList<File>();
	}

	public static Summary getInstance() {
		if (instance == null) {
			instance = new Summary();
		}
		return instance;
	}

	public void clear() {
		writtenFiles.clear();
	}

	public List<File> getWrittenFiles() {
		return writtenFiles;
	}

	public void addFile(File file) {
		writtenFiles.add(file);
	}
}
