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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class RemoveTransformationYMLAction extends AbstractYMLConfigurationAction {

	private String chain;
	private List<String> transformations;

	public RemoveTransformationYMLAction(String chain, List<String> transformations,
			YAMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.chain = chain;
		this.transformations = transformations;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		HashSet<String> transList = new HashSet<String>(transformations);
		JsonNode transfListNode = null;
		if (chain == null || "".equals(chain)) {
			if (node.has("transformations")) {
				transfListNode = node.get("transformations");

			}
		} else {
			if (node.has("chains")) {
				JsonNode chainsListNode = node.get("chains");
				if (chainsListNode.isArray()) {
					Iterator<JsonNode> it = chainsListNode.iterator();
					boolean found = false;
					while (it.hasNext() && !found) {
						JsonNode current = it.next();
						if (current.has("name")) {
							String name = current.get("name").asText();
							found = name.equals(chain);

							if (current.has("transformations")) {
								transfListNode = current.get("transformations");
							}
						}
					}
				}
			}
		}

		if (transfListNode != null) {
			if (transfListNode.isArray()) {
				ArrayNode transArray = (ArrayNode) transfListNode;
				Iterator<JsonNode> it = transArray.iterator();
				List<Integer> removeIndex = new LinkedList<Integer>();
				int i = 0;
				while (it.hasNext()) {
					JsonNode transfNode = it.next();
					if (transfNode.has("type")) {
						String type = transfNode.get("type").asText();
						if (transList.contains(type)) {
							removeIndex.add(i);
						}
					}
					i++;
				}
				for (Integer pos : removeIndex) {
					transArray.remove(pos);
				}
			}
			provider.write(node);
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveTransformationYMLAction(chain, transformations, (YAMLConfigurationProvider) provider,
				recursive);
	}

}
