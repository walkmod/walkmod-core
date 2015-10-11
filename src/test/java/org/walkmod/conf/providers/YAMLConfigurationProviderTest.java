package org.walkmod.conf.providers;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.commands.AddChainCommand;
import org.walkmod.commands.AddTransformationCommand;
import org.walkmod.commands.JSONConverter;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;

import com.fasterxml.jackson.databind.JsonNode;

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
			desiredOutput += "- \"org.walkmod:myplugin:1.0\"";

			Assert.assertEquals(desiredOutput, output);

		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testEmptyChain() throws Exception {
		File file = new File("src/test/resources/yaml/addEmptychain.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		ChainConfig chainCfg = new ChainConfigImpl();

		try {
			provider.addChainConfig(chainCfg);

			String output = FileUtils.readFileToString(file);

			Assert.assertEquals("", output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testChainWithDifferentDefaultPath() throws Exception {

		AddChainCommand command = new AddChainCommand(null, "src", null, null, null);
		ChainConfig chainCfg = command.buildChainCfg();

		File file = new File("src/test/resources/yaml/addEmptychain.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		try {
			provider.addChainConfig(chainCfg);

			String output = FileUtils.readFileToString(file);

			String validOutput = "chains:\n";
			validOutput += "- reader:\n";
			validOutput += "    path: \"src\"\n";
			validOutput += "  writer:\n";
			validOutput += "    path: \"src\"";
			Assert.assertEquals(validOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testChainWithName() throws Exception {

		AddChainCommand command = new AddChainCommand("hello", "src/main/java", null, null, null);
		ChainConfig chainCfg = command.buildChainCfg();

		File file = new File("src/test/resources/yaml/addEmptychain.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
		Configuration conf = new ConfigurationImpl();
		provider.init(conf);
		try {
			provider.addChainConfig(chainCfg);

			String output = FileUtils.readFileToString(file);

			Assert.assertEquals("", output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	@Test
	public void testAddChainWithReader() throws Exception{
		JSONConverter converter = new JSONConverter();
		JsonNode reader = converter
				.convert("{type: \"custom-reader\"}");
		AddChainCommand command = new AddChainCommand("mychain", "src/main/java", reader, null, null);

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

			ChainConfig chainCfg = command.buildChainCfg();

			provider.addChainConfig(chainCfg);
			String output = FileUtils.readFileToString(file);

			String desiredOutput = "chains:\n";
			desiredOutput += "- reader:\n";
			desiredOutput += "    type: \"custom-reader\"";

			Assert.assertEquals(desiredOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddChainTransformation() throws Exception {

		JSONConverter converter = new JSONConverter();
		JsonNode walker = converter
				.convert("{transformations: [ { type: \"walkmod:commons:method-refactor\", params: { refactoringConfigFile: \"src/conf/refactoring-methods.json\"} }]}");
		AddChainCommand command = new AddChainCommand("mychain", "src/main/java", null, null, walker);

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

			ChainConfig chainCfg = command.buildChainCfg();

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
	
	@Test
	public void testAddTransformation() throws Exception{
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null);
		File file = new File("src/test/resources/yaml/addtransformation.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		
		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transCfg = command.buildTransformationCfg();

			provider.addTransformationConfig(null, transCfg);
			String output = FileUtils.readFileToString(file);

			String desiredOutput = "transformations:\n";
			desiredOutput += "- type: \"imports-cleaner\"";
			
			Assert.assertEquals(desiredOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
