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

import org.walkmod.conf.entities.PluginConfig;

public class PluginConfigImpl implements PluginConfig {

	private String groupId;

	private String artifactId;

	private String version;
	
	public PluginConfigImpl(){
		
	}
	
	public PluginConfigImpl(String plugin){
		String[] parts = plugin.split(":");
		boolean valid = parts.length <= 3;
		for (int i = 0; i < parts.length && valid; i++) {
			valid = !parts[i].trim().equals("");
		}

		if (valid) {
			if (parts.length == 1) {
				setGroupId("org.walkmod");
				String artifactId = parts[0].trim();
				if(!artifactId.startsWith("walkmod-")){
					artifactId = "walkmod-"+artifactId;
				}
				if(!artifactId.endsWith("-plugin")){
					artifactId = artifactId+"-plugin";
				}
				setArtifactId(artifactId);
				setVersion("latest.integration");
			} else if (parts.length == 2) {
				setGroupId(parts[0].trim());
				setArtifactId(parts[1].trim());
				setVersion("latest.integration");
			} else {
				setGroupId(parts[0].trim());
				setArtifactId(parts[1].trim());
				setVersion(parts[2].trim());
			}
		} else {
			throw new IllegalArgumentException(
					"The plugin identifier is not well defined. The expected format is [groupId:artifactId:version]");
		}
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object pc) {
		if (pc instanceof PluginConfig) {
			if (groupId != null && artifactId != null) {
				PluginConfig aux = (PluginConfig) pc;
				return groupId.equals(aux.getGroupId()) && artifactId.equals(aux.getArtifactId());
			}
			return false;
		}
		return false;

	}
}
