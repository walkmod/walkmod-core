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
package org.walkmod.readers;

import java.io.File;

import org.walkmod.ChainReader;
import org.walkmod.Resource;
import org.walkmod.util.FileResource;

public class DefaultFileReader extends ChainReader {

	private String[] extensions;

	public String[] getExtensions() {
		return extensions;
	}

	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	@Override
	public Resource<File> read() throws Exception {
		FileResource resource = new FileResource();
		resource.setPath(getPath());
		resource.setExtensions(getExtensions());
		resource.setExcludes(getExcludes());
		resource.setIncludes(getIncludes());
		return resource;
	}
}
