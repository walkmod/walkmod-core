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
package org.walkmod.conf.providers.xml;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.providers.ConfigurationAction;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public abstract class AbstractXMLConfigurationAction implements ConfigurationAction {

	protected XMLConfigurationProvider provider;

	private boolean recursive = false;

	public AbstractXMLConfigurationAction(XMLConfigurationProvider provider, boolean recursive) {

		this.provider = provider;
		this.recursive = recursive;
	}

	@Override
	public void execute() throws Exception {
		File file = new File(provider.getConfigFileName());

		if (!file.exists()) {
			provider.createConfig();
			provider.init(new ConfigurationImpl());
		} else {
			provider.init(new ConfigurationImpl());
		}
		if (recursive) {
			
			Document document = provider.getDocument();
			Element rootElement = document.getDocumentElement();
			NodeList children = rootElement.getChildNodes();
			int childSize = children.getLength();
			boolean containsModules = false;
			
			for (int i = 0; i < childSize; i++) {
				Node childNode = children.item(i);
				if (childNode instanceof Element) {
					Element child  = (Element) childNode;
					final String nodeName = child.getNodeName();

					if ("modules".equals(nodeName)) {
						containsModules = true;
						NodeList moduleNodeList = child.getChildNodes();
						int max = moduleNodeList.getLength();
						for (int j = 0; j < max; j++) {
							String cfg = provider.getConfigFileName();
							if (cfg != null) {
								File auxFile = new File(cfg).getCanonicalFile().getParentFile();
								File moduleFileDir = new File(auxFile, moduleNodeList.item(j).getTextContent());
								XMLConfigurationProvider aux = new XMLConfigurationProvider(moduleFileDir.getAbsolutePath()
										+ File.separator + "walkmod.xml", false);

								AbstractXMLConfigurationAction ct = clone(aux, recursive);
								ct.execute();
							}
						}
					}
				}
			}
			if(!containsModules){
				doAction();
			}
		} else {
			doAction();
		}

	}

	public abstract void doAction() throws Exception;

	public List<Element> createParamsElement(Map<String, Object> params) {
		List<Element> result = null;
		if (params != null && !params.isEmpty()) {
			Document document = provider.getDocument();
			result = new LinkedList<Element>();
			Set<String> paramLabels = params.keySet();
			for (String label : paramLabels) {
				Element param = document.createElement("param");
				param.setAttribute("name", label);
				param.setNodeValue(params.get(label).toString());
				result.add(param);
			}

		}
		return result;
	}

	public void createReaderOrWriterContent(Element root, String path, String type, Map<String, Object> params,
			String[] includes, String[] excludes) {

		root.setAttribute("path", path);

		if (type != null && !"".equals(type)) {
			root.setAttribute("type", type);
		}

		List<Element> paramListEment = createParamsElement(params);
		if (paramListEment != null) {

			for (Element param : paramListEment) {
				root.appendChild(param);
			}
		}

		List<Element> includeElementList = createIncludeList(includes);
		if (includeElementList != null) {
			for (Element includeElement : includeElementList) {
				root.appendChild(includeElement);
			}
		}

		List<Element> excludeElementList = createExcludeList(excludes);

		if (excludeElementList != null) {
			for (Element excludeElement : excludeElementList) {
				root.appendChild(excludeElement);
			}
		}

	}

	public List<Element> createIncludeList(String[] includes) {
		List<Element> result = null;
		if (includes != null) {
			result = new LinkedList<Element>();
			Document document = provider.getDocument();
			for (String include : includes) {
				Element includeElem = document.createElement("include");
				includeElem.setNodeValue(include);
				result.add(includeElem);
			}
		}

		return result;
	}

	public List<Element> createExcludeList(String[] excludes) {
		List<Element> result = null;
		if (excludes != null) {
			result = new LinkedList<Element>();
			Document document = provider.getDocument();
			for (String exclude : excludes) {
				Element excludeElem = document.createElement("exclude");
				excludeElem.setNodeValue(exclude);
				result.add(excludeElem);
			}
		}

		return result;
	}

	private List<Element> createTransformationList(List<TransformationConfig> transformations) {
		List<Element> result = null;

		if (transformations != null) {
			result = new LinkedList<Element>();
			Document document = provider.getDocument();
			for (TransformationConfig tcfg : transformations) {
				Element trans = document.createElement("transformation");
				String name = tcfg.getName();

				if (name != null) {
					trans.setAttribute("name", name);
				}

				trans.setAttribute("type", tcfg.getType());

				String mergePolicy = tcfg.getMergePolicy();
				if (mergePolicy != null) {
					trans.setAttribute("merge-policy", mergePolicy);
				}
				if (tcfg.isMergeable()) {
					trans.setAttribute("isMergeable", "true");
				}

				Map<String, Object> params = tcfg.getParameters();
				List<Element> paramListEment = createParamsElement(params);
				if (paramListEment != null) {

					for (Element param : paramListEment) {
						trans.appendChild(param);
					}
				}
				result.add(trans);
			}
		}

		return result;
	}

	public Element createTransformationElement(TransformationConfig transformationCfg) {
		Document document = provider.getDocument();
		Element element = document.createElement("transformation");

		String name = transformationCfg.getName();
		if (name != null && !"".equals(name)) {
			element.setAttribute("name", transformationCfg.getName());
		}

		String type = transformationCfg.getType();
		if (type != null && !"".equals(type)) {
			element.setAttribute("type", type);
		}

		String mergePolicy = transformationCfg.getMergePolicy();
		if (mergePolicy != null && !"".equals(mergePolicy)) {
			element.setAttribute("merge-policy", mergePolicy);
		}

		if (transformationCfg.isMergeable()) {
			element.setAttribute("isMergeable", "true");
		}
		Map<String, Object> params = transformationCfg.getParameters();
		List<Element> paramListEment = createParamsElement(params);
		if (paramListEment != null) {

			for (Element param : paramListEment) {
				element.appendChild(param);
			}
		}

		return element;
	}

	public Element createChainElement(ChainConfig chainCfg) {
		Document document = provider.getDocument();

		Element element = document.createElement("chain");
		String name = chainCfg.getName();
		if (name != null && !"".equals(name)) {
			element.setAttribute("name", chainCfg.getName());
		}

		ReaderConfig rConfig = chainCfg.getReaderConfig();
		if (rConfig != null) {

			if (rConfig.getType() != null || rConfig.getPath() != null) {

				Element reader = document.createElement("reader");
				createReaderOrWriterContent(reader, rConfig.getPath(), rConfig.getType(), rConfig.getParameters(),
						rConfig.getIncludes(), rConfig.getExcludes());

				element.appendChild(reader);
			}

		}
		WalkerConfig wConfig = chainCfg.getWalkerConfig();
		if (wConfig != null) {
			// (param*, parser?, transformations)
			Map<String, Object> params = wConfig.getParams();
			List<Element> result = createTransformationList(wConfig.getTransformations());

			if (params == null && (wConfig.getType() == null || "".equals(wConfig.getType()))) {

				if (result != null) {
					for (Element transformationElement : result) {
						element.appendChild(transformationElement);
					}
				}
			} else {
				Element walker = document.createElement("walker");
				String type = wConfig.getType();

				if (type != null && !"".equals(type)) {
					walker.setAttribute("type", type);
				}

				List<Element> paramListEment = createParamsElement(params);
				if (paramListEment != null) {

					for (Element param : paramListEment) {
						walker.appendChild(param);
					}
				}

				Element transformationList = document.createElement("transformations");
				if (result != null) {
					for (Element transformationElement : result) {
						transformationList.appendChild(transformationElement);
					}
				}
				walker.appendChild(transformationList);
				element.appendChild(walker);

			}
		}
		WriterConfig writerConfig = chainCfg.getWriterConfig();
		if (writerConfig != null) {

			if (writerConfig.getType() != null || writerConfig.getPath() != null) {
				Element writer = document.createElement("writer");
				createReaderOrWriterContent(writer, rConfig.getPath(), rConfig.getType(), rConfig.getParameters(),
						rConfig.getIncludes(), rConfig.getExcludes());

				element.appendChild(writer);
			}

		}

		return element;
	}

	public abstract AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive);
}
