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
package org.walkmod.conf;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;

public interface ProjectConfigurationProvider extends ConfigurationProvider{
	
	public boolean addPluginConfig(PluginConfig pluginConfig) throws TransformerException;
	
	public boolean addChainConfig(ChainConfig chainCfg) throws TransformerException;

	public boolean addTransformationConfig(String chain, TransformationConfig transformationCfg) throws TransformerException;
	
	public void createConfig() throws IOException;

	public boolean addProviderConfig(ProviderConfig providerCfg) throws TransformerException;
	

}
