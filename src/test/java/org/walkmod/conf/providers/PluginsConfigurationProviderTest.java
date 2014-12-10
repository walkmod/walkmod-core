package org.walkmod.conf.providers;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;

public class PluginsConfigurationProviderTest {

	@Test
	public void testNullOverwriting() {
		PluginsConfigurationProvider provider = new PluginsConfigurationProvider();
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		provider.load();
		Collection<PluginConfig> plugins = conf.getPlugins();
		Assert.assertNotNull(plugins);
		Assert.assertEquals(2, plugins.size());
	}

	@Test
	public void testInvalidPlugins() {

		IvyConfigurationProvider provider = new IvyConfigurationProvider();
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		PluginConfig pc = new PluginConfigImpl();
		pc.setGroupId("foo");
		pc.setArtifactId("bar");
		pc.setVersion("10");
		conf.setPlugins(new LinkedList());
		conf.getPlugins().add(pc);
		Exception exception = null;
		try {
			provider.load();
		} catch (ConfigurationException e) {
			exception = e;
			
		}
		
		Assert.assertNotNull(exception);

	}

}
