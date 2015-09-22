package org.walkmod.conf.providers;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.impl.ConfigurationImpl;

public class XMLConfigurationProviderTest {

	@Test
	public void testVersion1_0() throws Exception{
		XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/testFiles/walkmod.xml", false);
		Configuration conf = new ConfigurationImpl();
		prov.init(conf);
		prov.load();
		Assert.assertEquals(1, conf.getChainConfigs().size());
		Assert.assertEquals("main-chain", conf.getChainConfigs().iterator().next().getName());
	}
	
	
	@Test
	public void testVersion1_1() throws Exception{
		XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/multimodule/walkmod.xml", false);
		Configuration conf = new ConfigurationImpl();
		prov.init(conf);
		prov.load();
		Assert.assertEquals(0, conf.getChainConfigs().size());
		Assert.assertEquals(2, conf.getModules().size());
	}
}
