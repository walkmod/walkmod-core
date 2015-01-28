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
import org.walkmod.conf.entities.TransformationConfig;

public class TransformationConfigImpl implements TransformationConfig {

	private String name;

	private String visitor;

	private Map<String, Object> parameters;

	private Object visitorInstance;

	private boolean isMergeable = false;

	private String mergePolicy;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getType() {
		return visitor;
	}

	@Override
	public void setType(String visitor) {
		this.visitor = visitor;
	}

	@Override
	public Object getVisitorInstance() {
		return visitorInstance;
	}

	@Override
	public void setVisitorInstance(Object visitorInstance) {
		this.visitorInstance = visitorInstance;
	}

	@Override
	public void isMergeable(boolean isMergeable) {
		this.isMergeable = isMergeable;
	}

	@Override
	public boolean isMergeable() {
		return isMergeable;
	}

	@Override
	public void setMergePolicy(String mergePolicy) {
		this.mergePolicy = mergePolicy;
	}

	@Override
	public String getMergePolicy() {
		return mergePolicy;
	}
}
