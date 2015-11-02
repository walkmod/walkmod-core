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

public class RemoveModulesXMLAction extends AbstractXMLConfigurationAction {

	private List<String> modules;

	public RemoveModulesXMLAction(List<String> modules, XMLConfigurationProvider provider) {
		super(provider, false);
		this.modules = modules;
	}

	@Override
	public void doAction() throws Exception {

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

				if ("modules".equals(nodeName)) {
					NodeList moduleNodeList = child.getChildNodes();
					int max = moduleNodeList.getLength();
					for (int j = 0; j < max; j++) {
						String value = moduleNodeList.item(j).getTextContent().trim();
						if (modules.contains(value)) {
							child.removeChild(moduleNodeList.item(j));
							removed++;
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

		return new RemoveModulesXMLAction(modules, (XMLConfigurationProvider) provider);
	}

}
