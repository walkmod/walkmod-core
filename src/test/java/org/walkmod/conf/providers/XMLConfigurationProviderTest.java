package org.walkmod.conf.providers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.commands.AddCfgProviderCommand;
import org.walkmod.commands.AddPluginCommand;
import org.walkmod.commands.AddTransformationCommand;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
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
      XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/multimodule/walkmod.xml", false);
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
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);
         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         String content = FileUtils.readFileToString(xml);

         Assert.assertTrue(content.contains("imports-cleaner"));

      } finally {
         xml.delete();
      }

   }

   @Test
   public void testAddTransformationWithBefore() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);
         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         command = new AddTransformationCommand("setter-getter", "common", false, null, null, null, null, false, null,
               "default");

         prov.addTransformationConfig("common", null, command.buildTransformationCfg(), false, null, "default");

         String content = FileUtils.readFileToString(xml);

         Assert.assertTrue(content.contains("imports-cleaner"));
         Assert.assertTrue(content.contains("common"));
         Assert.assertTrue(content.contains("setter-getter"));
         Assert.assertTrue(content.indexOf("common") < content.indexOf("default"));

      } finally {
         xml.delete();
      }

   }

   @Test
   public void testAddTransformationInDefaultChain() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);
         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         command = new AddTransformationCommand("setter-getter", "common", false, null, null, null, null, false, null,
               "default");

         prov.addTransformationConfig("common", null, command.buildTransformationCfg(), false, null, "default");

         command = new AddTransformationCommand("setter-getter", null, false, null, null, null, null, false, null,
               null);

         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         String content = FileUtils.readFileToString(xml);
         System.out.println(content);
         Assert.assertTrue(content.contains("imports-cleaner"));
         Assert.assertTrue(content.contains("common"));
         Assert.assertTrue(content.contains("setter-getter"));
         Assert.assertTrue(content.indexOf("common") < content.indexOf("default"));

      } finally {
         xml.delete();
      }

   }

   @Test
   public void testAddTransformationAfterMultipleInDefaultChain() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);
         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         command = new AddTransformationCommand("setter-getter", null, false, null, null, null, null, false, null,
               null);

         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         command = new AddTransformationCommand("setter-getter", "common", false, null, null, null, null, false, null,
               "default");

         prov.addTransformationConfig("common", null, command.buildTransformationCfg(), false, null, "default");

         String content = FileUtils.readFileToString(xml);
         System.out.println(content);
         Assert.assertTrue(content.contains("imports-cleaner"));
         Assert.assertTrue(content.contains("common"));
         Assert.assertTrue(content.contains("setter-getter"));
         Assert.assertTrue(content.indexOf("common") < content.indexOf("default"));

      } finally {
         xml.delete();
      }

   }

   @Test
   public void testAddTransformationRecursively() throws Exception {
      File aux = new File("src/test/resources/modulesxml");
      File aux1 = new File(aux, "module1");
      File aux2 = new File(aux, "module2");

      aux.mkdirs();
      aux1.mkdir();
      aux2.mkdir();

      File xml = new File(aux, "walkmod.xml");
      File xml1 = new File(aux1, "walkmod.xml");
      File xml2 = new File(aux2, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         List<String> modules = new LinkedList<String>();

         modules.add("module1");
         modules.add("module2");

         prov.addModules(modules);

         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, true);

         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), true, null, null);

         String content = FileUtils.readFileToString(xml);

         Assert.assertTrue(!content.contains("imports-cleaner"));

         content = FileUtils.readFileToString(xml1);
         Assert.assertTrue(content.contains("imports-cleaner"));

         content = FileUtils.readFileToString(xml2);
         Assert.assertTrue(content.contains("imports-cleaner"));

      } finally {
         FileUtils.deleteDirectory(aux);

      }

   }

   @Test
   public void testAddTransformationWithChainAndPath() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
               "src", null, null, false);
         prov.addTransformationConfig("mychain", "src", command.buildTransformationCfg(), false, null, null);

         String content = FileUtils.readFileToString(xml);

         Assert.assertTrue(content.contains("imports-cleaner") && content.contains("src"));

      } finally {
         xml.delete();
      }

   }

   @Test
   public void testAddTransformationWithPath() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, "src",
               null, null, false);
         prov.addTransformationConfig(null, "src", command.buildTransformationCfg(), false, null, null);

         String content = FileUtils.readFileToString(xml);

         Assert.assertTrue(content.contains("imports-cleaner") && content.contains("src"));

      } finally {
         xml.delete();
      }

   }

   @Test
   public void testAddMultipleTransformationWithPath() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, "src",
               null, null, false);
         prov.addTransformationConfig(null, "src", command.buildTransformationCfg(), false, null, null);

         String content = FileUtils.readFileToString(xml);

         System.out.println(content);

         command = new AddTransformationCommand("license-header", null, false, null, "src", null, null, false);
         prov.addTransformationConfig(null, "src", command.buildTransformationCfg(), false, null, null);

         content = FileUtils.readFileToString(xml);

         Assert.assertTrue(
               content.contains("imports-cleaner") && content.contains("license-header") && content.contains("src"));

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
         prov.addProviderConfig(provCfg, false);

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
   public void testConfigProvidersConfigRecursively() throws Exception {
      AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

      File aux = new File("src/test/resources/xmlmultimodule");
      aux.mkdirs();
      File module0 = new File(aux, "module0");
      module0.mkdir();

      File xml = new File(aux, "walkmod.xml");
      File cfg = new File(module0, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         ProviderConfig provCfg = command.build();
         prov.addModules(Arrays.asList("module0"));
         prov.addProviderConfig(provCfg, true);

         String output = FileUtils.readFileToString(cfg);

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

   @Test
   public void testRemoveTranformation() throws Exception {
      List<String> list = new LinkedList<String>();
      list.add("imports-cleaner");

      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);

         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         prov.removeTransformations(null, list, false);

         String output = FileUtils.readFileToString(xml);

         Assert.assertTrue(!output.contains("imports-cleaner"));
      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testRemoveChainTransformations() throws Exception {
      List<String> list = new LinkedList<String>();
      list.add("imports-cleaner");

      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         AddTransformationCommand command0 = new AddTransformationCommand("license-applier", "mychain", false, null,
               null, null, null, false);

         prov.addTransformationConfig("mychain", null, command0.buildTransformationCfg(), false, null, null);

         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
               null, null, null, false);

         prov.addTransformationConfig("mychain", null, command.buildTransformationCfg(), false, null, null);

         prov.removeTransformations("mychain", list, false);

         String output = FileUtils.readFileToString(xml);

         Assert.assertTrue(!output.contains("imports-cleaner"));

         Assert.assertTrue(output.contains("license-applier"));

         list.add("license-applier");

         prov.removeTransformations("mychain", list, false);

         output = FileUtils.readFileToString(xml);

         Assert.assertTrue(!output.contains("chain"));
      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testRemoveTransformationsRecursively() throws Exception {
      List<String> list = new LinkedList<String>();
      list.add("imports-cleaner");

      File aux = new File("src/test/resources/xmlmultimodule");
      aux.mkdirs();
      File module0 = new File(aux, "module0");
      File module1 = new File(aux, "module1");
      File cfg0 = new File(module0, "walkmod.xml");
      File cfg1 = new File(module1, "walkmod.xml");

      module0.mkdir();
      module1.mkdir();

      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         prov.addModules(Arrays.asList("module0", "module1"));

         AddTransformationCommand command0 = new AddTransformationCommand("license-applier", "mychain", false, null,
               null, null, null, true);

         prov.addTransformationConfig("mychain", null, command0.buildTransformationCfg(), true, null, null);

         String output = FileUtils.readFileToString(cfg0);

         System.out.println(output);

         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
               null, null, null, true);

         prov.addTransformationConfig("mychain", null, command.buildTransformationCfg(), true, null, null);

         output = FileUtils.readFileToString(cfg0);

         System.out.println(output);

         prov.removeTransformations("mychain", list, true);

         output = FileUtils.readFileToString(cfg0);

         Assert.assertTrue(!output.contains("imports-cleaner"));

         Assert.assertTrue(output.contains("license-applier"));

         output = FileUtils.readFileToString(cfg1);

         Assert.assertTrue(!output.contains("imports-cleaner"));

         Assert.assertTrue(output.contains("license-applier"));

      } finally {
         if (aux.exists()) {
            FileUtils.deleteDirectory(aux);
         }
      }
   }

   @Test
   public void testSetWriter() throws Exception {

      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);

         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         prov.setWriter(null, "javalang:string-writer", null, false, null);

         String output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(output.contains("javalang:string-writer"));

      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testSetReader() throws Exception {

      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
               null, null, false);

         prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false, null, null);

         prov.setReader(null, "walkmod:commons:file-reader", null, false, null);

         String output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(output.contains("walkmod:commons:file-reader"));

      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testRemovePlugin() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         List<String> plugins = new LinkedList<String>();
         plugins.add("org.walkmod:imports-cleaner");

         AddPluginCommand command = new AddPluginCommand(plugins);

         List<PluginConfig> pluginCfgs = command.build();

         prov.addPluginConfig(pluginCfgs.get(0), false);

         String output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(output.contains("imports-cleaner"));

         prov.removePluginConfig(pluginCfgs.get(0), false);

         output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(!output.contains("imports-cleaner"));

      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testAddPluginRecursively() throws Exception {
      File aux = new File("src/test/resources/multmodulexml");
      aux.mkdirs();

      File module0 = new File(aux, "module0");
      module0.mkdir();

      File xml = new File(aux, "walkmod.xml");
      File modulexml = new File(module0, "walkmod.xml");

      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         prov.addModules(Arrays.asList("module0"));

         List<String> plugins = new LinkedList<String>();
         plugins.add("org.walkmod:imports-cleaner");

         AddPluginCommand command = new AddPluginCommand(plugins);

         List<PluginConfig> pluginCfgs = command.build();

         prov.addPluginConfig(pluginCfgs.get(0), true);

         String output = FileUtils.readFileToString(modulexml);

         System.out.println(output);

         Assert.assertTrue(output.contains("imports-cleaner"));

      } finally {
         if (aux.exists()) {
            FileUtils.deleteQuietly(aux);
         }
      }
   }

   @Test
   public void testRemoveModule() throws Exception {
      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

      try {
         Configuration conf = new ConfigurationImpl();
         prov.init(conf);

         prov.createConfig();

         List<String> modules = new LinkedList<String>();
         modules.add("module1");

         prov.addModules(modules);

         String output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(output.contains("module1"));

         prov.removeModules(modules);

         output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(!output.contains("module1"));

      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testRemoveProviders() throws Exception {
      AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         ProviderConfig provCfg = command.build();
         prov.addProviderConfig(provCfg, false);

         String output = FileUtils.readFileToString(xml);

         System.out.println(output);

         Assert.assertTrue(output.contains("maven"));
         List<String> providers = new LinkedList<String>();

         providers.add("maven");
         prov.removeProviders(providers, false);

         output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(!output.contains("maven"));
      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testRemoveChains() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);

      File aux = new File("src/test/resources/xml");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);

         String output = FileUtils.readFileToString(xml);

         Assert.assertTrue(output.contains("mychain"));

         List<String> chains = new LinkedList<String>();

         chains.add("mychain");
         prov.removeChains(chains, false);

         output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(!output.contains("mychain"));
      } finally {
         if (xml.exists()) {
            xml.delete();
         }
      }
   }

   @Test
   public void testAddConfigurationParameter() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);

      File aux = new File("src/test/resources/xmlparams");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);

         prov.addConfigurationParameter("testParam", "hello", "imports-cleaner", null, null, null, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("testParam") && output.contains("hello"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddConfigurationParameterWithoutChain() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null, null,
            null, false);

      File aux = new File("src/test/resources/xmlparams");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig(null, null, transfCfg, false, null, null);

         prov.addConfigurationParameter("testParam", "hello", "imports-cleaner", null, null, null, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("testParam") && output.contains("hello"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddConfigurationParameterToWriter() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);

      File aux = new File("src/test/resources/xmlparams");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addConfigurationParameter("testParam", "hello", "eclipse-writer", null, null, null, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("testParam") && output.contains("hello"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddConfigurationParameterToWriterWithoutChain() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null, null,
            null, false);

      File aux = new File("src/test/resources/xmlparams");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig(null, null, transfCfg, false, null, null);
         prov.setWriter(null, "eclipse-writer", null, false, null);
         prov.addConfigurationParameter("testParam", "hello", "eclipse-writer", null, null, null, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("testParam") && output.contains("hello"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddConfigurationParameterWithChainFilter() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);

      File aux = new File("src/test/resources/xmlparams");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addConfigurationParameter("testParam", "hello", "eclipse-writer", null, null, "mychain", false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("testParam") && output.contains("hello"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddIncludes() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlincludes");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addIncludesToChain("mychain", Arrays.asList("foo"), false, true, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("wildcard") && output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddIncludesToWriter() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlincludes");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addIncludesToChain("mychain", Arrays.asList("foo"), false, false, true);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("wildcard") && output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddIncludesToReaderAndWriter() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlincludes");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addIncludesToChain("mychain", Arrays.asList("foo"), false, true, true);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("wildcard") && output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddExcludes() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlincludes");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addExcludesToChain("mychain", Arrays.asList("foo"), false, true, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("wildcard") && output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddIncludesExcludeInDefault() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null, null,
            null, false);
      File aux = new File("src/test/resources/xmlincludes2");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig(null, null, transfCfg, false, null, null);

         prov.addIncludesToChain(null, Arrays.asList("foo"), false, true, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("wildcard") && output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddIncludesExcludeInDefault2() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlincludes2");
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);

         command = new AddTransformationCommand("imports-cleaner", null, false, null, null, null, null, false);
         transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig(null, null, transfCfg, false, null, null);

         prov.addIncludesToChain(null, Arrays.asList("foo"), false, true, false);

         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("wildcard") && output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testRemoveIncludes() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlincludes");
      if (aux.exists()) {
         FileUtils.deleteDirectory(aux);
      }
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();

         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig("mychain", null, transfCfg, false, null, null);
         prov.setWriter("mychain", "eclipse-writer", null, false, null);
         prov.addIncludesToChain("mychain", Arrays.asList("foo"), false, true, false);
         prov.removeIncludesFromChain("mychain", Arrays.asList("foo"), false, true, false);
         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(!output.contains("wildcard") && !output.contains("foo"));
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }

   @Test
   public void testAddParamToTheImplicitWriter() throws Exception {
      AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
            null, null, false);
      File aux = new File("src/test/resources/xmlwriter");
      if (aux.exists()) {
         FileUtils.deleteDirectory(aux);
      }
      aux.mkdirs();
      File xml = new File(aux, "walkmod.xml");
      XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
      try {
         prov.createConfig();
         
         TransformationConfig transfCfg = command.buildTransformationCfg();
         prov.addTransformationConfig(null, null, transfCfg, false, null, null);
         
         prov.addConfigurationParameter("formatter", "formatter.xml", "eclipse-writer", "writer", null, null, false);
         
         String output = FileUtils.readFileToString(xml);
         System.out.println(output);

         Assert.assertTrue(output.contains("formatter"));
         
      } finally {
         FileUtils.deleteDirectory(aux);
      }
   }
}
