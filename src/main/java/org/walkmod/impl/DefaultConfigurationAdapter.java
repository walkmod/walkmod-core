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
package org.walkmod.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.walkmod.ConfigurationAdapter;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.merger.MergeEngine;
import org.walkmod.merger.MergePolicy;

public class DefaultConfigurationAdapter implements ConfigurationAdapter {

	private Configuration config;

	@Override
	public void setConfiguration(Configuration config) {
		this.config = config;
	}

	@Override
	public Configuration getConfiguration() {
		return config;
	}

	@Override
	public void prepare() {

		Collection<ProviderConfig> providers = config.getProviderConfigurations();

		if (providers != null) {
			for (ProviderConfig pc : providers) {
				Object aux = config.getBean(pc.getType(), pc.getParameters());
				if (aux instanceof ConfigurationProvider) {
					ConfigurationProvider cp = ((ConfigurationProvider) aux);
					cp.init(config);
					cp.load();

				}
			}
		}

		Collection<MergePolicyConfig> mergePolicies = config.getMergePolicies();
		if (mergePolicies != null) {
			Map<String, MergeEngine> mergeEngines = new HashMap<String, MergeEngine>();
			config.setMergeEngines(mergeEngines);
			for (MergePolicyConfig mpc : mergePolicies) {
				MergeEngine me = new MergeEngine();
				mergeEngines.put(mpc.getName(), me);
				String dopTypeLabel = mpc.getDefaultObjectPolicy();
				Object dop = null;
				Object top = null;
				if (dopTypeLabel != null) {
					dop = config.getBean(dopTypeLabel, null);
				}
				if (dop != null && dop instanceof MergePolicy<?>) {
					me.setDefaultObjectMergePolicy((MergePolicy) dop);
				}
				String topTypeLabel = mpc.getDefaultTypePolicy();
				if (topTypeLabel != null) {
					top = config.getBean(topTypeLabel, null);
				}
				if ((top != null) && top instanceof MergePolicy<?>) {
					me.setDefaultTypeMergePolicy((MergePolicy) top);
				}
				Map<String, String> policyEntries = mpc.getPolicyEntries();
				Class<?> oType = null;
				Object pType = null;
				Map<Class<?>, MergePolicy> resolvedEntries = new HashMap<Class<?>, MergePolicy>();
				if (policyEntries != null && !policyEntries.isEmpty()) {
					for (Map.Entry<String, String> entry : policyEntries.entrySet()) {
						try {
							oType = config.getClassLoader().loadClass(entry.getKey());
						} catch (ClassNotFoundException e) {
							throw new WalkModException("Invalid policy entry for " + entry.getKey());
						}
						pType = config.getBean(entry.getValue(), null);
						if (pType instanceof MergePolicy) {
							resolvedEntries.put(oType, (MergePolicy) pType);
						}
					}
				}
				me.setPolicyConfiguration(resolvedEntries);
			}
		}
	}
}
