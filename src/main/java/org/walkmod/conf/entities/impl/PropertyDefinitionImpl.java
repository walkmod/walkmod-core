package org.walkmod.conf.entities.impl;

import org.walkmod.conf.entities.PropertyDefinition;

public class PropertyDefinitionImpl implements PropertyDefinition{
	
	private String type;
	
	private String name;
	
	private String defaultValue;
	
	public PropertyDefinitionImpl(String type, String name, String defaultValue){
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

}
