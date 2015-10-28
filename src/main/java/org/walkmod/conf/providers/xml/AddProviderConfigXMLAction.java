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
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class AddProviderConfigXMLAction extends AbstractXMLConfigurationAction {

	private ProviderConfig providerCfg;

	public AddProviderConfigXMLAction(ProviderConfig providerCfg, XMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.providerCfg = providerCfg;
	}

	@Override
	public void doAction() throws Exception {

		Document document = provider.getDocument();
		Element rootElement = document.getDocumentElement();
		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		boolean exists = false;
		Element child = null;

		for (int i = 0; i < childSize && !exists; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				child = (Element) childNode;
				final String nodeName = child.getNodeName();

				if ("conf-providers".equals(nodeName)) {

					Node aux = (Node) child;
					NodeList cfgchildren = aux.getChildNodes();

					int cfgchildrenSize = cfgchildren.getLength();

					for (int j = 0; j < cfgchildrenSize && !exists; j++) {
						Node provNode = cfgchildren.item(j);
						Element entryElem = (Element) provNode;
						String otype = entryElem.getAttribute("name");
						exists = otype.equals(providerCfg.getType());
					}

				}
			}
		}
		if (!exists) {
			Element element = document.createElement("conf-provider");

			String type = providerCfg.getType();
			if (type != null && !"".equals(type)) {
				element.setAttribute("type", type);
			}

			Map<String, Object> params = providerCfg.getParameters();
			List<Element> paramListEment = createParamsElement(params);
			if (paramListEment != null) {

				for (Element param : paramListEment) {
					element.appendChild(param);
				}
			}
			if (child == null) {
				child = document.createElement("conf-providers");
				rootElement.appendChild(child);
			}
			child.appendChild(element);
			provider.persist();
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddProviderConfigXMLAction(providerCfg, (XMLConfigurationProvider) provider, recursive);
	}

}
