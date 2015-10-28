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

import java.util.HashSet;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class RemoveProvidersXMLAction extends AbstractXMLConfigurationAction {

	private List<String> providers;

	public RemoveProvidersXMLAction(List<String> providers, XMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.providers = providers;
	}

	@Override
	public void doAction() throws Exception {
		HashSet<String> aux = new HashSet<String>();
		for (String elem : providers) {
			String[] partsType = elem.split(":");
			if (partsType.length == 1) {
				elem = "org.walkmod:walkmod-" + elem + "-plugin:" + elem;
			}
			if (partsType.length != 3 && partsType.length != 1) {
				throw new TransformerException("Invalid conf-provider");
			}
			aux.add(elem);
		}
		Document document = provider.getDocument();
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();

		Element child = null;
		int removed = 0;
		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				child = (Element) childNode;
				final String nodeName = child.getNodeName();

				if ("conf-providers".equals(nodeName)) {
					NodeList moduleNodeList = child.getChildNodes();
					int max = moduleNodeList.getLength();
					for (int j = 0; j < max; j++) {
						Node providerNode = moduleNodeList.item(j);
						if (providerNode instanceof Element) {
							Element elemProvider = (Element) providerNode;
							String type = elemProvider.getAttribute("type");
							String[] partsType = type.split(":");
							if (partsType.length == 1) {
								type = "org.walkmod:walkmod-" + type + "-plugin:" + type;
							}
							if (partsType.length != 3 && partsType.length != 1) {
								throw new TransformerException("Invalid conf-provider");
							}
							if (aux.contains(type)) {
								child.removeChild(providerNode);
								removed++;
							}

						}
					}
					if (removed == max) {
						rootElement.removeChild(child);
					}
				}
			}
		}

		if (removed > 0) {
			provider.persist();
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveProvidersXMLAction(providers, (XMLConfigurationProvider) provider, recursive);
	}

}
