package org.walkmod.conf.providers;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;

public class PluginsConfigurationProviderTest {

	@Test
	public void testNullOverwriting(){
		PluginsConfigurationProvider provider = new PluginsConfigurationProvider();
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		provider.load();
		Collection<PluginConfig> plugins = conf.getPlugins();
		Assert.assertNotNull(plugins);
		Assert.assertEquals(2, plugins.size());
	}
	
}
