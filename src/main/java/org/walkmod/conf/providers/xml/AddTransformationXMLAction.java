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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class AddTransformationXMLAction extends AbstractXMLConfigurationAction {

	private String chain;
	private String path;
	private TransformationConfig transformationCfg;

	public AddTransformationXMLAction(String chain, String path, TransformationConfig transformationCfg,
			XMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.chain = chain;
		this.path = path;
		this.transformationCfg = transformationCfg;
	}

	public void doAction() throws Exception {
		Document document = provider.getDocument();

		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		if(chain == null || "".equals(chain)){
		   chain = "default";
		}
		if (!"default".equals(chain)) {
			boolean appended = false;
			boolean isTransformationList = false;
			for (int i = 0; i < childSize && !isTransformationList && !appended; i++) {
				Node childNode = children.item(i);
				if (childNode instanceof Element) {
					Element child = (Element) childNode;
					final String nodeName = child.getNodeName();
					if ("chain".equals(nodeName)) {
						String name = child.getAttribute("name");
						if (name.equals(chain)) {
							Element transfElement = createTransformationElement(transformationCfg);

							NodeList innerChainNodes = child.getChildNodes();

							int maxK = innerChainNodes.getLength();
							boolean added = false;
							boolean hasWalker = false;
							for (int k = 0; k < maxK &&!added; k++) {
								Element chainInnerElem = (Element) innerChainNodes.item(k);
								hasWalker = hasWalker || chainInnerElem.getNodeName().equals("walker");
								if (hasWalker) {
									NodeList transfList = chainInnerElem.getChildNodes();
									int maxj = transfList.getLength();

									for (int j = 0; j < maxj && !added; j++) {
										if (transfList.item(j).getNodeName().equals("transformations")) {
											transfList.item(j).appendChild(transfElement);
											added = true;
										}
									}

								} else if (chainInnerElem.getNodeName().equals("writer")) {
									child.insertBefore(transfElement, chainInnerElem);
									added = true;
								}

							}
							if (!added) {
								child.appendChild(transfElement);
							}

							appended = true;

						}
					} else if ("transformation".equals(nodeName)) {
						isTransformationList = true;
					}
				}
			}
			if (isTransformationList) {
				Configuration configuration = new ConfigurationImpl();
				provider.setConfiguration(configuration);
				// we write specifically a default chain, and
				// afterwards, we
				// add the requested one.
				provider.loadChains();
				Collection<ChainConfig> chainCfgs = configuration.getChainConfigs();
				ChainConfig chainCfg = chainCfgs.iterator().next();
				NodeList child = rootElement.getChildNodes();
				int limit = child.getLength();
				for(int i = 0; i < limit; i++){
				   Node item = child.item(i);
				   if(item instanceof Element){
				      Element auxElem = (Element) item;
				      if(auxElem.getNodeName().equals("transformation")){
				         rootElement.removeChild(auxElem);
				      }
				   }
				}
				
				rootElement.appendChild(createChainElement(chainCfg));
			}
			if (!appended) {
				ChainConfig chainCfg = new ChainConfigImpl();
				chainCfg.setName(chain);
				provider.addDefaultReaderConfig(chainCfg);
				provider.addDefaultWriterConfig(chainCfg);
				if (path != null && !"".equals(path.trim())) {

					chainCfg.getReaderConfig().setPath(path);
					chainCfg.getWriterConfig().setPath(path);
				}
				provider.addDefaultWalker(chainCfg);
				WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
				List<TransformationConfig> transfs = new LinkedList<TransformationConfig>();
				transfs.add(transformationCfg);
				walkerCfg.setTransformations(transfs);
				chainCfg.setWalkerConfig(walkerCfg);
				rootElement.appendChild(createChainElement(chainCfg));
			}
			provider.persist();
		} else {
			Element chainNode = null;
			boolean containsChains = false;
			for (int i = 0; i < childSize && !containsChains; i++) {
				Node childNode = children.item(i);
				if (childNode instanceof Element) {
					chainNode = (Element) childNode;
					final String nodeName = chainNode.getNodeName();
					containsChains = "chain".equals(nodeName);
				}
			}
			if (containsChains) {
				String attrName = chainNode.getAttribute("name");
				if (attrName == null || attrName.equals("") || attrName.equals("default")) {
					if (path != null && !"".equals(path.trim())) {
						NodeList chainChildren = chainNode.getChildNodes();
						for (int i = 0; i < chainChildren.getLength(); i++) {
							Node childNode = chainChildren.item(i);
							if (childNode.getNodeName().equals("reader")) {
								Element aux = (Element) childNode;
								if (!aux.getAttribute("path").equals(path.trim())) {
									throw new TransformerException(
											"The user must specify a chain name (new or existing) where to add the transformation: ["
													+ transformationCfg.getType() + "]");
								}

							}
						}
					}
				} else {
				   ChainConfig chainCfg = new ChainConfigImpl();
	            chainCfg.setName("default");
	            provider.addDefaultReaderConfig(chainCfg);
	            provider.addDefaultWriterConfig(chainCfg);
	            provider.addDefaultWalker(chainCfg);
	            WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
	            List<TransformationConfig> transfs = new LinkedList<TransformationConfig>();
	            transfs.add(transformationCfg);
	            walkerCfg.setTransformations(transfs);
	            chainCfg.setWalkerConfig(walkerCfg);
	            rootElement.appendChild(createChainElement(chainCfg));
	            provider.persist();
	            return;
				}

			}

			if (path != null && !"".equals(path.trim())) {
				Configuration configuration = new ConfigurationImpl();

				provider.setConfiguration(configuration);

				provider.loadChains();
				Collection<ChainConfig> chainCfgs = configuration.getChainConfigs();
				if (chainCfgs.isEmpty()) {
					ChainConfig chainCfg = new ChainConfigImpl();
					chainCfg.setName("default");
					provider.addDefaultReaderConfig(chainCfg);

					provider.addDefaultWriterConfig(chainCfg);
					if (path != null && !"".equals(path.trim())) {
						chainCfg.getReaderConfig().setPath(path);
						chainCfg.getWriterConfig().setPath(path);
					}
					provider.addDefaultWalker(chainCfg);
					WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
					List<TransformationConfig> transfs = new LinkedList<TransformationConfig>();
					transfs.add(transformationCfg);
					walkerCfg.setTransformations(transfs);
					chainCfg.setWalkerConfig(walkerCfg);

					NodeList childrenNodes = rootElement.getChildNodes();
					int limitChildren = childrenNodes.getLength();

					for (int i = 0; i < limitChildren; i++) {
						rootElement.removeChild(childrenNodes.item(i));
					}

					rootElement.appendChild(createChainElement(chainCfg));
					provider.persist();
					return;

				} else {
					ChainConfig chainCfg = chainCfgs.iterator().next();
					chainCfg.getReaderConfig().setPath(path);
					chainCfg.getWriterConfig().setPath(path);
					chainCfg.getWalkerConfig().getTransformations().add(transformationCfg);
					document.removeChild(rootElement);
					document.appendChild(createChainElement(chainCfg));
				}
				provider.persist();
				return;
			}

			rootElement.appendChild(createTransformationElement(transformationCfg));
			provider.persist();
		}
	}

	@Override
	public AddTransformationXMLAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddTransformationXMLAction(chain, path, transformationCfg, (XMLConfigurationProvider) provider,
				recursive);
	}

}
