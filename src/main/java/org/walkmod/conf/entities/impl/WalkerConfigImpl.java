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

import java.util.List;
import java.util.Map;
import org.walkmod.ChainWalker;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;

public class WalkerConfigImpl implements WalkerConfig {

	private String type;

	private List<TransformationConfig> transformations;

	private Map<String, Object> params;

	private String rootNamespace;

	private ChainConfig architectureConfig;

	private ChainWalker walker;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public List<TransformationConfig> getTransformations() {
		return transformations;
	}

	@Override
	public void setTransformations(List<TransformationConfig> transformations) {
		this.transformations = transformations;
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
	public String getRootNamespace() {
		return rootNamespace;
	}

	@Override
	public void setRootNamespace(String rootNamespace) {
		this.rootNamespace = rootNamespace;
	}

	@Override
	public void setChainConfig(ChainConfig architectureConfig) {
		this.architectureConfig = architectureConfig;
	}

	@Override
	public ChainConfig getChainConfig() {
		return architectureConfig;
	}

	public ChainWalker getWalker() {
		return walker;
	}

	public void setWalker(ChainWalker walker) {
		this.walker = walker;
	}
}
