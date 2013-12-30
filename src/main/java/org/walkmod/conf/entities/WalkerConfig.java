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
package org.walkmod.conf.entities;

import java.util.List;
import java.util.Map;

import org.walkmod.ChainWalker;

public interface WalkerConfig {

	public String getType();

	public void setType(String type);

	public List<TransformationConfig> getTransformations();

	public void setTransformations(
			List<TransformationConfig> transformations);

	public Map<String, Object> getParams();

	public void setParams(Map<String, Object> params);

	public String getRootNamespace();

	public void setRootNamespace(String rootNamespace);

	public void setArchitectureConfig(ChainConfig architectureConfig);

	public ChainConfig getArchitectureConfig();

	public ChainWalker getWalker();

	public void setWalker(ChainWalker walker);
}
