package org.walkmod.conf.providers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.commands.AddCfgProviderCommand;
import org.walkmod.commands.AddTransformationCommand;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;

public class XMLConfigurationProviderTest {

	@Test
	public void testVersion1_0() throws Exception {
		XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/testFiles/walkmod.xml", false);
		Configuration conf = new ConfigurationImpl();
		prov.init(conf);
		prov.load();
		Assert.assertEquals(1, conf.getChainConfigs().size());
		Assert.assertEquals("main-chain", conf.getChainConfigs().iterator().next().getName());
	}

	@Test
	public void testVersion1_1() throws Exception {
		XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/multimodule/walkmod.xml",
				false);
		Configuration conf = new ConfigurationImpl();
		prov.init(conf);
		prov.load();
		Assert.assertEquals(0, conf.getChainConfigs().size());
		Assert.assertEquals(2, conf.getModules().size());
	}

	@Test
	public void testAddTransformation() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null);
			prov.addTransformationConfig(null, command.buildTransformationCfg());

			String content = FileUtils.readFileToString(xml);

			Assert.assertTrue(content.contains("imports-cleaner"));

		} finally {
			xml.delete();
		}

	}

	@Test
	public void testConfigProvidersConfig() throws Exception {
		AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();

			ProviderConfig provCfg = command.build();
			prov.addProviderConfig(provCfg);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("maven"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}

	}
	
	@Test
	public void testAddModulesConfig() throws Exception {
		
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			List<String> modules = new LinkedList<String>();
			modules.add("module1");
			prov.addModules(modules);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("module1"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}

	}
}
