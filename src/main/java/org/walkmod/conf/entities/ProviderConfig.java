package org.walkmod.conf.entities;

import java.util.Map;

public interface ProviderConfig {

	public String getType();

	public void setType(String type);

	public Map<String, Object> getParameters();

	public void setParameters(Map<String, Object> parameters);
	
}
