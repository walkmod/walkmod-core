package org.walkmod.conf.providers;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;

public class YAMLConfigurationProviderTest {

	@Test
	public void testCompleteYaml() {
		YAMLConfigurationProvider provider = new YAMLConfigurationProvider("src/test/resources/yaml/walkmod.yml");
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		provider.load();
		Assert.assertEquals(2, conf.getPlugins().size());

		Iterator<PluginConfig> itPluginCfg = conf.getPlugins().iterator();

		PluginConfig firstPlugin = itPluginCfg.next();

		Assert.assertEquals("org.walkmod", firstPlugin.getGroupId());

		Assert.assertEquals("walkmod-maven-plugin", firstPlugin.getArtifactId());

		Assert.assertEquals("[1.0, 2.0)", firstPlugin.getVersion());

		Assert.assertEquals(1, conf.getProviderConfigurations().size());

		Iterator<ProviderConfig> itProvCfg = conf.getProviderConfigurations().iterator();

		Assert.assertEquals("walkmod:commons:maven", itProvCfg.next().getType());

		Assert.assertEquals(1, conf.getChainConfigs().size());

		ChainConfig cc = conf.getChainConfigs().iterator().next();
		Assert.assertEquals("main-chain", cc.getName());

		Assert.assertNotNull(cc.getReaderConfig());

		Assert.assertNull(cc.getReaderConfig().getPath());

		Assert.assertEquals(2, cc.getWalkerConfig().getTransformations().size());

		Assert.assertNotNull(cc.getWriterConfig());

		Assert.assertNull(cc.getWriterConfig().getPath());

		Iterator<TransformationConfig> it = cc.getWalkerConfig().getTransformations().iterator();
		TransformationConfig transCfg = it.next();

		Assert.assertEquals("walkmod:commons:method-refactor", transCfg.getType());

		Assert.assertNotNull(transCfg.getParameters());

		Assert.assertEquals("src/conf/refactoring-methods.json", transCfg.getParameters().get("refactoringConfigFile"));
	}

	@Test
	public void testTransformationsYaml() {
		YAMLConfigurationProvider provider = new YAMLConfigurationProvider("src/test/resources/yaml/basic.yml");
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		provider.load();

		Assert.assertNotNull(conf.getChainConfigs());

		ChainConfig cc = conf.getChainConfigs().iterator().next();

		Assert.assertNotNull(cc.getWalkerConfig().getTransformations());

		Iterator<TransformationConfig> it = cc.getWalkerConfig().getTransformations().iterator();

		TransformationConfig tc = it.next();

		Assert.assertEquals("walkmod:commons:method-refactor", tc.getType());
		Assert.assertEquals("src/conf/refactoring-methods.json", tc.getParameters().get("refactoringConfigFile"));
	}

	@Test
	public void testChainsYaml() {
		YAMLConfigurationProvider provider = new YAMLConfigurationProvider("src/test/resources/yaml/chains.yml");
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		provider.load();

		Assert.assertNotNull(conf.getChainConfigs());
		ChainConfig cc = conf.getChainConfigs().iterator().next();

		Iterator<TransformationConfig> it = cc.getWalkerConfig().getTransformations().iterator();
		TransformationConfig first = it.next();

		Assert.assertEquals("walkmod:commons:method-refactor", first.getType());

		Assert.assertNotNull(cc.getReaderConfig());
		Assert.assertNotNull(cc.getWriterConfig());
	}

	@Test
	public void testAddPlugin() throws Exception {

		File file = new File("src/test/resources/yaml/addplugin.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			PluginConfig pluginCfg = new PluginConfigImpl();
			pluginCfg.setGroupId("org.walkmod");
			pluginCfg.setArtifactId("myplugin");
			pluginCfg.setVersion("1.0");

			provider.addPluginConfig(pluginCfg);
			
			String output = FileUtils.readFileToString(file);
			
			String desiredOutput = "plugins:\n";
			desiredOutput+="- \"org.walkmod:myplugin:1.0\"";
			
			Assert.assertEquals(desiredOutput, output);
				
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddChain() throws Exception {
		File file = new File("src/test/resources/yaml/addchain.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			ChainConfig chainCfg = new ChainConfigImpl();
			chainCfg.setName("mychain");
			WalkerConfig walkerCfg = new WalkerConfigImpl();
			chainCfg.setWalkerConfig(walkerCfg);

			List<TransformationConfig> transformations = new LinkedList<TransformationConfig>();

			TransformationConfig transCfg = new TransformationConfigImpl();
			transformations.add(transCfg);

			transCfg.setType("walkmod:commons:method-refactor");

			Map<String, Object> params = new HashMap<String, Object>();
			transCfg.setParameters(params);
			params.put("refactoringConfigFile", "src/conf/refactoring-methods.json");

			walkerCfg.setTransformations(transformations);

			provider.addChainConfig(chainCfg);
			String output = FileUtils.readFileToString(file);

			String desiredOutput = "transformations:\n";
			desiredOutput += "- type: \"walkmod:commons:method-refactor\"\n";
			desiredOutput += "  params:\n";
			desiredOutput += "    refactoringConfigFile: \"src/conf/refactoring-methods.json\"";

			Assert.assertEquals(desiredOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}

	}
}
