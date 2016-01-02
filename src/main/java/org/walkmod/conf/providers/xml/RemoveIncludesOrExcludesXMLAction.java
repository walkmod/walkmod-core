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

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class RemoveIncludesOrExcludesXMLAction extends AbstractXMLConfigurationAction {

	private List<String> includes;
	private String chain;
	private boolean setToReader;
	private boolean setToWriter;
	private boolean isExcludes;

	public RemoveIncludesOrExcludesXMLAction(List<String> includes, String chain, boolean recursive,
			boolean setToReader, boolean setToWriter, boolean isExcludes, XMLConfigurationProvider provider) {
		super(provider, recursive);
		this.includes = includes;
		this.chain = chain;
		this.setToReader = setToReader;
		this.setToWriter = setToWriter;
	}

	@Override
	public void doAction() throws Exception {
		Document document = provider.getDocument();
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();

		if (chain == null) {
			chain = "default";
		}
		boolean updated = false;
		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				Element child = (Element) childNode;
				final String nodeName = child.getNodeName();
				if ("chain".equals(nodeName)) {
					String name = child.getAttribute("name");
					if (name.equals(chain)) {

						NodeList chainChildren = child.getChildNodes();
						int chainChildrenSize = chainChildren.getLength();

						for (int j = 0; j < chainChildrenSize; j++) {
							Node chainChild = chainChildren.item(j);
							if (chainChild instanceof Element) {
								Element elementChain = (Element) chainChild;
								String elementName = elementChain.getNodeName();
								if (elementName.equals("reader") && setToReader) {
									updateElement(elementChain);
									updated = true;
								} else if (elementName.equals("writer") && setToWriter) {
									updateElement(elementChain);
									updated = true;
								}
							}
						}
					}
				}
			}
		}
		if (updated) {
			provider.persist();
		}
	}

	private void updateElement(Element elementChain) {
		String label = "include";
		if (isExcludes) {
			label = "exclude";
		}
		NodeList childNodes = elementChain.getChildNodes();
		int limit = childNodes.getLength();

		for (int i = 0; i < limit; i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(label)) {
				Element aux = (Element) childNode;
				String value = aux.getAttribute("wildcard");
				if (includes.contains(value)) {
					elementChain.removeChild(childNode);
				}
			}
		}
	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new RemoveIncludesOrExcludesXMLAction(includes, chain, recursive, setToReader, setToWriter, isExcludes,
				(XMLConfigurationProvider) provider);
	}

}
