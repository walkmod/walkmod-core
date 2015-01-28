package org.walkmod.conf.entities.impl;

import java.util.Map;

import org.walkmod.conf.entities.ProviderConfig;

public class ProviderConfigImpl implements ProviderConfig {

	private String type;

	private Map<String, Object> parameters;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

}
