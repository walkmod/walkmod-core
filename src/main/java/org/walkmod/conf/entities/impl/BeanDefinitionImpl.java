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

import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.PropertyDefinition;

public class BeanDefinitionImpl implements BeanDefinition{
	
	private String category;
	
	private String type;
	
	private String description ="unavailable";
	
	private  List<PropertyDefinition> properties;
	
	public BeanDefinitionImpl(String category, String type, String description, List<PropertyDefinition> properties){
		this.category = category;
		this.type = type;
		if(description != null){
			this.description = description;
		}
		this.properties = properties;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public List<PropertyDefinition> getProperties() {
		return properties;
	}

}
