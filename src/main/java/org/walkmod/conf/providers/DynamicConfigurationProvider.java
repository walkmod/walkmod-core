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

import org.apache.commons.lang.StringUtils;
import org.walkmod.Options;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;

public class DynamicConfigurationProvider implements ConfigurationProvider {

    private Configuration config;
    private String[] chains;
    private Options options;

    public DynamicConfigurationProvider(Options options, String... chains) {
        this.options = options;
        this.chains = chains;
    }

    @Override
    public void init(Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public void load() throws ConfigurationException {
        config.prepareInitializers();
        if (chains != null) {
            for (String chain : chains) {
                if (StringUtils.isNotEmpty(chain)) {
                    PluginConfig plugin = config.resolvePlugin(chain);
                    config.getPlugins().add(plugin);
                }
            }
            config.preparePlugins();
        }
        String path = options.getPath();

        if (chains != null) {
            for (String chain : chains) {
                if (StringUtils.isNotEmpty(chain)) {
                    ChainConfig cc = config.getChainConfig(chain);
                    if (cc == null) {
                        cc = new ChainConfigImpl(new TransformationConfigImpl(chain));
                        config.addChainConfig(cc);
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(path)) {
            for (ChainConfig cc : config.getChainConfigs()) {
                cc.setPath(path);
            }
        }
    }

}
