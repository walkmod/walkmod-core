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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.walkmod.ChainReader;
import org.walkmod.ChainWalker;
import org.walkmod.ChainWriter;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.Initializer;
import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.InitializerConfig;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.PropertyDefinition;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.merger.MergeEngine;
import org.walkmod.merger.MergePolicy;
import org.walkmod.walkers.VisitorMessage;

public class ConfigurationImpl implements Configuration {

	private Map<String, Object> parameters;

	private Map<String, ChainConfig> chainConfigs;

	private BeanFactory beanFactory;

	private Collection<PluginConfig> plugins;

	private ClassLoader classLoader = null;

	private Collection<MergePolicyConfig> mergePolicies;

	private Map<String, MergeEngine> mergeEngines;

	private String defaultLanguage;

	private Collection<ProviderConfig> providers;

	private List<InitializerConfig> initializers;

	private List<String> modules;

	private BeanDefinitionRegistry beanDefinitionRegistry;

	public ConfigurationImpl() {
		this.parameters = new LinkedHashMap<String, Object>();
		this.chainConfigs = new LinkedHashMap<String, ChainConfig>();
		this.mergeEngines = new LinkedHashMap<String, MergeEngine>();
		this.beanFactory = null;
		this.defaultLanguage = null;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public Collection<ChainConfig> getChainConfigs() {
		return chainConfigs.values();
	}

	public void setChainConfigs(Collection<ChainConfig> chainConfigs) {

		this.chainConfigs.clear();
		Iterator<ChainConfig> it = chainConfigs.iterator();
		while (it.hasNext()) {
			ChainConfig current = it.next();
			current.setConfiguration(this);
			this.chainConfigs.put(current.getName(), current);
		}
	}

	public boolean addChainConfig(ChainConfig architecture) {
		boolean result = chainConfigs.containsKey(architecture.getName());
		if (!result) {
			architecture.setConfiguration(this);
			chainConfigs.put(architecture.getName(), architecture);

		}
		return result;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	@Override
	public Object getBean(String name, Map<?, ?> parameters) {
		Object result = null;
		if (name == null || "".equals(name)) {
			return result;
		}
		if (beanFactory != null && beanFactory.containsBean(name)) {
			result = beanFactory.getBean(name);
		}
		if (!name.contains(":")) {
			result = beanFactory.getBean("org.walkmod:walkmod-" + name + "-plugin:" + name);
		} else {
			String[] parts = name.split(":");
			if (parts.length == 2) {
				String pluginId = parts[0].trim();
				String beanId = parts[1].trim();
				if (pluginId.length() > 0 && beanId.length() > 0) {
					result = beanFactory.getBean("org.walkmod:walkmod-" + pluginId + "-plugin:" + beanId);
				}
			}
		}
		if (result == null) {
			try {
				Class<?> clazz = getClassLoader().loadClass(name);
				result = clazz.newInstance();
			} catch (Exception e) {
				throw new WalkModException(
						"Sorry, it is impossible to load the bean "
								+ name
								+ ". Please, assure that it is a valid class name and the library which contains it is in the classpath",
						e);
			}
		}
		if (result != null) {
			BeanWrapper bw = new BeanWrapperImpl(result);
			if (this.parameters != null) {
				MutablePropertyValues pvs = new MutablePropertyValues(this.parameters);
				bw.setPropertyValues(pvs, true, true);
			}
			if (parameters != null) {
				MutablePropertyValues pvs = new MutablePropertyValues(parameters);
				bw.setPropertyValues(pvs, true, true);
			}
		}
		return result;
	}

	public void populate(Object element, Map<?, ?> parameters) {
		if (element != null) {
			BeanWrapper bw = new BeanWrapperImpl(element);
			if (this.parameters != null) {
				bw.setPropertyValues(this.parameters);
			}
			bw.setPropertyValues(parameters);
		}
	}

	public Collection<VisitorMessage> getVisitorMessages() {
		Collection<VisitorMessage> result = new LinkedList<VisitorMessage>();
		if (getChainConfigs() != null) {
			for (ChainConfig aqConfig : getChainConfigs()) {
				result.addAll(aqConfig.getWalkerConfig().getWalker().getVisitorMessages());
			}
		}
		return result;
	}

	@Override
	public Collection<PluginConfig> getPlugins() {

		return plugins;
	}

	@Override
	public void setPlugins(Collection<PluginConfig> plugins) {
		this.plugins = plugins;
	}

	@Override
	public ClassLoader getClassLoader() {
		if (classLoader == null) {
			return Thread.currentThread().getContextClassLoader();
		}
		return classLoader;
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public Collection<MergePolicyConfig> getMergePolicies() {
		return mergePolicies;
	}

	@Override
	public void setMergePolicies(Collection<MergePolicyConfig> mergePolicies) {
		this.mergePolicies = mergePolicies;
	}

	@Override
	public void setMergeEngines(Map<String, MergeEngine> mergeEngines) {
		this.mergeEngines = mergeEngines;
	}

	@Override
	public MergeEngine getMergeEngine(String name) {
		return mergeEngines.get(name);
	}

	@Override
	public String getDefaultLanguage() {

		return defaultLanguage;
	}

	@Override
	public void setDefaultLanguage(String defaults) {
		this.defaultLanguage = defaults;
	}

	@Override
	public Collection<ProviderConfig> getProviderConfigurations() {
		return providers;
	}

	@Override
	public void setProviderConfigurations(Collection<ProviderConfig> providers) {
		this.providers = providers;
	}

	@Override
	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	@Override
	public List<String> getModules() {
		return modules;
	}

	@Override
	public void setInitializers(List<InitializerConfig> initializers) {
		this.initializers = initializers;
	}

	@Override
	public List<InitializerConfig> getInitializers() {
		return initializers;
	}

	@Override
	public boolean containsBean(String beanId) {
		if (beanFactory != null) {
			if (!beanId.contains(":")) {
				return beanFactory.containsBean("org.walkmod:walkmod-" + beanId + "-plugin:" + beanId);
			}
			return beanFactory.containsBean(beanId);
		}
		return false;
	}

	public List<BeanDefinition> getAvailableBeans(PluginConfig pc) {
		List<BeanDefinition> result = new LinkedList<BeanDefinition>();

		String[] names = beanDefinitionRegistry.getBeanDefinitionNames();
		if (names != null) {
			for (String name : names) {
				if (name.startsWith(pc.getGroupId() + ":" + pc.getArtifactId())) {
					Object o = beanFactory.getBean(name);
					String classification = "transformation";
					if (o instanceof ChainReader) {
						classification = "reader";
					} else if (o instanceof ChainWriter) {
						classification = "writer";
					} else if (o instanceof ChainWalker) {
						classification = "walker";
					} else if (o instanceof Initializer) {
						classification = "initializer";
					} else if (o instanceof ConfigurationProvider) {
						classification = "conf-provider";
					} else if (o instanceof MergePolicy) {
						classification = "policy-entry";
					}

					String id = name;
					int index0 = "walkmod-".length();
					int index1 = pc.getArtifactId().indexOf("-plugin");
					if (index1 != -1) {
						id = pc.getArtifactId().substring(index0, index1);
						String uniqueFullName = pc.getGroupId() + ":" + pc.getArtifactId() + ":" + id;
						if (!name.equals(uniqueFullName)) {
							if (!beanDefinitionRegistry.isAlias(name)) {
								String[] aliases = beanDefinitionRegistry.getAliases(name);
								boolean add = true;
								if (aliases != null) {
									for (int i = 0; i < aliases.length && add; i++) {
										add = !(aliases[i].equals(uniqueFullName));
									}
								}
								if (add) {
									result.add(new BeanDefinitionImpl(classification, name, beanDefinitionRegistry
											.getBeanDefinition(name).getDescription(), getProperties(o)));
								} else {
									result.add(new BeanDefinitionImpl(classification, id, beanDefinitionRegistry
											.getBeanDefinition(name).getDescription(), getProperties(o)));
								}
							}
						} else {
							result.add(new BeanDefinitionImpl(classification, id, beanDefinitionRegistry
									.getBeanDefinition(name).getDescription(), getProperties(o)));
						}
					}

				}
			}
		}
		return result;
	}

	private List<PropertyDefinition> getProperties(Object o) {
		List<PropertyDefinition> result = new LinkedList<PropertyDefinition>();
		PropertyDescriptor[] properties = BeanUtils.getPropertyDescriptors(o.getClass());
		if (properties != null) {
			for (PropertyDescriptor pd : properties) {
				if (pd.getWriteMethod() != null) {
					String name = pd.getDisplayName();
					Class<?> clazz = pd.getPropertyType();
					String type = clazz.getSimpleName();
					String value = "";
					if (String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
							|| clazz.isPrimitive()) {
						if (pd.getReadMethod() != null) {
							try {
								value = pd.getReadMethod().invoke(o).toString();
							} catch (Exception e) {
							}
						} else {
							Field[] fields = o.getClass().getDeclaredFields();
							boolean found = false;
							for (int i = 0; i < fields.length && !found; i++) {
								if (fields[i].getName().equals(name)) {
									found = true;
									fields[i].setAccessible(true);
									try {
										value = fields[i].get(o).toString();
									} catch (Exception e) {
									}
								}
							}
						}
					}

					PropertyDefinition item = new PropertyDefinitionImpl(type, name, value);

					result.add(item);
				}
			}
		}
		return result;
	}

	@Override
	public void setBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
		this.beanDefinitionRegistry = beanDefinitionRegistry;
	}

}
