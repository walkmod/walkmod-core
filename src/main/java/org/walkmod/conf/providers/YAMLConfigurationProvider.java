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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ProjectConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.JSONConfigParser;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.MergePolicyConfigImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.conf.entities.impl.ProviderConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
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
	public boolean addPluginConfig(final PluginConfig pluginConfig) throws TransformerException {

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
		return true;
	}

	private void write(JsonNode node) throws TransformerException {
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

	@Override
	public boolean addChainConfig(ChainConfig chainCfg) throws TransformerException {
		File cfg = new File(fileName);

		ArrayNode chainsList = null;
		JsonNode chainsNode = null;
		try {

			chainsNode = mapper.readTree(cfg);
		} catch (Exception e) {

		}
		if (chainsNode != null) {
			if (!chainsNode.has("chains")) {
				chainsList = new ArrayNode(mapper.getNodeFactory());
				if (chainsNode.isObject()) {
					ObjectNode aux = (ObjectNode) chainsNode;
					aux.set("chains", chainsList);
				} else {
					throw new TransformerException("The root element is not a JSON node");
				}
			} else {
				JsonNode aux = chainsNode.get("chains");
				if (aux.isArray()) {
					chainsList = (ArrayNode) chainsNode.get("chains");
				} else {
					throw new TransformerException("The plugins element is not a valid array");
				}
			}
		}
		ObjectNode chainNode = new ObjectNode(mapper.getNodeFactory());
		ReaderConfig readerCfg = chainCfg.getReaderConfig();
		if (readerCfg != null) {
			if (chainsNode == null) {
				chainsNode = new ObjectNode(mapper.getNodeFactory());
				ObjectNode aux = (ObjectNode) chainsNode;
				chainsList = new ArrayNode(mapper.getNodeFactory());
				aux.set("chains", chainsList);
			}
			ObjectNode readerNode = new ObjectNode(mapper.getNodeFactory());
			chainNode.set("reader", readerNode);
			populateWriterReader(readerNode, readerCfg.getPath(), readerCfg.getType(), readerCfg.getIncludes(),
					readerCfg.getExcludes(), readerCfg.getParameters());

		} else {
			addDefaultReaderConfig(chainCfg);
		}

		WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
		if (walkerCfg != null) {

			ObjectNode walkerNode = null;

			String type = walkerCfg.getType();
			if (type != null) {
				if (chainsNode == null) {
					chainsNode = new ObjectNode(mapper.getNodeFactory());
					ObjectNode aux = (ObjectNode) chainsNode;
					chainsList = new ArrayNode(mapper.getNodeFactory());
					aux.set("chains", chainsList);
				}
				walkerNode = new ObjectNode(mapper.getNodeFactory());
				chainNode.set("walker", walkerNode);
				walkerNode.set("type", new TextNode(type));
			}

			Map<String, Object> wparams = walkerCfg.getParams();
			if (wparams != null && !wparams.isEmpty()) {
				if (walkerNode == null) {
					if (chainsNode == null) {
						chainsNode = new ObjectNode(mapper.getNodeFactory());
						ObjectNode aux = (ObjectNode) chainsNode;
						chainsList = new ArrayNode(mapper.getNodeFactory());
						aux.set("chains", chainsList);
					}
					walkerNode = new ObjectNode(mapper.getNodeFactory());
					chainNode.set("walker", walkerNode);
				}
				populateParams(walkerNode, wparams);
			}

			String rootNamespace = walkerCfg.getRootNamespace();
			if (rootNamespace != null) {
				if (walkerNode == null) {
					if (chainsNode == null) {
						chainsNode = new ObjectNode(mapper.getNodeFactory());
						ObjectNode aux = (ObjectNode) chainsNode;
						chainsList = new ArrayNode(mapper.getNodeFactory());
						aux.set("chains", chainsList);
					}
					walkerNode = new ObjectNode(mapper.getNodeFactory());
					chainNode.set("walker", walkerNode);
				}
				walkerNode.set("root-namespace", new TextNode(rootNamespace));
			}

			List<TransformationConfig> transformationList = walkerCfg.getTransformations();
			if (transformationList != null && !transformationList.isEmpty()) {
				ArrayNode transformationListNode = new ArrayNode(mapper.getNodeFactory());
				if (walkerNode == null) {
					if (chainsNode == null) {
						ObjectNode aux = new ObjectNode(mapper.getNodeFactory());
						aux.set("transformations", transformationListNode);
						chainsNode = aux;
					} else {
						chainNode.set("transformations", transformationListNode);
					}
				} else {
					walkerNode.set("transformations", transformationListNode);
				}
				for (TransformationConfig transCfg : transformationList) {
					ObjectNode transformationNode = new ObjectNode(mapper.getNodeFactory());
					transformationListNode.add(transformationNode);
					createTransformation(transformationNode, transCfg);
				}

			}

		}

		WriterConfig writerCfg = chainCfg.getWriterConfig();
		if (writerCfg != null) {
			if (chainsNode == null) {
				chainsNode = new ObjectNode(mapper.getNodeFactory());
				ObjectNode aux = (ObjectNode) chainsNode;
				chainsList = new ArrayNode(mapper.getNodeFactory());
				aux.set("chains", chainsList);
			}
			ObjectNode writerNode = new ObjectNode(mapper.getNodeFactory());
			chainNode.set("writer", writerNode);
			populateWriterReader(writerNode, writerCfg.getPath(), writerCfg.getType(), writerCfg.getIncludes(),
					writerCfg.getExcludes(), writerCfg.getParams());

		} else {
			addDefaultWriterConfig(chainCfg);
		}
		if (chainsList != null) {
			chainsList.add(chainNode);
		}
		write(chainsNode);
		return true;
	}

	private void createTransformation(ObjectNode transformationNode, TransformationConfig transCfg) {

		String name = transCfg.getName();
		if (name != null) {
			transformationNode.set("name", new TextNode(name));
		}
		String typeName = transCfg.getType();
		if (typeName != null) {
			transformationNode.set("type", new TextNode(typeName));
		}
		String mergePolicy = transCfg.getMergePolicy();
		if (mergePolicy != null) {
			transformationNode.set("merge-policy", new TextNode(mergePolicy));
		}
		if (transCfg.isMergeable()) {
			transformationNode.set("isMergeable", BooleanNode.TRUE);
		}
		Map<String, Object> params = transCfg.getParameters();
		if (params != null && !params.isEmpty()) {
			populateParams(transformationNode, params);
		}
	}

	private void populateParams(ObjectNode root, Map<String, Object> params) {
		ObjectNode paramsNode = new ObjectNode(mapper.getNodeFactory());
		root.set("params", paramsNode);

		Set<String> keys = params.keySet();
		for (String key : keys) {
			paramsNode.set(key, new TextNode(params.get(key).toString()));
		}
	}

	private void populateWriterReader(ObjectNode root, String path, String type, String[] includes, String[] excludes,
			Map<String, Object> params) {
		if (path != null && !"".equals(path)) {
			root.set("path", new TextNode(path));
		}

		if (type != null) {
			root.set("type", new TextNode(type));
		}

		if (includes != null && includes.length > 0) {
			ArrayNode includesNode = new ArrayNode(mapper.getNodeFactory());
			for (int i = 0; i < includes.length; i++) {
				includesNode.add(new TextNode(includes[i]));
			}
			root.set("includes", includesNode);
		}

		if (excludes != null && excludes.length > 0) {
			ArrayNode excludesNode = new ArrayNode(mapper.getNodeFactory());
			for (int i = 0; i < excludes.length; i++) {
				excludesNode.add(new TextNode(excludes[i]));
			}
			root.set("excludes", excludesNode);
		}
		if (params != null && !params.isEmpty()) {
			populateParams(root, params);
		}

	}

	@Override
	public boolean addTransformationConfig(String chain, TransformationConfig transformationCfg)
			throws TransformerException {
		if (transformationCfg != null) {
			File cfg = new File(fileName);
			ArrayNode transformationsNode = null;
			JsonNode chainsNode = null;
			try {
				chainsNode = mapper.readTree(cfg);
			} catch (Exception e) {

			}
			if (chainsNode == null) {
				chainsNode = new ObjectNode(mapper.getNodeFactory());

			}
			boolean validChainName = chain != null && !"".equals(chain) && !"default".equals(chain);
			if (!chainsNode.has("chains")) {
				if (chainsNode.has("transformations")) {
					JsonNode aux = chainsNode.get("transformations");
					if (aux.isArray()) {
						transformationsNode = (ArrayNode) aux;
					}
					
					if (!validChainName) {
						ObjectNode auxRoot = (ObjectNode) chainsNode;
						if(transformationsNode == null){
							transformationsNode = new ArrayNode(mapper.getNodeFactory());
						}
						auxRoot.set("transformations", transformationsNode);
					} else {
						//reset the root
						chainsNode = new ObjectNode(mapper.getNodeFactory());
						ObjectNode auxRoot = (ObjectNode) chainsNode;
						
						//the default chain list added
						ObjectNode chainObject = new ObjectNode(mapper.getNodeFactory());
						chainObject.set("name", new TextNode("default"));
						chainObject.set("transformations", transformationsNode);
						ArrayNode chainsListNode = new ArrayNode(mapper.getNodeFactory());
						chainsListNode.add(chainObject);
						
						//the requested chain added
						ObjectNode newChain = new ObjectNode(mapper.getNodeFactory());
						newChain.set("name", new TextNode(chain));
						transformationsNode = new ArrayNode(mapper.getNodeFactory());
						newChain.set("transformations", transformationsNode);
						chainsListNode.add(newChain);
						
						auxRoot.set("chains", chainsListNode);
						
					}
				}
				else{
					ObjectNode auxRoot = (ObjectNode) chainsNode;
					transformationsNode = new ArrayNode(mapper.getNodeFactory());
					if (validChainName){
						ArrayNode auxChainsList = new ArrayNode(mapper.getNodeFactory());
						ObjectNode aux = new ObjectNode(mapper.getNodeFactory());
						auxChainsList.add(aux);
						auxRoot.set("chains", auxChainsList);
						
						aux.set("name", new TextNode(chain));
						auxRoot = aux;
					}
					auxRoot.set("transformations", transformationsNode);
				}
				
			} else {
				if (validChainName) {
					JsonNode aux = chainsNode.get("chains");
					boolean found = false;
					if (aux.isArray()) {
						Iterator<JsonNode> it = aux.elements();
						while (it.hasNext()) {
							JsonNode next = it.next();
							if (next.has("name")) {
								String id = next.get("name").asText();
								if (chain.equals(id)) {
									found = true;
									if (next.has("transformations")) {
										JsonNode auxTrans = next.get("transformations");
										if (auxTrans.isArray()) {
											transformationsNode = (ArrayNode) auxTrans;
										} else {
											throw new TransformerException("The chain [" + chain
													+ "] does not have a valid transformations node");
										}
									} else if (next.isObject()) {
										ObjectNode auxNext = (ObjectNode) next;
										transformationsNode = new ArrayNode(mapper.getNodeFactory());
										auxNext.set("transformations", transformationsNode);
									} else {
										throw new TransformerException("The chain [" + chain
												+ "] does not have a valid structure");
									}
								}
							}

						}
						if (!found) {
							ChainConfig chainCfg = new ChainConfigImpl();
							chainCfg.setName(chain);
							WalkerConfig walkerCfg = new WalkerConfigImpl();
							List<TransformationConfig> transfs = new LinkedList<TransformationConfig>();
							transfs.add(transformationCfg);
							walkerCfg.setTransformations(transfs);
							chainCfg.setWalkerConfig(walkerCfg);
							addChainConfig(chainCfg);
							return true;
						}
					}
				} else {
					throw new TransformerException(
							"The user must specify a chain name (new or existing) where to add the transformation: ["
									+ transformationCfg.getType() + "]");

				}
			}
			if (transformationsNode != null) {
				ObjectNode transformationNode = new ObjectNode(mapper.getNodeFactory());
				transformationsNode.add(transformationNode);

				createTransformation(transformationNode, transformationCfg);
				write(chainsNode);
				return true;
			} else if (chain != null) {
				throw new TransformerException("The chain [" + chain + "] does not exists");
			}
		}
		return false;
	}

	@Override
	public void createConfig() throws IOException {
		File cfg = new File(fileName);
		if (!cfg.exists() && !cfg.createNewFile()) {
			throw new IOException("The system can't create the [" + fileName + "] file");
		}
	}

	@Override
	public boolean addProviderConfig(ProviderConfig providerCfg) throws TransformerException {
		File cfg = new File(fileName);
		JsonNode node = null;
		try {
			node = mapper.readTree(cfg);
		} catch (Exception e) {

		}
		if (node == null) {
			node = new ObjectNode(mapper.getNodeFactory());
		}

		if (node.has("conf-providers")) {
			JsonNode list = node.get("conf-providers");
			Iterator<JsonNode> it = list.iterator();
			boolean found = false;
			while (it.hasNext() && !found) {
				JsonNode next = it.next();
				found = providerCfg.getType().equals(next.get("type").asText());
			}
			if (!found) {
				if (list.isArray()) {
					ArrayNode aux = (ArrayNode) list;
					ObjectNode prov = new ObjectNode(mapper.getNodeFactory());
					prov.set("type", new TextNode(providerCfg.getType()));
					Map<String, Object> params = providerCfg.getParameters();
					if (params != null && !params.isEmpty()) {
						populateParams(prov, params);
					}
					aux.add(prov);
					write(node);
					return true;
				}
			}
		} else {
			ArrayNode aux = new ArrayNode(mapper.getNodeFactory());
			ObjectNode prov = new ObjectNode(mapper.getNodeFactory());
			prov.set("type", new TextNode(providerCfg.getType()));
			Map<String, Object> params = providerCfg.getParameters();
			if (params != null && !params.isEmpty()) {
				populateParams(prov, params);
			}
			aux.add(prov);
			ObjectNode auxNode = (ObjectNode) node;
			auxNode.set("conf-providers", aux);
			write(node);
			return true;
		}

		return false;
	}

	@Override
	public void addModules(List<String> modules) throws TransformerException {
		File cfg = new File(fileName);
		JsonNode node = null;
		try {
			node = mapper.readTree(cfg);
		} catch (Exception e) {

		}
		if (node == null) {
			node = new ObjectNode(mapper.getNodeFactory());
		}
		ArrayNode aux = null;
		HashSet<String> modulesToAdd = new HashSet<String>(modules);
		if (node.has("modules")) {
			JsonNode list = node.get("modules");
			Iterator<JsonNode> it = list.iterator();

			while (it.hasNext()) {
				JsonNode next = it.next();
				modulesToAdd.remove(next.asText().trim());

			}
			if (!modulesToAdd.isEmpty()) {
				if (list.isArray()) {
					aux = (ArrayNode) list;
				}
			}
		} else {
			aux = new ArrayNode(mapper.getNodeFactory());
		}
		if (!modulesToAdd.isEmpty()) {
			for (String moduleToAdd : modulesToAdd) {
				TextNode prov = new TextNode(moduleToAdd);
				aux.add(prov);
			}
			ObjectNode auxNode = (ObjectNode) node;
			auxNode.set("modules", aux);
			write(node);
		}

	}

	@Override
	public void removeTransformations(String chain, List<String> transformations) throws TransformerException {
		if (transformations != null && !transformations.isEmpty()) {
			File cfg = new File(fileName);
			JsonNode node = null;
			try {
				node = mapper.readTree(cfg);
			} catch (Exception e) {

			}
			if (node == null) {
				node = new ObjectNode(mapper.getNodeFactory());
			}
			HashSet<String> transList = new HashSet<String>(transformations);
			JsonNode transfListNode = null;
			if (chain == null || "".equals(chain)) {
				if (node.has("transformations")) {
					transfListNode = node.get("transformations");

				}
			} else {
				if (node.has("chains")) {
					JsonNode chainsListNode = node.get("chains");
					if (chainsListNode.isArray()) {
						Iterator<JsonNode> it = chainsListNode.iterator();
						boolean found = false;
						while (it.hasNext() && !found) {
							JsonNode current = it.next();
							if (current.has("name")) {
								String name = current.get("name").asText();
								found = name.equals(chain);

								if (current.has("transformations")) {
									transfListNode = current.get("transformations");
								}
							}
						}
					}
				}
			}

			if (transfListNode != null) {
				if (transfListNode.isArray()) {
					ArrayNode transArray = (ArrayNode) transfListNode;
					Iterator<JsonNode> it = transArray.iterator();
					List<Integer> removeIndex = new LinkedList<Integer>();
					int i = 0;
					while (it.hasNext()) {
						JsonNode transfNode = it.next();
						if (transfNode.has("type")) {
							String type = transfNode.get("type").asText();
							if (transList.contains(type)) {
								removeIndex.add(i);
							}
						}
						i++;
					}
					for (Integer pos : removeIndex) {
						transArray.remove(pos);
					}
				}
				write(node);
			}
		}
	}

	@Override
	public void setWriter(String chain, String type) throws TransformerException {
		if (type != null && !"".equals(type.trim())) {
			File cfg = new File(fileName);
			JsonNode node = null;
			try {
				node = mapper.readTree(cfg);
			} catch (Exception e) {

			}
			if (node == null) {
				node = new ObjectNode(mapper.getNodeFactory());
			}
			ObjectNode writer = null;
			if (chain != null && !"".equals(chain.trim())) {

				if (node.has("chains")) {
					JsonNode chainsListNode = node.get("chains");
					if (chainsListNode.isArray()) {
						Iterator<JsonNode> it = chainsListNode.iterator();
						boolean found = false;
						while (it.hasNext() && !found) {
							JsonNode current = it.next();
							if (current.has("name")) {
								String name = current.get("name").asText();
								found = name.equals(chain);
								if (found) {
									if (current.has("writer")) {
										writer = (ObjectNode) current.get("writer");
									} else {
										writer = new ObjectNode(mapper.getNodeFactory());
									}
									writer.set("type", new TextNode(type));
								}
							}
						}
					}
				}
			} else {
				if (node.has("writer")) {
					writer = (ObjectNode) node.get("writer");
				} else {
					writer = new ObjectNode(mapper.getNodeFactory());
					ObjectNode aux = (ObjectNode) node;
					aux.set("writer", writer);
				}
				writer.set("type", new TextNode(type));
			}

			if (writer != null) {
				write(node);
			}
		}

	}
}
