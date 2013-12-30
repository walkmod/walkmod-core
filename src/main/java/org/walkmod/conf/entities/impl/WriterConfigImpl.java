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
package org.walkmod.conf.entities.impl;

import java.util.Map;
import org.walkmod.ChainWriter;
import org.walkmod.conf.entities.WriterConfig;

public class WriterConfigImpl implements WriterConfig {

	private String path;

	private String type;
	
	private String[] excludes;
	
	private String[] includes;

	private Map<String, Object> params;

	private ChainWriter modelWriter;

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Map<String, Object> getParams() {
		return params;
	}

	@Override
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}


	@Override
	public ChainWriter getModelWriter() {
		return modelWriter;
	}

	@Override
	public void setModelWriter(ChainWriter modelWriter) {
		this.modelWriter = modelWriter;
	}

	@Override
	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	@Override
	public String[] getExcludes() {
		
		return excludes;
	}

	@Override
	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	@Override
	public String[] getIncludes() {		
		return includes;
	}
}
