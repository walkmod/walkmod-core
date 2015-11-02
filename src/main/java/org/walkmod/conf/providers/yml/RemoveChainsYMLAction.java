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
import java.util.List;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RemoveChainsYMLAction extends AbstractYMLConfigurationAction {

	private List<String> chains;

	public RemoveChainsYMLAction(List<String> chains, YAMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.chains = chains;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		HashSet<String> chainsSet = new HashSet<String>(chains);
		ArrayNode chainsList = null;
		ObjectMapper mapper = provider.getObjectMapper();

		if (node.has("chains")) {
			JsonNode aux = node.get("chains");
			if (aux.isArray()) {
				chainsList = (ArrayNode) node.get("chains");
				Iterator<JsonNode> it = chainsList.iterator();
				ArrayNode newChainsList = new ArrayNode(mapper.getNodeFactory());
				while (it.hasNext()) {
					JsonNode next = it.next();
					if (next.isObject()) {
						String type = next.get("name").asText();
						if (!chainsSet.contains(type)) {
							newChainsList.add(next);
						}
					}
				}
				ObjectNode oNode = (ObjectNode) node;
				if (newChainsList.size() > 0) {
					oNode.set("chains", newChainsList);
				} else {
					oNode.remove("chains");
				}
				provider.write(node);
			}
		} else if (node.has("transformations") && chainsSet.contains("default")) {
			ObjectNode oNode = (ObjectNode) node;
			oNode.remove("transformations");
			provider.write(node);
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveChainsYMLAction(chains, (YAMLConfigurationProvider) provider, recursive);
	}

}
