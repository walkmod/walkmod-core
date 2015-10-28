package org.walkmod.conf.providers;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.commands.AddCfgProviderCommand;
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

			provider.addPluginConfig(pluginCfg, false);

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
	public void testAddPluginRecursively() throws Exception {

		File dir = new File("src/test/resources/multimoduleyaml");
		dir.mkdirs();
		
		File module0 = new File(dir, "module0");
		module0.mkdir();
		
		File moduleCfg = new File(module0, "walkmod.yml");
		
		File file = new File(dir, "walkmod.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);
			
			provider.addModules(Arrays.asList("module0"));

			PluginConfig pluginCfg = new PluginConfigImpl();
			pluginCfg.setGroupId("org.walkmod");
			pluginCfg.setArtifactId("myplugin");
			pluginCfg.setVersion("1.0");

			provider.addPluginConfig(pluginCfg, true);

			String output = FileUtils.readFileToString(moduleCfg);

			String desiredOutput = "plugins:\n";
			desiredOutput += "- \"org.walkmod:myplugin:1.0\"";

			Assert.assertEquals(desiredOutput, output);

		} finally {
			if (dir.exists()) {
				FileUtils.deleteDirectory(dir);
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
	public void testAddChainTransformation() throws Exception {

		JSONConverter converter = new JSONConverter();
		JsonNode walker = converter.convert("{ refactoringConfigFile: \"src/conf/refactoring-methods.json\"}");
		AddTransformationCommand command = new AddTransformationCommand("walkmod:commons:method-refactor", null, false,
				null, null, walker, false);

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

			TransformationConfig transformationCfg = command.buildTransformationCfg();

			provider.addTransformationConfig(null, null, transformationCfg, false);
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
	public void testAddChainTransformationRecursively() throws Exception {

		JSONConverter converter = new JSONConverter();
		JsonNode walker = converter.convert("{ refactoringConfigFile: \"src/conf/refactoring-methods.json\"}");
		AddTransformationCommand command = new AddTransformationCommand("walkmod:commons:method-refactor", null, false,
				null, null, walker, false);

		File dir = new File("src/test/resources/multimoduleyaml");

		File module1 = new File(dir, "module1");
		File module2 = new File(dir, "module2");

		dir.mkdirs();

		module1.mkdir();
		module2.mkdir();

		File file = new File(dir, "walkmod.yml");

		File file2 = new File(module1, "walkmod.yml");

		File file3 = new File(module2, "walkmod.yml");

		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");
		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			List<String> modules = new LinkedList<String>();
			modules.add("module1");
			modules.add("module2");

			provider.addModules(modules);

			TransformationConfig transformationCfg = command.buildTransformationCfg();

			provider.addTransformationConfig(null, null, transformationCfg, true);

			String output = FileUtils.readFileToString(file);
			Assert.assertTrue(!output.contains("method-refactor"));

			output = FileUtils.readFileToString(file2);
			Assert.assertTrue(output.contains("method-refactor"));

			output = FileUtils.readFileToString(file3);
			Assert.assertTrue(output.contains("method-refactor"));

		} finally {
			FileUtils.deleteDirectory(dir);
		}
	}

	@Test
	public void testAddChainTransformationToChainAfterTranfList() throws Exception {

		JSONConverter converter = new JSONConverter();
		JsonNode walker = converter.convert("{ refactoringConfigFile: \"src/conf/refactoring-methods.json\"}");
		AddTransformationCommand command = new AddTransformationCommand("walkmod:commons:method-refactor", null, false,
				null, null, walker, false);

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

			TransformationConfig transformationCfg = command.buildTransformationCfg();

			provider.addTransformationConfig(null, null, transformationCfg, false);
			command = new AddTransformationCommand("walkmod:commons:class-refactor", "mychain", false, null, null,
					walker, false);
			transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig("mychain", null, transformationCfg, false);

			String output = FileUtils.readFileToString(file);

			Assert.assertTrue(output.contains("mychain") && output.contains("default"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddChainTransformationWithNewChain() throws Exception {

		JSONConverter converter = new JSONConverter();
		JsonNode walker = converter.convert("{ refactoringConfigFile: \"src/conf/refactoring-methods.json\"}");
		AddTransformationCommand command = new AddTransformationCommand("walkmod:commons:method-refactor", "mychain",
				false, null, null, walker, false);

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

			TransformationConfig transformationCfg = command.buildTransformationCfg();

			provider.addTransformationConfig("mychain", null, transformationCfg, false);

			String output = FileUtils.readFileToString(file);

			Assert.assertTrue(output.contains("mychain"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddChainTransformationWithExistingChain() throws Exception {

		JSONConverter converter = new JSONConverter();
		JsonNode walker = converter.convert("{ refactoringConfigFile: \"src/conf/refactoring-methods.json\"}");
		AddTransformationCommand command = new AddTransformationCommand("walkmod:commons:method-refactor", "mychain",
				false, null, null, walker, false);

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

			TransformationConfig transformationCfg = command.buildTransformationCfg();

			provider.addTransformationConfig("mychain", null, transformationCfg, false);
			command = new AddTransformationCommand("walkmod:commons:class-refactor", "mychain", false, null, null,
					walker, false);
			transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig("mychain", null, transformationCfg, false);

			String output = FileUtils.readFileToString(file);

			Assert.assertTrue(output.contains("walkmod:commons:class-refactor") && !output.contains("default"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddTransformationToPath() throws Exception {
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
				"src", null, false);
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

			provider.addTransformationConfig("mychain", "src", transCfg, false);
			String output = FileUtils.readFileToString(file);

			Assert.assertTrue(output.contains("src"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddTransformationToPath2() throws Exception {
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, "src",
				null, false);
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

			provider.addTransformationConfig(null, "src", transCfg, false);
			String output = FileUtils.readFileToString(file);

			Assert.assertTrue(output.contains("src"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddTransformation() throws Exception {
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
				null, false);
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

			provider.addTransformationConfig(null, null, transCfg, false);
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

	@Test
	public void testConfigProvidersConfig() throws Exception {
		AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

		File file = new File("src/test/resources/yaml/addcfgproviders.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			ProviderConfig provCfg = command.build();
			provider.addProviderConfig(provCfg);

			String output = FileUtils.readFileToString(file);

			String desiredOutput = "conf-providers:\n";
			desiredOutput += "- type: \"maven\"";

			Assert.assertEquals(desiredOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testAddModulesToConfig() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("module1");

		File file = new File("src/test/resources/yaml/addmodules.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			provider.addModules(list);

			String output = FileUtils.readFileToString(file);

			String desiredOutput = "modules:\n";
			desiredOutput += "- \"module1\"";

			Assert.assertEquals(desiredOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testRemoveTranformation() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("imports-cleaner");

		File file = new File("src/test/resources/yaml/rmTransf.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		String input = "transformations:\n";
		input += "- type: \"imports-cleaner\"";

		FileUtils.write(file, input);

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			provider.removeTransformations(null, list, false);

			String output = FileUtils.readFileToString(file);

			String desiredOutput = "transformations: []";

			Assert.assertEquals(desiredOutput, output);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	@Test
	public void testRemoveTranformationRecursively() throws Exception {
		
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
				null, false);
		
		
		List<String> list = new LinkedList<String>();
		list.add("imports-cleaner");

		File dir = new File("src/test/resources/yamlmultimodule");
		dir.mkdirs();
		
		File module0 = new File(dir, "module0");
		module0.mkdir();
		
		File module1 = new File(dir, "module1");
		module1.mkdir();
		
		File file = new File(dir, "walkmod.yml");
		if (file.exists()) {
			file.delete();
		}
		
		File cfg0 = new File(module0, "walkmod.yml");

		
		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			provider.createConfig();
			provider.addModules(Arrays.asList("module0", "module1"));
			
			provider.addTransformationConfig(null, null, command.buildTransformationCfg(), true);
			
			String output = FileUtils.readFileToString(cfg0);
			
			Assert.assertTrue(output.contains("imports-cleaner"));
			
			provider.removeTransformations(null, list, true);

			output = FileUtils.readFileToString(cfg0);

			Assert.assertTrue(!output.contains("imports-cleaner"));
		} finally {
			if (file.exists()) {
				FileUtils.deleteDirectory(dir);
			}
		}
	}

	@Test
	public void testSetWriter() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("javalang:string-writer");

		File file = new File("src/test/resources/yaml/setWriter.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		String input = "transformations:\n";
		input += "- type: \"imports-cleaner\"";

		FileUtils.write(file, input);

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			provider.setWriter(null, list.get(0), null);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("javalang:string-writer"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testRemovePlugin() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("org.walkmod:javalang");
		File file = new File("src/test/resources/yaml/removePlugin.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		String input = "plugins:\n";
		input += "- \"org.walkmod:imports-cleaner:2.0\"";
		FileUtils.write(file, input);

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);
			PluginConfig pc = new PluginConfigImpl();
			pc.setGroupId("org.walkmod");
			pc.setArtifactId("imports-cleaner");

			provider.removePluginConfig(pc);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(!output.contains("imports-cleaner"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testRemoveModule() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("module1");
		File file = new File("src/test/resources/yaml/removeModule.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		String input = "modules:\n";
		input += "- \"module1\"";
		FileUtils.write(file, input);

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			provider.removeModules(list);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(!output.contains("module1"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testRemoveProvider() throws Exception {
		AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

		File file = new File("src/test/resources/yaml/addcfgproviders.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			ProviderConfig provCfg = command.build();
			provider.addProviderConfig(provCfg);
			List<String> providers = new LinkedList<String>();
			providers.add("maven");
			provider.removeProviders(providers);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(!output.contains("maven"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testRemoveChains() throws Exception {
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
				null, null, false);

		File file = new File("src/test/resources/yaml/rmchains.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig("mychain", null, transformationCfg, false);
			List<String> chains = new LinkedList<String>();
			chains.add("mychain");
			provider.removeChains(chains);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(!output.contains("mychain"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	
	@Test
	public void testAddConfigurationParameter() throws Exception{
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
				null, null, false);

		File file = new File("src/test/resources/yaml/rmchains.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig("mychain", null, transformationCfg, false);
			provider.addConfigurationParameter("testParam", "hello", "imports-cleaner", null, null, null);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("params") && output.contains("testParam") && output.contains("hello"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	@Test
	public void testAddConfigurationParameterWithChain() throws Exception{
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
				null, null, false);

		File file = new File("src/test/resources/yaml/rmchains.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig("mychain", null, transformationCfg, false);
			provider.addConfigurationParameter("testParam", "hello", "imports-cleaner", null, null, "mychain");

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("params") && output.contains("testParam") && output.contains("hello"));
			
			provider.addConfigurationParameter("testParam", "bye", "imports-cleaner", null, null, "mychain2");
			output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertFalse(output.contains("params") && output.contains("testParam") && output.contains("bye"));
			
			
			provider.addConfigurationParameter("testParam", "bye", "imports-cleaner", null, null, "mychain");
			output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("params") && output.contains("testParam") && output.contains("bye") && !output.contains("hello"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	@Test
	public void testAddConfigurationParameter2() throws Exception{
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null,
				null, null, false);

		File file = new File("src/test/resources/yaml/rmchains.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig(null, null, transformationCfg, false);
			provider.addConfigurationParameter("testParam", "hello", "imports-cleaner", null, null, null);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("params") && output.contains("testParam") && output.contains("hello"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	

	@Test
	public void testAddConfigurationParameterWithCategory() throws Exception{
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null,
				null, null, false);

		File file = new File("src/test/resources/yaml/rmchains.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig(null, null, transformationCfg, false);
			provider.addConfigurationParameter("testParam", "hello", "imports-cleaner", "transformation", null, null);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("params") && output.contains("testParam") && output.contains("hello"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	@Test
	public void testAddConfigurationParameterToWriter() throws Exception{
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null,
				null, null, false);

		File file = new File("src/test/resources/yaml/rmchains.yml");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileUtils.write(file, "");

		try {
			YAMLConfigurationProvider provider = new YAMLConfigurationProvider(file.getPath());
			Configuration conf = new ConfigurationImpl();
			provider.init(conf);

			TransformationConfig transformationCfg = command.buildTransformationCfg();
			provider.addTransformationConfig(null, null, transformationCfg, false);
			provider.setWriter(null, "eclipse-writer", null);
			
			provider.addConfigurationParameter("testParam", "hello", "eclipse-writer", null, null, null);

			String output = FileUtils.readFileToString(file);
			System.out.println(output);
			Assert.assertTrue(output.contains("params") && output.contains("testParam") && output.contains("hello"));
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
