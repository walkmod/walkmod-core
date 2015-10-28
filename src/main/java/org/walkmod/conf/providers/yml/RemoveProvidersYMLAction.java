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

import javax.xml.transform.TransformerException;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RemoveProvidersYMLAction extends AbstractYMLConfigurationAction {

	private List<String> providers;

	public RemoveProvidersYMLAction(List<String> providers, YAMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.providers = providers;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		HashSet<String> providerSet = new HashSet<String>();
		for (String elem : providers) {
			String[] partsType = elem.split(":");
			if (partsType.length == 1) {
				elem = "org.walkmod:walkmod-" + elem + "-plugin:" + elem;
			}
			if (partsType.length != 3 && partsType.length != 1) {
				throw new TransformerException("Invalid conf-provider");
			}
			providerSet.add(elem);
		}
		if (node.has("conf-providers")) {
			JsonNode aux = node.get("conf-providers");
			if (aux.isArray()) {
				ArrayNode providersList = (ArrayNode) node.get("conf-providers");
				Iterator<JsonNode> it = providersList.iterator();
				ArrayNode newProvidersList = new ArrayNode(provider.getObjectMapper().getNodeFactory());
				while (it.hasNext()) {
					JsonNode next = it.next();
					if (next.isObject()) {
						String type = next.get("type").asText();
						String[] parts = type.split(":");
						if (parts.length == 1) {
							type = "org.walkmod:walkmod-" + type + "-plugin:" + type;
						} else if (parts.length != 3) {
							throw new TransformerException("Invalid conf-provider");
						}
						if (!providerSet.contains(type)) {
							newProvidersList.add(next);
						}
					}
				}
				ObjectNode oNode = (ObjectNode) node;
				if (newProvidersList.size() > 0) {
					oNode.set("conf-providers", newProvidersList);
				} else {
					oNode.remove("conf-providers");
				}
				provider.write(node);
			}
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveProvidersYMLAction(providers, (YAMLConfigurationProvider) provider, recursive);
	}

}
