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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ProjectConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.ParserConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.MergePolicyConfigImpl;
import org.walkmod.conf.entities.impl.ParserConfigImpl;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.conf.entities.impl.ProviderConfigImpl;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;
import org.walkmod.conf.providers.xml.AddChainXMLAction;
import org.walkmod.conf.providers.xml.AddConfigurationParameterXMLAction;
import org.walkmod.conf.providers.xml.AddIncludesOrExcludesXMLAction;
import org.walkmod.conf.providers.xml.AddModulesXMLAction;
import org.walkmod.conf.providers.xml.AddPluginXMLAction;
import org.walkmod.conf.providers.xml.AddProviderConfigXMLAction;
import org.walkmod.conf.providers.xml.AddTransformationXMLAction;
import org.walkmod.conf.providers.xml.RemoveChainsXMLAction;
import org.walkmod.conf.providers.xml.RemoveIncludesOrExcludesXMLAction;
import org.walkmod.conf.providers.xml.RemoveModulesXMLAction;
import org.walkmod.conf.providers.xml.RemovePluginXMLAction;
import org.walkmod.conf.providers.xml.RemoveProvidersXMLAction;
import org.walkmod.conf.providers.xml.RemoveTransformationXMLAction;
import org.walkmod.conf.providers.xml.SetReaderXMLAction;
import org.walkmod.conf.providers.xml.SetWriterXMLAction;
import org.walkmod.util.DomHelper;
import org.xml.sax.InputSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class XMLConfigurationProvider extends AbstractChainConfigurationProvider
		implements ProjectConfigurationProvider {

	/**
	 * Configuration file.
	 */
	private String configFileName;

	/**
	 * Error if configuration file is not found.
	 */
	private boolean errorIfMissing;

	/**
	 * Loaded configuration
	 */
	private Configuration configuration;

	/**
	 * Set of supported versions of dtdMappings
	 */
	private Map<String, String> dtdMappings;

	/**
	 * XML structure
	 */
	private Document document;

	private static final Log LOG = LogFactory.getLog(XMLConfigurationProvider.class);

	public XMLConfigurationProvider() {
		this("walkmod.xml", true);
	}

	public XMLConfigurationProvider(String configFileName, boolean errorIfMissing) {
		this.configFileName = configFileName;
		this.errorIfMissing = errorIfMissing;
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("-//WALKMOD//DTD//1.0", "walkmod-1.0.dtd");
		mappings.put("-//WALKMOD//DTD//1.1", "walkmod-1.1.dtd");
		setDtdMappings(mappings);
	}

	public void setDtdMappings(Map<String, String> mappings) {
		this.dtdMappings = Collections.unmodifiableMap(mappings);
	}

	public Map<String, String> getDtdMappings() {
		return dtdMappings;
	}

	public void init(Configuration configuration) {
		this.configuration = configuration;
		this.document = loadDocument(configFileName);
	}

	public void init() {
		init(null);
	}

	@Override
	public void addChainConfig(ChainConfig chainCfg, boolean recursive, String before) throws Exception {
		AddChainXMLAction action = new AddChainXMLAction(chainCfg, this, recursive, before);
		action.execute();
	}

	@Override
	public void addPluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception {

		AddPluginXMLAction action = new AddPluginXMLAction(pluginConfig, this, recursive);
		action.execute();
	}

	public void persist() throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//WALKMOD//DTD");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.walkmod.com/dtd/walkmod-1.1.dtd");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(document);

		StreamResult result = new StreamResult(new File(configFileName));
		transformer.transform(source, result);
	}

	/**
	 * Load the XML configuration on memory as a DOM structure with SAX.
	 * Additional information about elements location is added. Non valid DTDs
	 * or XML structures are detected.
	 * 
	 * @param file
	 *            XML configuration
	 * @return XML tree
	 */
	private Document loadDocument(String file) {
		Document doc = null;
		URL url = null;
		File f = new File(file);
		if (f.exists()) {
			try {
				url = f.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ConfigurationException("Unable to load " + file, e);
			}
		}
		if (url == null) {
			url = ClassLoader.getSystemResource(file);
		}
		InputStream is = null;
		if (url == null) {
			if (errorIfMissing) {
				throw new ConfigurationException("Could not open files of the name " + file);
			} else {
				LOG.info("Unable to locate configuration files of the name " + file + ", skipping");
				return doc;
			}
		}
		try {
			is = url.openStream();
			InputSource in = new InputSource(is);
			in.setSystemId(url.toString());
			doc = DomHelper.parse(in, dtdMappings);
		} catch (Exception e) {
			throw new ConfigurationException("Unable to load " + file, e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.error("Unable to close input stream", e);
			}
		}
		if (doc != null) {
			LOG.debug("Wallmod configuration parsed");
		}
		return doc;
	}

	/**
	 * This method will find all the parameters under this
	 * <code>paramsElement</code> and return them as Map<String, String>. For
	 * example,
	 * 
	 * <pre>
	 *   <result ... >
	 *      <param name="param1">value1</param>
	 *      <param name="param2">value2</param>
	 *      <param name="param3">value3</param>
	 *   </result>
	 * </pre>
	 * 
	 * will returns a Map<String, String> with the following key, value pairs :-
	 * <ul>
	 * <li>param1 - value1</li>
	 * <li>param2 - value2</li>
	 * <li>param3 - value3</li>
	 * </ul>
	 * 
	 * @param paramsElement
	 * @return
	 */
	private Map<String, Object> getParams(Element paramsElement) {
		LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
		if (paramsElement == null) {
			return params;
		}
		NodeList childNodes = paramsElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if ((childNode.getNodeType() == Node.ELEMENT_NODE) && "param".equals(childNode.getNodeName())) {
				Element paramElement = (Element) childNode;
				String paramName = paramElement.getAttribute("name");
				String val = getContent(paramElement);
				if (val.length() > 0) {
					char startChar = val.charAt(0);
					char endChar = val.charAt(val.length() - 1);
					if (startChar == '{' && endChar == '}') {
						try {
							JSONObject o = JSON.parseObject(val);
							params.put(paramName, o);
						} catch (JSONException e) {
							params.put(paramName, val);
						}
					} else if (startChar == '[' && endChar == ']') {
						try {
							JSONArray array = JSON.parseArray(val);
							params.put(paramName, array);
						} catch (JSONException e) {
							params.put(paramName, val);
						}
					} else {
						params.put(paramName, val);
					}
				}
			}
		}
		return params;
	}

	/**
	 * This method will return the content of this particular
	 * <code>element</code>. For example,
	 * <p/>
	 * 
	 * <pre>
	 *    <result>something</result>
	 * </pre>
	 * 
	 * When the {@link org.w3c.dom.Element} <code>&lt;result&gt;</code> is
	 * passed in as argument (<code>element</code> to this method, it returns
	 * the content of it, namely, <code>something</code> in the example above.
	 * 
	 * @return
	 */
	private String getContent(Element element) {
		StringBuilder paramValue = new StringBuilder();
		NodeList childNodes = element.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node currentNode = childNodes.item(j);
			if (currentNode != null && currentNode.getNodeType() == Node.TEXT_NODE) {
				String val = currentNode.getNodeValue();
				if (val != null) {
					paramValue.append(val.trim());
				}
			}
		}
		return paramValue.toString().trim();
	}

	public void loadChains() throws ConfigurationException {
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				Element child = (Element) childNode;
				final String nodeName = child.getNodeName();
				if ("chain".equals(nodeName)) {
					ChainConfig ac = new ChainConfigImpl();
					if ("".equals(child.getAttribute("name"))) {
						if (i == 0) {
							ac.setName("chain_" + (i + 1));
						} else {
							ac.setName("chain_" + (i + 1));
						}
					} else {
						ac.setName(child.getAttribute("name"));
					}
					NodeList childrenModel = child.getChildNodes();
					ac.setParameters(getParams(child));
					int index = 0;
					if ("reader".equals(childrenModel.item(index).getNodeName())) {
						loadReaderConfig((Element) childrenModel.item(index), ac);
						index++;
					} else {
						addDefaultReaderConfig(ac);
					}
					if (index >= childrenModel.getLength()) {
						throw new ConfigurationException(
								"Invalid architecture definition for the " + "element" + ac.getName());
					}
					if ("walker".equals(childrenModel.item(index).getNodeName())) {
						loadWalkerConfig((Element) childrenModel.item(index), ac);
						index++;
					} else if ("transformation".equals(childrenModel.item(index).getNodeName())) {
						addDefaultWalker(ac, child);
					} else {
						throw new ConfigurationException(
								"Invalid transformation chain. A walker or at least one transformation must be specified");
					}
					if (index > childrenModel.getLength()) {
						throw new ConfigurationException(
								"Invalid architecture definition for the " + "element" + ac.getName());
					}
					boolean found = false;
					while (index < childrenModel.getLength() && !found) {
						if ("writer".equals(childrenModel.item(index).getNodeName())) {
							found = true;
							loadWriter((Element) childrenModel.item(index), ac);
						}
						index++;
					}
					if (!found) {
						addDefaultWriterConfig(ac);
					}
					configuration.addChainConfig(ac);
				} else if ("transformation".equals(nodeName)) {

					ChainConfig ac = new ChainConfigImpl();
					ac.setName("default");
					List<TransformationConfig> transformationConfigs = getTransformationItems(rootElement, true);
					WalkerConfig wc = new WalkerConfigImpl();
					wc.setType(null);
					wc.setParserConfig(new ParserConfigImpl());
					wc.setTransformations(transformationConfigs);
					addDefaultReaderConfig(ac);
					ac.setWalkerConfig(wc);
					addDefaultWriterConfig(ac);
					configuration.addChainConfig(ac);
					i = i + transformationConfigs.size() - 1;
				}
			}
		}
		LOG.debug("Transformation chains loaded");
	}

	public void loadReaderConfig(Element element, ChainConfig ac) throws ConfigurationException {
		ReaderConfig readerConfig = new ReaderConfig();
		if ("reader".equals(element.getNodeName())) {
			if ("".equals(element.getAttribute("path"))) {
				throw new ConfigurationException("Invalid reader definition: " + "A path attribute must be specified");
			}
			readerConfig.setPath(element.getAttribute("path"));
			if ("".equals(element.getAttribute("type"))) {
				readerConfig.setType(null);
			} else {
				readerConfig.setType(element.getAttribute("type"));
			}
			readerConfig.setParameters(getParams(element));
			NodeList childs = element.getChildNodes();
			if (childs != null) {
				int max = childs.getLength();
				List<String> excludes = new LinkedList<String>();
				List<String> includes = new LinkedList<String>();
				for (int i = 0; i < max; i++) {
					Node n = childs.item(i);
					String nodeName = n.getNodeName();
					if ("exclude".equals(nodeName)) {
						Element exclude = (Element) n;
						excludes.add(exclude.getAttribute("wildcard"));
					} else if ("include".equals(nodeName)) {
						Element include = (Element) n;
						includes.add(include.getAttribute("wildcard"));
					} else {
						throw new ConfigurationException(
								"Invalid reader definition. Only exclude or include tags are supported");
					}
				}
				if (!excludes.isEmpty()) {
					String[] excludesArray = new String[excludes.size()];
					int j = 0;
					for (String exclude : excludes) {
						excludesArray[j] = exclude;
						j++;
					}
					readerConfig.setExcludes(excludesArray);
				}
				if (!includes.isEmpty()) {
					String[] includesArray = new String[includes.size()];
					int j = 0;
					for (String include : includes) {
						includesArray[j] = include;
						j++;
					}
					readerConfig.setIncludes(includesArray);
				}
			}
		} else {
			throw new ConfigurationException("Invalid architecture definition. "
					+ "A reader element must be defined in the architecture element " + ac.getName());
		}
		ac.setReaderConfig(readerConfig);
	}

	public void addDefaultWalker(ChainConfig ac, Element parentWalkerNode) {

		super.addDefaultWalker(ac);
		ac.getWalkerConfig().setTransformations(getTransformationItems(parentWalkerNode, false));

	}

	public void loadWalkerConfig(Element element, ChainConfig ac) {
		NodeList children;
		Node walkerNode = element;
		if ("walker".equals(walkerNode.getNodeName())) {
			WalkerConfig wc = new WalkerConfigImpl();
			String type = ((Element) walkerNode).getAttribute("type");
			if ("".equals(type)) {
				wc.setType(null);
			} else {
				wc.setType(type);
			}
			wc.setParams(getParams((Element) walkerNode));
			wc.setRootNamespace(((Element) walkerNode).getAttribute("root-namespace"));
			children = walkerNode.getChildNodes();
			if (children.getLength() > 3) {
				throw new ConfigurationException("Invalid walker definition in the " + "architecture" + ac.getName()
						+ ". Please, verify the dtd");
			}
			int max = children.getLength();
			int transformationIndex = wc.getParams().size();
			if (transformationIndex < max) {
				final String nodeName = children.item(transformationIndex).getNodeName();
				if (("parser").equals(nodeName)) {
					loadParserConfig((Element) children.item(transformationIndex), wc);
					transformationIndex = 1;
				} else {
					wc.setParserConfig(new ParserConfigImpl());
				}
			}
			else{
				wc.setParserConfig(new ParserConfigImpl());
			}

			if (transformationIndex < max) {
				loadTransformationConfigs((Element) children.item(transformationIndex), wc);
			}
			ac.setWalkerConfig(wc);
		} else {
			throw new ConfigurationException("Invalid architecture definition. "
					+ "A walker element must be defined in the architecture element " + ac.getName());
		}
	}

	public void loadParserConfig(Element element, WalkerConfig wc) {
		final String nodeName = element.getNodeName();
		if ("parser".equals(nodeName)) {
			ParserConfig pc = new ParserConfigImpl();
			if ("".equals(element.getAttribute("type"))) {
				pc.setType(null);
			} else {
				pc.setType(element.getAttribute("type"));
			}
			pc.setParameters(getParams(element));
		}
	}

	public List<TransformationConfig> getTransformationItems(Element element, boolean exceptionsEnabled) {
		List<TransformationConfig> transformationConfigs = new LinkedList<TransformationConfig>();
		NodeList transfNodes = element.getChildNodes();
		for (int j = 0; j < transfNodes.getLength(); j++) {
			element = (Element) transfNodes.item(j);
			if ("transformation".equals(element.getNodeName())) {
				TransformationConfig tc = new TransformationConfigImpl();
				String name = element.getAttribute("name");
				String visitor = element.getAttribute("type");
				String isMergeable = element.getAttribute("isMergeable");
				String mergePolicy = element.getAttribute("merge-policy");
				if ("".equals(visitor)) {
					throw new ConfigurationException(
							"Invalid transformation definition: A " + "type attribute must be specified");
				}
				if ("".equals(name)) {
					name = null;
				}
				tc.setName(name);
				tc.setType(visitor);
				tc.setParameters(getParams(element));
				if (isMergeable != null && !("".equals(isMergeable))) {
					tc.isMergeable(Boolean.parseBoolean(isMergeable));
				}
				if (!"".equals(mergePolicy.trim())) {
					tc.isMergeable(true);
					tc.setMergePolicy(mergePolicy);
				}
				transformationConfigs.add(tc);
			}
		}
		return transformationConfigs;
	}

	public void loadTransformationConfigs(Element element, WalkerConfig wc) {
		List<TransformationConfig> transformationConfigs = new LinkedList<TransformationConfig>();
		final String nodeName = element.getNodeName();
		if ("transformations".equals(nodeName)) {
			transformationConfigs = getTransformationItems(element, true);
		} else {
			throw new ConfigurationException(
					"Invalid walker definition. " + "A walker element must contain a \"transformations\" element ");
		}
		wc.setTransformations(transformationConfigs);
	}

	public void loadWriter(Element child, ChainConfig ac) {
		if ("writer".equals(child.getNodeName())) {
			WriterConfig wc = new WriterConfigImpl();
			String path = child.getAttribute("path");
			if ("".equals(path)) {
				throw new ConfigurationException("Invalid writer definition: " + "A path attribute must be specified");
			}
			wc.setPath(path);
			String type = child.getAttribute("type");
			if ("".equals(type)) {
				wc.setType(null);
			} else {
				wc.setType(type);
			}
			NodeList childs = child.getChildNodes();
			if (childs != null) {
				int max = childs.getLength();
				List<String> excludes = new LinkedList<String>();
				List<String> includes = new LinkedList<String>();
				for (int i = 0; i < max; i++) {
					Node n = childs.item(i);
					String nodeName = n.getNodeName();
					if ("exclude".equals(nodeName)) {
						Element exclude = (Element) n;
						excludes.add(exclude.getAttribute("wildcard"));
					} else if ("include".equals(nodeName)) {
						Element include = (Element) n;
						includes.add(include.getAttribute("wildcard"));
					} else if (!"param".equals(nodeName)) {
						throw new ConfigurationException(
								"Invalid writer definition. Only exclude or include tags are supported");
					}
				}
				if (!excludes.isEmpty()) {
					String[] excludesArray = new String[excludes.size()];
					int j = 0;
					for (String exclude : excludes) {
						excludesArray[j] = exclude;
						j++;
					}
					wc.setExcludes(excludesArray);
				}
				if (!includes.isEmpty()) {
					String[] includesArray = new String[includes.size()];
					int j = 0;
					for (String include : includes) {
						includesArray[j] = include;
						j++;
					}
					wc.setIncludes(includesArray);
				}
			}
			wc.setParams(getParams(child));
			ac.setWriterConfig(wc);
		}
	}

	@Override
	public void load() throws ConfigurationException {
		Map<String, Object> params = getParams(document.getDocumentElement());
		configuration.setParameters(params);
		inferInitializers(configuration);
		loadModules();
		loadPlugins();
		loadProviders();
		loadMergePolicies();
		loadChains();
		inferPlugins(configuration);
	}

	private void loadModules() {
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		boolean found = false;
		for (int i = 0; i < childSize && !found; i++) {
			Node childNode = children.item(i);
			if ("modules".equals(childNode.getNodeName())) {
				found = true;
				Element child = (Element) childNode;
				List<String> modules = new LinkedList<String>();
				configuration.setModules(modules);
				NodeList modulesNodes = child.getChildNodes();
				int modulesSize = modulesNodes.getLength();
				for (int j = 0; j < modulesSize; j++) {
					Node moduleNode = modulesNodes.item(j);
					if ("module".equals(moduleNode.getNodeName())) {
						Element pluginElement = (Element) moduleNode;
						String value = pluginElement.getTextContent();
						if (value != null) {
							modules.add(value);
						}
					}
				}
			}
		}
	}

	private void loadProviders() {
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		Collection<ProviderConfig> providers = new LinkedList<ProviderConfig>();
		int childSize = children.getLength();

		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if ("conf-providers".equals(childNode.getNodeName())) {
				Element child = (Element) childNode;
				NodeList providersNodes = child.getChildNodes();
				int providersSize = providersNodes.getLength();
				for (int j = 0; j < providersSize; j++) {
					Node providerNode = providersNodes.item(j);
					if ("conf-provider".equals(providerNode.getNodeName())) {
						Element providerElem = (Element) providerNode;
						ProviderConfig pc = new ProviderConfigImpl();
						pc.setType(providerElem.getAttribute("type"));
						pc.setParameters(getParams(providerElem));
						providers.add(pc);
					}
				}
			}
		}

		configuration.setProviderConfigurations(providers);
	}

	private void loadMergePolicies() {
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		Collection<MergePolicyConfig> mergePolicies = new LinkedList<MergePolicyConfig>();

		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if ("merge-policies".equals(childNode.getNodeName())) {
				Element child = (Element) childNode;
				NodeList policiesNodes = child.getChildNodes();
				int policiesSize = policiesNodes.getLength();
				for (int j = 0; j < policiesSize; j++) {
					Node policyNode = policiesNodes.item(j);
					if ("policy".equals(policyNode.getNodeName())) {
						Element policyElem = (Element) policyNode;
						MergePolicyConfig policy = new MergePolicyConfigImpl();
						policy.setName(policyElem.getAttribute("name"));
						String defaultOP = policyElem.getAttribute("default-object-policy");
						if (!"".equals(defaultOP.trim())) {
							policy.setDefaultObjectPolicy(defaultOP);
						} else {
							policy.setDefaultObjectPolicy(null);
						}
						String defaultTP = policyElem.getAttribute("default-type-policy");
						if (!"".equals(defaultTP)) {
							policy.setDefaultTypePolicy(defaultTP);
						} else {
							policy.setDefaultTypePolicy(null);
						}
						NodeList entriesNodes = policyElem.getChildNodes();
						int entriesSize = entriesNodes.getLength();
						Map<String, String> policyEntries = new HashMap<String, String>();
						policy.setPolicyEntries(policyEntries);
						mergePolicies.add(policy);
						for (int k = 0; k < entriesSize; k++) {
							Node entry = entriesNodes.item(k);
							if ("policy-entry".equals(entry.getNodeName())) {
								Element entryElem = (Element) entry;
								String otype = entryElem.getAttribute("object-type");
								String ptype = entryElem.getAttribute("policy-type");
								if (!("".equals(otype.trim())) && !("".equals(ptype.trim()))) {
									policyEntries.put(otype, ptype);
								}
							}
						}
					}
				}

			}
		}

		configuration.setMergePolicies(mergePolicies);
	}

	private void loadPlugins() {
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if ("plugins".equals(childNode.getNodeName())) {
				Element child = (Element) childNode;
				Collection<PluginConfig> plugins = new LinkedList<PluginConfig>();
				configuration.setPlugins(plugins);
				NodeList pluginNodes = child.getChildNodes();
				int pluginSize = pluginNodes.getLength();
				for (int j = 0; j < pluginSize; j++) {
					Node pluginNode = pluginNodes.item(j);
					if ("plugin".equals(pluginNode.getNodeName())) {
						Element pluginElement = (Element) pluginNode;
						PluginConfig pc = new PluginConfigImpl();
						String groupId = pluginElement.getAttribute("groupId");
						String artifactId = pluginElement.getAttribute("artifactId");
						String version = pluginElement.getAttribute("version");

						if (groupId == null) {
							throw new ConfigurationException("Invalid plugin definition. A groupId is necessary.");
						}

						if (artifactId == null) {
							throw new ConfigurationException("Invalid plugin definition. A artifactId is necessary.");
						}
						if (version == null) {
							throw new ConfigurationException("Invalid plugin definition. A version is necessary.");
						}
						pc.setGroupId(groupId);
						pc.setArtifactId(artifactId);
						pc.setVersion(version);
						plugins.add(pc);
					}
				}
			}
		}
	}

	@Override
	public void addTransformationConfig(String chain, String path, TransformationConfig transformationCfg,
			boolean recursive, Integer order, String before) throws Exception {
		AddTransformationXMLAction action = new AddTransformationXMLAction(chain, path, transformationCfg, this,
				recursive, order, before);
		action.execute();

	}

	@Override
	public void createConfig() throws IOException {
		File cfg = new File(configFileName);
		if (!cfg.exists()) {
			if (cfg.createNewFile()) {
				FileWriter fos = new FileWriter(cfg);
				BufferedWriter bos = new BufferedWriter(fos);
				try {
					bos.write(
							"<!DOCTYPE walkmod PUBLIC \"-//WALKMOD//DTD\"  \"http://www.walkmod.com/dtd/walkmod-1.1.dtd\" >");
					bos.newLine();
					bos.write("<walkmod>");
					bos.newLine();
					bos.write("</walkmod>");

				} finally {
					bos.close();
				}
			} else {
				throw new IOException("The system can't create the [" + configFileName + "] file");
			}
		}
	}

	@Override
	public void addProviderConfig(ProviderConfig providerCfg, boolean recursive) throws Exception {

		if (providerCfg != null) {
			AddProviderConfigXMLAction action = new AddProviderConfigXMLAction(providerCfg, this, recursive);
			action.execute();
		}

	}

	@Override
	public void addModules(List<String> modules) throws Exception {
		if (modules != null && !modules.isEmpty()) {
			AddModulesXMLAction action = new AddModulesXMLAction(modules, this);
			action.execute();
		}

	}

	@Override
	public void removeTransformations(String chain, List<String> transformations, boolean recursive) throws Exception {

		if (transformations != null && !transformations.isEmpty()) {
			RemoveTransformationXMLAction action = new RemoveTransformationXMLAction(chain, transformations, this,
					recursive);
			action.execute();
		}
	}

	@Override
	public void setWriter(String chain, String type, String path, boolean recursive, Map<String, String> params) throws Exception {
		if ((type != null && !type.trim().equals("")) || (path != null && !path.trim().equals(""))) {
			SetWriterXMLAction action = new SetWriterXMLAction(chain, type, path, this, recursive, params);
			action.execute();
		}
	}

	@Override
	public void setReader(String chain, String type, String path, boolean recursive, Map<String, String> params) throws Exception {
		if ((type != null && !type.trim().equals("")) || (path != null && !path.trim().equals(""))) {
			SetReaderXMLAction action = new SetReaderXMLAction(chain, type, path, this, recursive, params);
			action.execute();
		}
	}

	@Override
	public void removePluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception {
		RemovePluginXMLAction action = new RemovePluginXMLAction(pluginConfig, this, recursive);
		action.execute();
	}

	@Override
	public void removeModules(List<String> modules) throws Exception {
		if (modules != null && !modules.isEmpty()) {

			RemoveModulesXMLAction action = new RemoveModulesXMLAction(modules, this);
			action.execute();
		}
	}

	@Override
	public void removeProviders(List<String> providers, boolean recursive) throws Exception {
		if (providers != null && !providers.isEmpty()) {
			RemoveProvidersXMLAction action = new RemoveProvidersXMLAction(providers, this, recursive);
			action.execute();
		}

	}

	@Override
	public void removeChains(List<String> chains, boolean recursive) throws Exception {
		if (chains != null && !chains.isEmpty()) {
			RemoveChainsXMLAction action = new RemoveChainsXMLAction(chains, this, recursive);
			action.execute();
		}
	}

	@Override
	public void addConfigurationParameter(String param, String value, String type, String category, String name,
			String chain, boolean recursive) throws Exception {
		if (param == null || value == null) {
			throw new TransformerException("The param and value arguments cannot be null");
		}
		AddConfigurationParameterXMLAction action = new AddConfigurationParameterXMLAction(param, value, type, category,
				name, chain, this, recursive);
		action.execute();

	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Document getDocument() {
		return document;
	}

	public void setConfiguration(Configuration configuration2) {
		this.configuration = configuration2;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	@Override
	public String getFileExtension() {
		return "xml";
	}

	@Override
	public File getConfigurationFile() {
		return new File(configFileName);
	}

	@Override
	public ProjectConfigurationProvider clone(File cfgFile) {
		return new XMLConfigurationProvider(cfgFile.getAbsolutePath(), errorIfMissing);
	}

   @Override
   public void addIncludesToChain(String chain, List<String> includes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception {
      AddIncludesOrExcludesXMLAction action = new AddIncludesOrExcludesXMLAction(includes, chain, recursive, setToReader, setToWriter,false, this);
      action.execute();
      
   }

   @Override
   public void addExcludesToChain(String chain, List<String> excludes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception{
      AddIncludesOrExcludesXMLAction action = new AddIncludesOrExcludesXMLAction(excludes, chain, recursive, setToReader, setToWriter,true, this);
      action.execute();
   }

   @Override
   public void removeExcludesFromChain(String chain, List<String> excludes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception {
      RemoveIncludesOrExcludesXMLAction action = new RemoveIncludesOrExcludesXMLAction(excludes, chain, recursive, setToReader, setToWriter,true, this);
      action.execute();
   }

   @Override
   public void removeIncludesFromChain(String chain, List<String> includes, boolean recursive, boolean setToReader,
         boolean setToWriter) throws Exception {
      RemoveIncludesOrExcludesXMLAction action = new RemoveIncludesOrExcludesXMLAction(includes, chain, recursive, setToReader, setToWriter,false, this);
      action.execute();
      
   }
}
