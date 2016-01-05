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

public class RemoveTransformationXMLAction extends AbstractXMLConfigurationAction {

	private String chain;
	private List<String> transformations;

	public RemoveTransformationXMLAction(String chain, List<String> transformations, XMLConfigurationProvider provider,
			boolean recursive) {
		super(provider, recursive);
		this.chain = chain;
		this.transformations = transformations;
	}

	private boolean removeTransformation(Element rootNode, HashSet<String> transformations) {
		NodeList children = rootNode.getChildNodes();
		int childSize = children.getLength();
		Element child = null;
		boolean modified = false;
		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				child = (Element) childNode;
				final String nodeName = child.getNodeName();

				if ("transformations".equals(nodeName)) {
					NodeList transfNodeList = child.getChildNodes();
					int limit = transfNodeList.getLength();
					for (int j = 0; j < limit; j++) {
						Node aux = transfNodeList.item(j);
						Element elemAux = (Element) aux;
						if (elemAux.hasAttribute("type")) {
							String type = elemAux.getAttribute("type");
							if (transformations.contains(type)) {
								child.removeChild(aux);
								modified = true;
								i--;
							}
						}
					}
					if (transfNodeList.getLength() == 0) {
						rootNode.getParentNode().removeChild(rootNode);
					}
				} else if (("transformation").equals(nodeName)) {

					if (child.hasAttribute("type")) {
						String type = child.getAttribute("type");
						if (transformations.contains(type)) {
							rootNode.removeChild(child);
							i--;
							modified = true;
						}
					}

					boolean thereAreMoreTransformations = false;
					int childSize2 = children.getLength();
					for (int j = 0; j < childSize2 && !thereAreMoreTransformations; j++) {
						Node childNode2 = children.item(j);
						if (childNode2 instanceof Element) {
							Element child2 = (Element) childNode2;
							final String nodeName2 = child2.getNodeName();
							thereAreMoreTransformations = nodeName2.equals("transformation");
						}
					}
					if (!thereAreMoreTransformations) {
						if (!rootNode.getNodeName().equals("walkmod")) {
							rootNode.getParentNode().removeChild(rootNode);
						}
					}
				}
			}
		}
		return modified;
	}

	@Override
	public void doAction() throws Exception {

		Document document = provider.getDocument();

		Element rootElement = document.getDocumentElement();
		HashSet<String> transformationsToRemove = new HashSet<String>(transformations);
		boolean modified = false;
		boolean hasChains = false;

		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();

		Element child = null;

		for (int i = 0; i < childSize; i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				child = (Element) childNode;
				final String nodeName = child.getNodeName();

				if ("chain".equals(nodeName)) {
					hasChains = true;
					if (chain == null) {
						chain = "default";
					}
					if (child.hasAttribute("name")) {
						String name = child.getAttribute("name");
						if (name.equals(chain)) {
							removeTransformation(child, transformationsToRemove);
							modified = true;
						}

					}
				}

			}
		}
		if (!hasChains && (chain == null || "default".equals(chain))) {
			modified = removeTransformation(rootElement, transformationsToRemove);
		}

		if (modified) {
			provider.persist();
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveTransformationXMLAction(chain, transformations, (XMLConfigurationProvider) provider, recursive);
	}

}
