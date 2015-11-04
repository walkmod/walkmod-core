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

public class SetReaderXMLAction extends AbstractXMLConfigurationAction {

	private String chain;

	private String type;

	private String path;

	public SetReaderXMLAction(String chain, String type, String path, XMLConfigurationProvider provider,
			boolean recursive) {
		super(provider, recursive);
		this.chain = chain;
		this.type = type;
		this.path = path;
	}

	@Override
	public void doAction() throws Exception {

		Document document = provider.getDocument();
		Element rootElement = document.getDocumentElement();
		Element readerParent = null;

		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();

		List<Node> transformationsToRemove = new LinkedList<Node>();

		Element child = null;
		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				child = (Element) childNode;
				final String nodeName = child.getNodeName();

				if ("chain".equals(nodeName)) {

					if (child.hasAttribute("name")) {
						String name = child.getAttribute("name");
						if (name.equals(chain)) {
							readerParent = child;

						}
					}
				} else if ("transformation".equals(nodeName)) {
					if (chain == null || "default".equals(chain)) {
						readerParent = rootElement;
						transformationsToRemove.add(childNode);
					}
				}
			}
		}
		if (readerParent == rootElement) {

			Iterator<Node> it = transformationsToRemove.iterator();
			while (it.hasNext()) {
				Node current = it.next();
				rootElement.removeChild(current);
			}

			Element chainElem = document.createElement("chain");
			chainElem.setAttribute("name", "default");
			rootElement.appendChild(chainElem);
			it = transformationsToRemove.iterator();
			while (it.hasNext()) {
				Node current = it.next();
				chainElem.appendChild(current.cloneNode(true));
			}

			readerParent = chainElem;
		}
		if (readerParent != null) {
			NodeList siblings = readerParent.getChildNodes();
			int limit = siblings.getLength();
			boolean updated = false;
			for (int i = 0; i < limit && !updated; i++) {
				Node childNode = siblings.item(i);
				if (childNode instanceof Element) {
					Element aux = (Element) childNode;
					if ("reader".equals(aux.getNodeName())) {
						if (type != null && !"".equals(type.trim())) {
							aux.setAttribute("type", type);
						}
						if (path != null && !"".equals(path.trim())) {
							aux.setAttribute("path", path);
						}
						updated = true;
					}
				}
			}
			if (!updated) {
				Element readerElement = document.createElement("reader");
				if (type != null && !"".equals(type.trim())) {
					readerElement.setAttribute("type", type);
				}
				if (path != null && !"".equals(path.trim())) {
					readerElement.setAttribute("path", path);
				} else {
					readerElement.setAttribute("path", "src/main/java");
				}
				readerParent.insertBefore(readerElement, readerParent.getFirstChild());
			}
			provider.persist();
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new SetReaderXMLAction(chain, type, path, (XMLConfigurationProvider) provider, recursive);
	}

}
