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

import java.util.Iterator;
import java.util.List;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RemoveModulesYMLAction extends AbstractYMLConfigurationAction {

	private List<String> modules;

	public RemoveModulesYMLAction(List<String> modules, YAMLConfigurationProvider provider) {
		super(provider, false);
		this.modules = modules;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		if (node.has("modules")) {
			JsonNode aux = node.get("modules");
			ObjectMapper mapper = provider.getObjectMapper();
			if (aux.isArray()) {
				ArrayNode modulesList = (ArrayNode) node.get("modules");
				Iterator<JsonNode> it = modulesList.iterator();
				ArrayNode newModulesList = new ArrayNode(mapper.getNodeFactory());
				while (it.hasNext()) {
					JsonNode next = it.next();
					if (next.isTextual()) {
						String text = next.asText();
						if (!modules.contains(text)) {
							newModulesList.add(text);
						}
					}
				}
				ObjectNode oNode = (ObjectNode) node;
				if (newModulesList.size() > 0) {
					oNode.set("modules", newModulesList);
				} else {
					oNode.remove("modules");
				}
				provider.write(node);
			}
		}
	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveModulesYMLAction(modules, (YAMLConfigurationProvider) provider);
	}

}
