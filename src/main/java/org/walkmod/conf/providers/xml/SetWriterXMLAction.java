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
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class SetWriterXMLAction extends AbstractXMLConfigurationAction {

	private String chain;
	private String type;
	private String path;
	private Map<String, String> params;

	public SetWriterXMLAction(String chain, String type, String path, XMLConfigurationProvider provider,
			boolean recursive, Map<String, String> params) {
		super(provider, recursive);
		this.chain = chain;
		this.type = type;
		this.path = path;
		this.params = params;
	}

	@Override
	public void doAction() throws Exception {
		Document document = provider.getDocument();
		Element rootElement = document.getDocumentElement();
		Element writerParent = null;

		NodeList children = rootElement.getChildNodes();
		int childSize = children.getLength();
		Element writerElem = null;
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
							writerParent = child;

						}
					}
				} else if ("transformation".equals(nodeName)) {
					if (chain == null || "default".equals(chain)) {
						writerParent = rootElement;
					}
				}
			}
		}
		if (writerParent == rootElement) {

			Element chainElem = document.createElement("chain");
			rootElement.appendChild(chainElem);
			chainElem.setAttribute("name", "default");
			

			List<Node> tranformationsToRemove = new LinkedList<Node>();

			for (int i = 0; i < childSize; i++) {
				Node currentChild = children.item(i);
				if (currentChild.getNodeName().equals("transformation")) {
					tranformationsToRemove.add(currentChild);
				}
			}
			Iterator<Node> it = tranformationsToRemove.iterator();
			while (it.hasNext()) {
				Node current = it.next();
				rootElement.removeChild(current);

			}
			
			it = tranformationsToRemove.iterator();
			while(it.hasNext()){
				Node trsnfNode = it.next();
				chainElem.appendChild(trsnfNode);
			}
			

			writerParent = chainElem;
		}
		if (writerParent != null) {
			NodeList siblings = writerParent.getChildNodes();
			int limit = siblings.getLength();
			boolean updated = false;
			for (int i = 0; i < limit && !updated; i++) {
				Node childNode = siblings.item(i);
				if (childNode instanceof Element) {
					Element aux = (Element) childNode;
					if ("writer".equals(aux.getNodeName())) {
						if (type != null && !"".equals(type.trim())) {
							aux.setAttribute("type", type);
						}
						if (path != null && !"".equals(path.trim())) {
							aux.setAttribute("path", path);
						}
						writerElem = aux;
						updated = true;
					}
				}
			}
			if (!updated) {
				writerElem = document.createElement("writer");
				if (type != null && !"".equals(type.trim())) {
					writerElem.setAttribute("type", type);
				}
				if (path != null && !"".equals(path.trim())) {
					writerElem.setAttribute("path", path);
				} else {
					writerElem.setAttribute("path", "src/main/java");
				}
				writerParent.appendChild(writerElem);
			}
			if(params != null && !params.isEmpty() && writerElem != null){
			   NodeList childrenWriter = writerElem.getChildNodes();
			   int limitChildren = childrenWriter.getLength();
			   Element previousElem = null;
			   for(int i = 0; i < limitChildren; i++){
			      Node childWriter = childrenWriter.item(i);
			      if(childWriter.getNodeName().equals("param")){
			         Element param = (Element) childWriter;
			        
			         if(params.containsKey(param.getAttribute("name"))){
			            param.setTextContent(params.get(param.getAttribute("name")).toString());
			            params.remove(param.getAttribute("name"));
			         }
			      }
			      else if(previousElem == null){
			         previousElem = (Element) childWriter;
			      }
			   }
			   Set<String> keys = params.keySet();
			   for(String key:keys){
			      Element elem = document.createElement("param");
			      elem.setAttribute("name", key);
			      elem.setTextContent(params.get(key).toString());
			      if(previousElem == null){
			         writerElem.appendChild(elem);
			      }
			      else{
			         writerElem.insertBefore(elem, previousElem);
			         previousElem = elem;
			      }
			     
			   }
			}
			provider.persist();
		}

	}

	@Override
	public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new SetWriterXMLAction(chain, type, path, (XMLConfigurationProvider) provider, recursive, params);
	}

}
