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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ProjectConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.JSONConfigParser;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.MergePolicyConfigImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.conf.entities.impl.ProviderConfigImpl;
import org.walkmod.conf.providers.yml.AddChainYMLAction;
import org.walkmod.conf.providers.yml.AddConfigurationParameterYMLAction;
import org.walkmod.conf.providers.yml.AddModulesYMLAction;
import org.walkmod.conf.providers.yml.AddProviderConfigYMLAction;
import org.walkmod.conf.providers.yml.AddTransformationYMLAction;
import org.walkmod.conf.providers.yml.RemoveChainsYMLAction;
import org.walkmod.conf.providers.yml.RemoveModulesYMLAction;
import org.walkmod.conf.providers.yml.RemovePluginYMLAction;
import org.walkmod.conf.providers.yml.RemoveProvidersYMLAction;
import org.walkmod.conf.providers.yml.RemoveTransformationYMLAction;
import org.walkmod.conf.providers.yml.SetReaderYMLAction;
import org.walkmod.conf.providers.yml.SetWriterYMLAction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public class YAMLConfigurationProvider extends AbstractChainConfigurationProvider implements
		ProjectConfigurationProvider {

	private Configuration configuration;

	private String fileName;

	private YAMLFactory factory;

	private ObjectMapper mapper;

	private JSONConfigParser converter = new JSONConfigParser();

	public YAMLConfigurationProvider() {
		this("walkmod.yml");
	}

	public YAMLConfigurationProvider(String fileName) {
		this.fileName = fileName;
		factory = new YAMLFactory();
		mapper = new ObjectMapper(factory);
		factory.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
	}

	@Override
	public void init(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void load() throws ConfigurationException {

		try {
			JsonNode node = mapper.readTree(new File(fileName));
			inferInitializers(configuration);
			if (node.has("plugins")) {

				Iterator<JsonNode> it = node.get("plugins").iterator();
				Collection<PluginConfig> pluginList = new LinkedList<PluginConfig>();

				while (it.hasNext()) {
					JsonNode current = it.next();

					String pluginId = current.asText();
					String[] split = pluginId.split(":");
					if (split.length > 3) {

					} else {
						String groupId, artifactId, version;

						groupId = split[0];
						artifactId = split[1];
						version = split[2];

						PluginConfig plugin = new PluginConfigImpl();
						plugin.setGroupId(groupId);
						plugin.setArtifactId(artifactId);
						plugin.setVersion(version);

						pluginList.add(plugin);
					}

				}
				configuration.setPlugins(pluginList);

			}
			if (node.has("modules")) {

				Iterator<JsonNode> it = node.get("modules").iterator();
				List<String> modules = new LinkedList<String>();
				configuration.setModules(modules);
				while (it.hasNext()) {
					JsonNode current = it.next();
					modules.add(current.asText());
				}
				configuration.setModules(modules);
			}
			if (node.has("merge-policies")) {
				Iterator<JsonNode> it = node.get("merge-policies").iterator();
				Collection<MergePolicyConfig> mergePolicies = new LinkedList<MergePolicyConfig>();
				while (it.hasNext()) {
					JsonNode next = it.next();
					if (next.has("policy")) {
						MergePolicyConfig mergeCfg = new MergePolicyConfigImpl();
						mergeCfg.setName(next.get("name").asText());
						mergeCfg.setDefaultObjectPolicy(next.get("default-object-policy").asText());
						mergeCfg.setDefaultTypePolicy(next.get("default-type-policy").asText());
						if (next.has("policy")) {
							Iterator<JsonNode> it2 = next.get("policy").iterator();
							Map<String, String> policies = new HashMap<String, String>();
							while (it2.hasNext()) {
								JsonNode nextPolicy = it2.next();
								String objectType = nextPolicy.get("object-type").asText();
								String policyType = nextPolicy.get("policy-type").asText();

								policies.put(objectType, policyType);
							}
							mergeCfg.setPolicyEntries(policies);
						}

						mergePolicies.add(mergeCfg);
					}

				}
				configuration.setMergePolicies(mergePolicies);
			}

			if (node.has("conf-providers")) {
				Iterator<JsonNode> it = node.get("conf-providers").iterator();
				Collection<ProviderConfig> provConfigs = new LinkedList<ProviderConfig>();
				while (it.hasNext()) {
					JsonNode next = it.next();

					ProviderConfig provCfg = new ProviderConfigImpl();
					provCfg.setType(next.get("type").asText());

					provCfg.setParameters(converter.getParams(next));
					provConfigs.add(provCfg);
				}
				configuration.setProviderConfigurations(provConfigs);
			}

			if (node.has("chains")) {
				Iterator<JsonNode> it = node.get("chains").iterator();
				Collection<ChainConfig> chains = new LinkedList<ChainConfig>();
				int i = 0;
				while (it.hasNext()) {
					ChainConfig chainCfg = new ChainConfigImpl();

					JsonNode current = it.next();
					if (current.has("name")) {
						chainCfg.setName(current.get("name").asText());
					} else {
						chainCfg.setName("chain_" + i);
					}

					if (current.has("reader")) {
						JsonNode reader = current.get("reader");

						chainCfg.setReaderConfig(converter.getReader(reader));
					} else {
						addDefaultReaderConfig(chainCfg);
					}
					if (current.has("writer")) {

						JsonNode writer = current.get("writer");

						chainCfg.setWriterConfig(converter.getWriter(writer));
					} else {
						addDefaultWriterConfig(chainCfg);
					}
					if (current.has("walker")) {

						chainCfg.setWalkerConfig(converter.getWalker(current));
					} else {
						addDefaultWalker(chainCfg);
						if (current.has("transformations")) {
							WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
							walkerCfg.setTransformations(converter.getTransformationCfgs(current));
						}
					}
					chains.add(chainCfg);
				}
				configuration.setChainConfigs(chains);

			} else if (node.has("transformations")) {

				Collection<ChainConfig> chains = new LinkedList<ChainConfig>();
				ChainConfig chainCfg = new ChainConfigImpl();
				chainCfg.setName("");
				addDefaultReaderConfig(chainCfg);
				addDefaultWalker(chainCfg);
				WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
				walkerCfg.setTransformations(converter.getTransformationCfgs(node));
				addDefaultWriterConfig(chainCfg);
				chains.add(chainCfg);
				configuration.setChainConfigs(chains);
			}

		} catch (JsonProcessingException e) {
			throw new ConfigurationException("Error parsing the " + fileName + " configuration", e);
		} catch (IOException e) {
			throw new ConfigurationException("Error reading the " + fileName + " configuration", e);

		}
		inferPlugins(configuration);
	}

	@Override
	public void addPluginConfig(final PluginConfig pluginConfig, boolean recursive) throws TransformerException {

		File cfg = new File(fileName);

		ArrayNode pluginList = null;
		JsonNode node = null;
		try {
			node = mapper.readTree(cfg);
		} catch (Exception e) {

		}
		if (node == null) {
			node = new ObjectNode(mapper.getNodeFactory());
		}
		if (recursive && node.has("modules")) {
			JsonNode aux = node.get("modules");
			if (aux.isArray()) {
				ArrayNode modules = (ArrayNode) aux;
				int max = modules.size();
				for (int i = 0; i < max; i++) {
					JsonNode module = modules.get(i);
					if (module.isTextual()) {
						String moduleDir = module.asText();

						try {
							File auxFile = new File(fileName).getCanonicalFile().getParentFile();
							YAMLConfigurationProvider child = new YAMLConfigurationProvider(auxFile.getAbsolutePath()
									+ File.separator + moduleDir + File.separator + "walkmod.yml");
							child.createConfig();
							child.addPluginConfig(pluginConfig, recursive);
						} catch (IOException e) {
							throw new TransformerException(e);
						}

					}
				}
			}
		} else {
			if (!node.has("plugins")) {
				pluginList = new ArrayNode(mapper.getNodeFactory());
				if (node.isObject()) {
					ObjectNode aux = (ObjectNode) node;
					aux.set("plugins", pluginList);
				} else {
					throw new TransformerException("The root element is not a JSON node");
				}
			} else {
				JsonNode aux = node.get("plugins");
				if (aux.isArray()) {
					pluginList = (ArrayNode) node.get("plugins");
				} else {
					throw new TransformerException("The plugins element is not a valid array");
				}
			}
			pluginList.add(new TextNode(pluginConfig.getGroupId() + ":" + pluginConfig.getArtifactId() + ":"
					+ pluginConfig.getVersion()));
			write(node);
		}
	}

	public void write(JsonNode node) throws TransformerException {
		if (node != null) {
			File cfg = new File(fileName);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(cfg);
				JsonGenerator generator = mapper.getFactory().createGenerator(fos);

				generator.useDefaultPrettyPrinter();
				mapper.writeTree(generator, node);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						throw new TransformerException("Error writting the configuration", e);
					}
				}
			}
		}
	}

	public ObjectMapper getObjectMapper() {
		return mapper;
	}

	@Override
	public void addChainConfig(ChainConfig chainCfg, boolean recursive) throws Exception {
		AddChainYMLAction action = new AddChainYMLAction(chainCfg, this, recursive);
		action.execute();
	}

	@Override
	public void addTransformationConfig(String chain, String path, TransformationConfig transformationCfg,
			boolean recursive) throws Exception {
		if (transformationCfg != null) {
			AddTransformationYMLAction action = new AddTransformationYMLAction(chain, path, transformationCfg, this,
					recursive);
			action.execute();
		}
		return;
	}

	@Override
	public void createConfig() throws IOException {
		File cfg = new File(fileName);
		if (!cfg.exists() && !cfg.createNewFile()) {
			throw new IOException("The system can't create the [" + fileName + "] file");
		}
	}

	public JsonNode getRootNode() {
		File cfg = new File(fileName);
		JsonNode node = null;
		try {
			node = mapper.readTree(cfg);
		} catch (Exception e) {

		}
		if (node == null) {
			node = new ObjectNode(mapper.getNodeFactory());
		}
		return node;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public void addProviderConfig(ProviderConfig providerCfg, boolean recursive) throws Exception {
		AddProviderConfigYMLAction action = new AddProviderConfigYMLAction(providerCfg, this, recursive);
		action.execute();
	}

	@Override
	public void addModules(List<String> modules) throws Exception {
		AddModulesYMLAction action = new AddModulesYMLAction(modules, this);
		action.execute();
	}

	@Override
	public void removeTransformations(String chain, List<String> transformations, boolean recursive) throws Exception {
		if (transformations != null && !transformations.isEmpty()) {
			RemoveTransformationYMLAction action = new RemoveTransformationYMLAction(chain, transformations, this,
					recursive);
			action.execute();
		}
	}

	@Override
	public void setWriter(String chain, String type, String path, boolean recursive) throws Exception {
		if ((type != null && !"".equals(type.trim())) || (path != null && !"".equals(path.trim()))) {
			SetWriterYMLAction action = new SetWriterYMLAction(chain, type, path, this, recursive);
			action.execute();
		}

	}

	@Override
	public void setReader(String chain, String type, String path, boolean recursive) throws Exception {
		if ((type != null && !"".equals(type.trim())) || (path != null && !"".equals(path.trim()))) {
			SetReaderYMLAction action = new SetReaderYMLAction(chain, type, path, this, recursive);
			action.execute();
		}

	}

	@Override
	public void removePluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception {
		RemovePluginYMLAction action = new RemovePluginYMLAction(pluginConfig, this, recursive);
		action.execute();
	}

	@Override
	public void removeModules(List<String> modules) throws Exception {
		if (modules != null) {
			RemoveModulesYMLAction action = new RemoveModulesYMLAction(modules, this);
			action.execute();
		}

	}

	@Override
	public void removeProviders(List<String> providers, boolean recursive) throws Exception {
		if (providers != null) {
			RemoveProvidersYMLAction action = new RemoveProvidersYMLAction(providers, this, recursive);
			action.execute();
		}
	}

	@Override
	public void removeChains(List<String> chains, boolean recursive) throws Exception {
		if (chains != null) {
			RemoveChainsYMLAction action = new RemoveChainsYMLAction(chains, this, recursive);
			action.execute();
		}

	}

	@Override
	public void addConfigurationParameter(String param, String value, String type, String category, String name,
			String chain, boolean recursive) throws Exception {
		if (param != null && value != null) {
			AddConfigurationParameterYMLAction action = new AddConfigurationParameterYMLAction(param, value, type,
					category, name, chain, this, recursive);
			action.execute();
		}

	}

	@Override
	public String getFileExtension() {
		return "yml";
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public File getConfigurationFile() {
		return new File(fileName);
	}

	@Override
	public ProjectConfigurationProvider clone(File cfgFile) {
		return new YAMLConfigurationProvider(cfgFile.getAbsolutePath());
	}
}
