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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AddModulesYMLAction extends AbstractYMLConfigurationAction {

	private List<String> modules;

	public AddModulesYMLAction(List<String> modules, YAMLConfigurationProvider provider) {
		super(provider, false);
		this.modules = modules;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		ArrayNode aux = null;
		HashSet<String> modulesToAdd = new HashSet<String>(modules);
		if (node.has("modules")) {
			JsonNode list = node.get("modules");
			Iterator<JsonNode> it = list.iterator();

			while (it.hasNext()) {
				JsonNode next = it.next();
				modulesToAdd.remove(next.asText().trim());

			}
			if (!modulesToAdd.isEmpty()) {
				if (list.isArray()) {
					aux = (ArrayNode) list;
				}
			}
		} else {
			aux = new ArrayNode(provider.getObjectMapper().getNodeFactory());
		}
		if (!modulesToAdd.isEmpty()) {
			for (String moduleToAdd : modulesToAdd) {
				TextNode prov = new TextNode(moduleToAdd);
				aux.add(prov);
			}
			ObjectNode auxNode = (ObjectNode) node;
			auxNode.set("modules", aux);
			provider.write(node);
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddModulesYMLAction(modules, (YAMLConfigurationProvider) provider);
	}

}
