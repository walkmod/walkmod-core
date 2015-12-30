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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class AddConfigurationParameterXMLAction extends AbstractXMLConfigurationAction {

	private String param;
	private String value;
	private String type;
	private String category;
	private String name;
	private String chain;

	public AddConfigurationParameterXMLAction(String param, String value, String type, String category, String name,
			String chain, XMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.param = param;
		this.value = value;
		this.type = type;
		this.category = category;
		this.name = name;
		this.chain = chain;

	}

	@Override
	public void doAction() throws Exception {
		List<Element> elementsToModify = new LinkedList<Element>();
		Document document = provider.getDocument();

		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();

		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				Element child = (Element) childNode;
				final String nodeName = child.getNodeName();
				Element chainToAnalyze = null;
				if ("chain".equals(nodeName)) {
					if (chain != null && !chain.equals("")) {
						chainToAnalyze = child;
					} else {
						chainToAnalyze = child;
					}
					if (chainToAnalyze != null) {

						NodeList chainChildren = chainToAnalyze.getChildNodes();
						int limit = chainChildren.getLength();

						for (int j = 0; j < limit; j++) {
							Node chainChild = chainChildren.item(j);
							if (chainChild instanceof Element) {
								Element chainElement = (Element) chainChild;
								final String chainElementName = chainElement.getNodeName();

								if (category != null && category.equals(chainElementName) || category == null) {
									analizeBean(chainElement, type, name, elementsToModify);

									if (chainElementName.equals("walker")
											&& (category == null || category.equals("transformation"))) {
										NodeList walkerChildren = chainChild.getChildNodes();
										int limit2 = walkerChildren.getLength();

										for (int k = 0; k < limit2; k++) {
											Node walkerChild = walkerChildren.item(k);
											if (walkerChild instanceof Element) {
												if (walkerChild.getNodeName().equals("transformations")) {
													NodeList transform = walkerChild.getChildNodes();
													int limit3 = transform.getLength();

													for (int h = 0; h < limit3; h++) {
														Node transformationItem = transform.item(h);
														if (transformationItem instanceof Element) {
															analizeBean((Element) transformationItem, type, name,
																	elementsToModify);
														}
													}

												} else {
													analizeBean((Element) walkerChild, type, name, elementsToModify);
												}
											}
										}
									}
								}
							}
						}
					}
				} else if ("transformation".equals(nodeName)) {

					analizeBean(child, type, name, elementsToModify);

				}
			}
		}

		Iterator<Element> it = elementsToModify.iterator();
		while (it.hasNext()) {
			Element current = it.next();
			NodeList childrenElement = current.getChildNodes();
			int limit = childrenElement.getLength();
			boolean found = false;
			for (int i = 0; i < limit && !found; i++) {
				Node item = childrenElement.item(i);

				if (item.getNodeName().equals("param")) {
					Element aux = (Element) item;
					if (aux.getAttribute("name").equals(param)) {
						aux.setTextContent(value);
						found = true;
					}
				}
			}
			if (!found) {
				Element paramElement = document.createElement("param");
				paramElement.setAttribute("name", param);
				paramElement.setTextContent(value);
				current.appendChild(paramElement);
			}
		}
		if (!elementsToModify.isEmpty()) {
			provider.persist();
		}

	}

	private void analizeBean(Element chainElement, String type, String name, List<Element> elementsToModify) {
		if (chainElement.hasAttribute("type")) {

			String typeAttribute = chainElement.getAttribute("type");
			
			if (type != null && !"".equals(type) && type.equals(typeAttribute)) {
				if (name != null && !name.equals("")) {
					if (chainElement.hasAttribute("name")) {
						if (name.equals(chainElement.getAttribute("name"))) {
							elementsToModify.add(chainElement);
						}
					}
				} else {
					elementsToModify.add(chainElement);
				}
			}
			else if(name != null && !"".equals(name) && chainElement.hasAttribute("name")){
			   if (name.equals(chainElement.getAttribute("name"))) {
               elementsToModify.add(chainElement);
            }
			}
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddConfigurationParameterXMLAction(param, value, type, category, name, chain,
				(XMLConfigurationProvider) provider, recursive);
	}

}
