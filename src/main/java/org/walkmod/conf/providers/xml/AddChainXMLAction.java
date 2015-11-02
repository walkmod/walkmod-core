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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class AddChainXMLAction extends AbstractXMLConfigurationAction {

	private ChainConfig chainCfg;

	public AddChainXMLAction(ChainConfig chainCfg, XMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.chainCfg = chainCfg;
	}

	@Override
	public void doAction() throws Exception {
		Document document = provider.getDocument();
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		if (chainCfg.getName() != null && !"".equals(chainCfg.getName())) {
			for (int i = 0; i < childSize; i++) {
				Node childNode = children.item(i);
				if (childNode instanceof Element) {
					Element child = (Element) childNode;
					final String nodeName = child.getNodeName();
					if ("chain".equals(nodeName)) {
						String name = child.getAttribute("name");
						if (name.equals(chainCfg.getName())) {
							return;
						}
					}
				}
			}
		}
		rootElement.appendChild(createChainElement(chainCfg));

		provider.persist();

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new AddChainXMLAction(chainCfg, (XMLConfigurationProvider) provider, recursive);
	}

}
