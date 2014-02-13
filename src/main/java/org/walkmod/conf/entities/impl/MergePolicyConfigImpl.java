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
import org.walkmod.conf.entities.MergePolicyConfig;

public class MergePolicyConfigImpl implements MergePolicyConfig {

	private String defaultObjectPolicy;

	private String defaultTypePolicy;

	private String name;

	private Map<String, String> policyEntries;

	@Override
	public void setDefaultObjectPolicy(String defaultObjectPolicy) {
		this.defaultObjectPolicy = defaultObjectPolicy;
	}

	@Override
	public void setDefaultTypePolicy(String defaultTypePolicy) {
		this.defaultTypePolicy = defaultTypePolicy;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDefaultTypePolicy() {
		return defaultTypePolicy;
	}

	@Override
	public String getDefaultObjectPolicy() {
		return defaultObjectPolicy;
	}

	@Override
	public void setPolicyEntries(Map<String, String> policyEntries) {
		this.policyEntries = policyEntries;
	}

	@Override
	public Map<String, String> getPolicyEntries() {
		return policyEntries;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MergePolicyConfig) {
			String n1 = getName();
			String n2 = ((MergePolicyConfig) o).getName();
			return n1 != null && n2 != null && n1.equals(n2);
		}
		return false;
	}
}
