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
package org.walkmod.conf.providers;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.InitializerConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.InitializerConfigImpl;
import org.walkmod.conf.entities.impl.ParserConfigImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;

public class AbstractChainConfigurationProvider {

	public void addDefaultReaderConfig(ChainConfig ac) {
		ReaderConfig readerConfig = new ReaderConfig();
		readerConfig.setPath(null);
		readerConfig.setType(null);
		ac.setReaderConfig(readerConfig);
	}

	public void addDefaultWalker(ChainConfig ac) {
		WalkerConfig wc = new WalkerConfigImpl();
		wc.setType(null);
		wc.setParserConfig(new ParserConfigImpl());
		ac.setWalkerConfig(wc);
	}

	public void addDefaultWriterConfig(ChainConfig ac) {
		WriterConfig wc = new WriterConfigImpl();
		wc.setPath(ac.getReaderConfig().getPath());
		wc.setType(null);
		ac.setWriterConfig(wc);
	}

	public void inferInitializers(Configuration config) {
		InitializerConfig init = null;
		File pom = new File("pom.xml");
		if (pom.exists()) {
			init = new InitializerConfigImpl();
			init.setType("maven-initializer");
		} else {
			File gradle = new File("settings.gradle");
			if (gradle.exists()) {
				init = new InitializerConfigImpl();
				init.setType("gradle-initializer");
			}
		}
		if(init != null){
			List<InitializerConfig> list = config.getInitializers();
			if(list == null){
				list = new LinkedList<InitializerConfig>();
			}
			list.add(init);
			config.setInitializers(list);
		}
	}

	public void inferPlugins(Configuration config) {
		Collection<PluginConfig> pluginCfg = config.getPlugins();
		HashSet<String> plugins = new HashSet<String>();

		HashSet<String> previousPlugins = new HashSet<String>();

		if (pluginCfg == null || pluginCfg.isEmpty()) {
			pluginCfg = new LinkedList<PluginConfig>();
		} else {
			for (PluginConfig pc : pluginCfg) {
				previousPlugins.add(pc.getGroupId() + ":" + pc.getArtifactId());
			}
		}
		Collection<ChainConfig> chains = config.getChainConfigs();
		if (chains != null) {

			Iterator<ChainConfig> it = chains.iterator();
			while (it.hasNext()) {
				ChainConfig cc = it.next();
				composeName(cc.getReaderConfig().getType(), plugins);
				composeName(cc.getWalkerConfig().getParserConfig().getType(), plugins);
				List<TransformationConfig> trans = cc.getWalkerConfig().getTransformations();
				if (trans != null) {
					for (TransformationConfig transformation : trans) {
						String type = transformation.getType();
						composeName(type, plugins);
					}
				}
				composeName(cc.getWriterConfig().getType(), plugins);
			}

		}
		Collection<ProviderConfig> providers = config.getProviderConfigurations();
		if (providers != null) {
			for (ProviderConfig provider : providers) {
				String type = provider.getType();
				composeName(type, plugins);
			}
		}
		
		Collection<InitializerConfig> initializers = config.getInitializers();
		if (initializers != null) {
			for (InitializerConfig initializer : initializers) {
				plugins.add(initializer.getPluginGroupId()+":walkmod-"+initializer.getPluginArtifactId()+"-plugin");
			}
		}

		for (String id : plugins) {
			if (!previousPlugins.contains(id)) {
				String[] parts = id.split(":");
				PluginConfig cfg = new PluginConfigImpl();
				cfg.setGroupId(parts[0].trim());
				cfg.setArtifactId(parts[1].trim());
				cfg.setVersion("latest.integration");
				pluginCfg.add(cfg);
			}
		}
		config.setPlugins(pluginCfg);

	}

	private void composeName(String type, HashSet<String> plugins) {
		if (type != null && !type.startsWith("walkmod:commons")) {
			String[] parts = type.split(":");
			if (parts.length == 3) {
				plugins.add(parts[0] + ":" + parts[1]);
			} else if (parts.length <= 2) {
				String aux = parts[0].trim();
				if (aux.length() > 0) {
					plugins.add("org.walkmod:walkmod-" + aux + "-plugin");
				}
			}
		}
	}
}
