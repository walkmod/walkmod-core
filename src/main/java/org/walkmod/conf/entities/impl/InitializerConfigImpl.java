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

import org.walkmod.conf.entities.InitializerConfig;

public class InitializerConfigImpl implements InitializerConfig {

	private String type;

	private Map<String, Object> params;

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Map<String, Object> getParams() {
		return params;
	}

	@Override
	public String getPluginArtifactId() {

		return type.substring(0, type.indexOf("-initializer"));
	}

	@Override
	public String getPluginGroupId() {

		return "org.walkmod";
	}

}
