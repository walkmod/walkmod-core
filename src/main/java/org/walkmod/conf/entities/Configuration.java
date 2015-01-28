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
package org.walkmod.conf.entities;

import java.util.Collection;
import java.util.Map;
import org.springframework.beans.factory.BeanFactory;
import org.walkmod.merger.MergeEngine;
import org.walkmod.walkers.VisitorMessage;

public interface Configuration {

	public Map<String, Object> getParameters();

	public void setParameters(Map<String, Object> parameters);

	public Collection<ChainConfig> getChainConfigs();

	public Collection<PluginConfig> getPlugins();

	public void setPlugins(Collection<PluginConfig> plugins);

	public void setChainConfigs(Collection<ChainConfig> chainConfigs);

	public boolean addChainConfig(ChainConfig architecture);

	public void setBeanFactory(BeanFactory beanFactory);

	public Object getBean(String name, Map<?, ?> parameters);

	public Collection<VisitorMessage> getVisitorMessages();

	public ClassLoader getClassLoader();

	public void setClassLoader(ClassLoader classLoader);

	public Collection<MergePolicyConfig> getMergePolicies();

	public void setMergePolicies(Collection<MergePolicyConfig> mergePolicies);

	public void setMergeEngines(Map<String, MergeEngine> mergeEngines);

	public MergeEngine getMergeEngine(String name);

	public void populate(Object element, Map<?, ?> parameters);

	public String getDefaultLanguage();

	public void setDefaultLanguage(String language);

	public Collection<ProviderConfig> getProviderConfigurations();

	public void setProviderConfigurations(Collection<ProviderConfig> providers);
}
