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
import java.util.Map;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AddProviderConfigYMLAction extends AbstractYMLConfigurationAction {

	private ProviderConfig providerCfg;

	public AddProviderConfigYMLAction(ProviderConfig providerCfg, YAMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.providerCfg = providerCfg;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {

		ObjectMapper mapper = provider.getObjectMapper();

		if (node.has("conf-providers")) {
			JsonNode list = node.get("conf-providers");
			Iterator<JsonNode> it = list.iterator();
			boolean found = false;
			while (it.hasNext() && !found) {
				JsonNode next = it.next();
				found = providerCfg.getType().equals(next.get("type").asText());
			}
			if (!found) {
				if (list.isArray()) {
					ArrayNode aux = (ArrayNode) list;
					ObjectNode prov = new ObjectNode(mapper.getNodeFactory());
					prov.set("type", new TextNode(providerCfg.getType()));
					Map<String, Object> params = providerCfg.getParameters();
					if (params != null && !params.isEmpty()) {
						populateParams(prov, params);
					}
					aux.add(prov);
					provider.write(node);
					return;
				}
			}
		} else {
			ArrayNode aux = new ArrayNode(mapper.getNodeFactory());
			ObjectNode prov = new ObjectNode(mapper.getNodeFactory());
			prov.set("type", new TextNode(providerCfg.getType()));
			Map<String, Object> params = providerCfg.getParameters();
			if (params != null && !params.isEmpty()) {
				populateParams(prov, params);
			}
			aux.add(prov);
			ObjectNode auxNode = (ObjectNode) node;
			auxNode.set("conf-providers", aux);
			provider.write(node);
			return;
		}

		return;

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddProviderConfigYMLAction(providerCfg, (YAMLConfigurationProvider) provider, recursive);
	}

}
