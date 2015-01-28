package org.walkmod.conf.providers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.entities.impl.ParserConfigImpl;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;

public class LanguageConfigurationProviderTest {

	@Test
	public void testNullOverwriting() {
		LanguageConfigurationProvider provider = new LanguageConfigurationProvider();
		Configuration conf = new ConfigurationImpl();
		ChainConfig cc = new ChainConfigImpl();
		cc.setName("test-chain");
		ReaderConfig reader = new ReaderConfig();
		WalkerConfig walker = new WalkerConfigImpl();
		TransformationConfig transformation = new TransformationConfigImpl();
		transformation.isMergeable(true);
		List<TransformationConfig> transf = new LinkedList<TransformationConfig>();
		transf.add(transformation);
		walker.setParserConfig(new ParserConfigImpl());
		walker.setTransformations(transf);
		WriterConfig writer = new WriterConfigImpl();
		cc.setReaderConfig(reader);
		cc.setWalkerConfig(walker);
		cc.setWriterConfig(writer);
		conf.addChainConfig(cc);
		provider.init(conf);
		provider.load();
		Assert.assertNotNull(reader.getPath());
		Assert.assertNotNull(reader.getType());
		Assert.assertNotNull(walker.getType());
		Assert.assertNotNull(walker.getParserConfig().getType());
		Assert.assertNotNull(writer.getPath());
		Assert.assertNotNull(writer.getType());
		Assert.assertNotNull(transformation.getMergePolicy());
		Assert.assertNotNull(conf.getMergePolicies());
		Collection<MergePolicyConfig> mergec = conf.getMergePolicies();
		Assert.assertEquals(1, mergec.size());
		MergePolicyConfig mpc = mergec.iterator().next();
		Assert.assertNotNull(mpc.getDefaultObjectPolicy());
		Assert.assertNotNull(mpc.getDefaultTypePolicy());
		Map<String, String> entries = mpc.getPolicyEntries();
		Assert.assertEquals(2, entries.size());
	}
}
