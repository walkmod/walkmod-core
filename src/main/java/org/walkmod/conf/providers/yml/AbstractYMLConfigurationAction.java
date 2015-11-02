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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.providers.ConfigurationAction;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public abstract class AbstractYMLConfigurationAction implements ConfigurationAction {

	protected YAMLConfigurationProvider provider;
	private boolean recursive;

	public AbstractYMLConfigurationAction(YAMLConfigurationProvider provider, boolean recursive) {

		this.provider = provider;
		this.recursive = recursive;
	}

	@Override
	public void execute() throws Exception {

		JsonNode node = provider.getRootNode();
		boolean isMultiModule = node.has("modules");
		if (recursive && isMultiModule) {

			JsonNode aux = node.get("modules");
			if (aux.isArray()) {
				ArrayNode modules = (ArrayNode) aux;
				int max = modules.size();
				for (int i = 0; i < max; i++) {
					JsonNode module = modules.get(i);
					if (module.isTextual()) {
						String moduleDir = module.asText();

						try {
							File auxFile = new File(provider.getFileName()).getCanonicalFile().getParentFile();
							YAMLConfigurationProvider child = new YAMLConfigurationProvider(auxFile.getAbsolutePath()
									+ File.separator + moduleDir + File.separator + "walkmod.yml");
							child.createConfig();

							AbstractYMLConfigurationAction childAction = clone(child, recursive);
							childAction.execute();

						} catch (IOException e) {
							throw new TransformerException(e);
						}

					}
				}
			}

		} else {
			doAction(node);
		}
	}

	public void populateParams(ObjectNode root, Map<String, Object> params) {
		ObjectNode paramsNode = new ObjectNode(provider.getObjectMapper().getNodeFactory());
		root.set("params", paramsNode);

		Set<String> keys = params.keySet();
		for (String key : keys) {
			paramsNode.set(key, new TextNode(params.get(key).toString()));
		}
	}

	public void populateWriterReader(ObjectNode root, String path, String type, String[] includes, String[] excludes,
			Map<String, Object> params) {
		if (path != null && !"".equals(path)) {
			root.set("path", new TextNode(path));
		}

		if (type != null) {
			root.set("type", new TextNode(type));
		}

		if (includes != null && includes.length > 0) {
			ArrayNode includesNode = new ArrayNode(provider.getObjectMapper().getNodeFactory());
			for (int i = 0; i < includes.length; i++) {
				includesNode.add(new TextNode(includes[i]));
			}
			root.set("includes", includesNode);
		}

		if (excludes != null && excludes.length > 0) {
			ArrayNode excludesNode = new ArrayNode(provider.getObjectMapper().getNodeFactory());
			for (int i = 0; i < excludes.length; i++) {
				excludesNode.add(new TextNode(excludes[i]));
			}
			root.set("excludes", excludesNode);
		}
		if (params != null && !params.isEmpty()) {
			populateParams(root, params);
		}

	}

	public void createTransformation(ObjectNode transformationNode, TransformationConfig transCfg) {

		String name = transCfg.getName();
		if (name != null) {
			transformationNode.set("name", new TextNode(name));
		}
		String typeName = transCfg.getType();
		if (typeName != null) {
			transformationNode.set("type", new TextNode(typeName));
		}
		String mergePolicy = transCfg.getMergePolicy();
		if (mergePolicy != null) {
			transformationNode.set("merge-policy", new TextNode(mergePolicy));
		}
		if (transCfg.isMergeable()) {
			transformationNode.set("isMergeable", BooleanNode.TRUE);
		}
		Map<String, Object> params = transCfg.getParameters();
		if (params != null && !params.isEmpty()) {
			populateParams(transformationNode, params);
		}
	}

	public abstract void doAction(JsonNode node) throws Exception;

	public abstract AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive);

}
