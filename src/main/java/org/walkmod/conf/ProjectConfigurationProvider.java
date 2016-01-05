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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;

public interface ProjectConfigurationProvider extends ConfigurationProvider {

	public void addPluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception;

	public void addChainConfig(ChainConfig chainCfg, boolean recursive, String before) throws Exception;

	public void addTransformationConfig(String chain, String path, TransformationConfig transformationCfg,
			boolean recursive, Integer order, String before) throws Exception;

	public void createConfig() throws IOException;

	public void addProviderConfig(ProviderConfig providerCfg, boolean recursive) throws Exception;

	public void addModules(List<String> modules) throws Exception;

	public void removeTransformations(String chain, List<String> transformations, boolean recursive) throws Exception;

	public void setWriter(String chain, String type, String path, boolean recursive, Map<String, String> params) throws Exception;

	public void setReader(String chain, String type, String path, boolean recursive, Map<String, String> params) throws Exception;

	public void removePluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception;

	public void removeModules(List<String> modules) throws Exception;

	public void removeProviders(List<String> providers, boolean recursive) throws Exception;

	public void removeChains(List<String> chains, boolean recursive) throws Exception;

	public void addConfigurationParameter(String param, String value, String type, String category, String name,
			String chain, boolean recursive) throws Exception;

	public String getFileExtension();

	public Configuration getConfiguration();

	public File getConfigurationFile();

	public ProjectConfigurationProvider clone(File cfgFile);

   public void addIncludesToChain(String chain, List<String> includes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception;

   public void addExcludesToChain(String chain, List<String> excludes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception;

   public void removeExcludesFromChain(String chain, List<String> excludes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception;

   public void removeIncludesFromChain(String chain, List<String> includes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception;


}
