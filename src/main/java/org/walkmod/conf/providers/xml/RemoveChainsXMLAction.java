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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class RemoveChainsXMLAction extends AbstractXMLConfigurationAction {

	private List<String> chains;

	public RemoveChainsXMLAction(List<String> chains, XMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.chains = chains;
	}

	@Override
	public void doAction() throws Exception {
		Document document = provider.getDocument();
		HashSet<String> aux = new HashSet<String>(chains);

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

				if ("chain".equals(nodeName)) {
					if (aux.contains(child.getAttribute("name"))) {
						rootElement.removeChild(childNode);
						removed++;
					}
				}

				else if ("transformation".equals(nodeName) && aux.contains("default")) {
					rootElement.removeChild(childNode);
					removed++;
				}
			}
		}

		if (removed > 0) {

			provider.persist();
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveChainsXMLAction(chains, (XMLConfigurationProvider) provider, recursive);
	}

}
