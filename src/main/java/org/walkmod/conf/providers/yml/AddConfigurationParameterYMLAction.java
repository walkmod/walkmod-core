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
package org.walkmod.conf.providers.yml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AddConfigurationParameterYMLAction extends AbstractYMLConfigurationAction {
	String param;
	String value;
	String type;
	String category;
	String name;
	String chain;

	public AddConfigurationParameterYMLAction(String param, String value, String type, String category, String name,
			String chain, YAMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.param = param;
		this.value = value;
		this.type = type;
		this.category = category;
		this.name = name;
		this.chain = chain;
	}

	private void analyzeNode(JsonNode node, String type, String name, List<JsonNode> elementsToModify) {
		if (type != null && node.has("type")) {
			if (node.get("type").textValue().equals(type)) {
				if (name != null && node.has("name")) {
					if (node.get("name").textValue().equals(name)) {
						elementsToModify.add(node);
					}
				} else {
					elementsToModify.add(node);
				}
			}

		}
	}

	@Override
	public void doAction(JsonNode node) throws Exception {

		List<JsonNode> elementsToModify = new LinkedList<JsonNode>();

		if (node.has("chains")) {
			JsonNode aux = node.get("chains");
			if (aux.isArray()) {
				ArrayNode chainsList = (ArrayNode) node.get("chains");
				Iterator<JsonNode> it = chainsList.iterator();

				while (it.hasNext()) {
					JsonNode next = it.next();
					JsonNode walkerNode = null;
					JsonNode transformationsNode = null;
					if (chain == null || chain.equals(next.get("name").asText())) {
						if (category != null) {
							if (node.has(category)) { // reader, walker,
														// writer
								JsonNode categoryNode = node.get(category);
								analyzeNode(categoryNode, type, name, elementsToModify);
							} else {
								if (category.equals("transformation")) {
									if (node.has("transformations")) {
										transformationsNode = node.get("transformations");
									}
								}
								if (node.has("walker")) {
									walkerNode = node.get("walker");
								}
							}
						} else {
							Iterator<JsonNode> it2 = next.iterator();
							while (it2.hasNext()) {
								analyzeNode(it2.next(), type, name, elementsToModify);
							}
							if (next.has("walker")) {
								walkerNode = next.get("walker");

							} else if (next.has("transformations")) {
								transformationsNode = next.get("transformations");
							}
						}
					}
					if (walkerNode != null) {
						if (category != null) {

							if (walkerNode.has(category)) {
								JsonNode categoryNode = node.get(category);
								analyzeNode(categoryNode, type, name, elementsToModify);
							}
						} else if (walkerNode.has("transformations")) {
							transformationsNode = walkerNode.get("transformations");

						}

					}
					if (transformationsNode != null) {
						if (transformationsNode.isArray()) {
							ArrayNode transformationsArray = (ArrayNode) transformationsNode;
							Iterator<JsonNode> it2 = transformationsArray.iterator();

							while (it2.hasNext()) {
								JsonNode current = it2.next();

								analyzeNode(current, type, name, elementsToModify);
							}
						}
					}
				}
			}
		} else if (node.has("transformations") && (category == null || "transformation".equals(category))) {

			JsonNode transformationsNode = node.get("transformations");
			if (transformationsNode.isArray()) {
				ArrayNode transformationsArray = (ArrayNode) transformationsNode;
				Iterator<JsonNode> it = transformationsArray.iterator();

				while (it.hasNext()) {
					JsonNode current = it.next();
					analyzeNode(current, type, name, elementsToModify);
				}
			}

		}

		Iterator<JsonNode> it = elementsToModify.iterator();
		while (it.hasNext()) {
			JsonNode current = it.next();
			if (current.isObject()) {
				JsonNode params = null;
				if (current.has("params")) {
					params = current.get("params");
					if (params.isObject()) {
						((ObjectNode) params).set(param, new TextNode(value));
					}
				} else {
					Map<String, Object> paramToAdd = new HashMap<String, Object>();
					paramToAdd.put(param, value);
					populateParams((ObjectNode) current, paramToAdd);
				}
			}
		}

		if (!elementsToModify.isEmpty()) {
			provider.write(node);
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new AddConfigurationParameterYMLAction(param, value, type, category, name, chain,
				(YAMLConfigurationProvider) provider, recursive);
	}

}
