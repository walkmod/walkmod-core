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
package org.walkmod.conf.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.walkmod.conf.entities.impl.ParserConfigImpl;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;

import com.fasterxml.jackson.databind.JsonNode;

public class JSONConfigParser {

	public ReaderConfig getReader(JsonNode reader) {
		ReaderConfig model = new ReaderConfig();

		if (reader.has("path")) {
			model.setPath(reader.get("path").asText());
		}
		if (reader.has("type")) {
			model.setType(reader.get("type").asText());
		}
		if (reader.has("includes")) {
			JsonNode includesJson = reader.get("includes");
			model.setIncludes(getFileSet(includesJson));
		}
		if (reader.has("excludes")) {
			JsonNode excludesJson = reader.get("excludes");
			model.setExcludes(getFileSet(excludesJson));
		}
		model.setParameters(getParams(reader));
		return model;
	}

	public WalkerConfig getWalker(JsonNode current) {
		WalkerConfig walkerCfg = new WalkerConfigImpl();
		if (current.has("type")) {
			walkerCfg.setType(current.get("type").asText());
		}
		if (current.has("parser")) {
			ParserConfig parserCfg = new ParserConfigImpl();
			JsonNode parserNode = current.get("parser");
			parserCfg.setType(parserNode.get("type").asText());
			parserCfg.setParameters(getParams(parserNode));
			walkerCfg.setParserConfig(parserCfg);
		}
		walkerCfg.setTransformations(getTransformationCfgs(current));

		if (current.has("root-namespace")) {
			walkerCfg.setRootNamespace(current.get("root-namespace").asText());
		}
		walkerCfg.setParams(getParams(current));

		return walkerCfg;
	}

	public List<TransformationConfig> getTransformationCfgs(JsonNode current) {
		if (current.has("transformations")) {
			List<TransformationConfig> transList = new LinkedList<TransformationConfig>();
			Iterator<JsonNode> itTrans = current.get("transformations").iterator();

			while (itTrans.hasNext()) {
				JsonNode item = itTrans.next();
				TransformationConfig trans = new TransformationConfigImpl();
				if (item.has("type")) {
					trans.setType(item.get("type").asText());
				}
				if (item.has("name")) {
					trans.setName(item.get("name").asText());
				}
				if (item.has("merge-policy")) {
					trans.setMergePolicy(item.get("merge-policy").asText());
				}
				if (item.has("isMergeable")) {
					trans.isMergeable(item.get("isMergeable").asBoolean());
				}

				trans.setParameters(getParams(item));
				transList.add(trans);
			}
			return transList;

		}
		return null;
	}

	public WriterConfig getWriter(JsonNode writer) {
		WriterConfig model = new WriterConfigImpl();

		if (writer.has("path")) {
			model.setPath(writer.get("path").asText());
		}
		if (writer.has("type")) {
			model.setType(writer.get("type").asText());
		}
		if (writer.has("includes")) {
			JsonNode includesJson = writer.get("includes");
			model.setIncludes(getFileSet(includesJson));
		}
		if (writer.has("excludes")) {
			JsonNode excludesJson = writer.get("excludes");
			model.setExcludes(getFileSet(excludesJson));
		}
		model.setParams(getParams(writer));
		return model;
	}

	public String[] getFileSet(JsonNode parent) {
		String[] includes = new String[parent.size()];
		Iterator<JsonNode> includesIt = parent.iterator();
		int j = 0;
		while (includesIt.hasNext()) {
			JsonNode item = includesIt.next();
			includes[j] = item.asText();
			j++;
		}
		return includes;
	}

	public Map<String, Object> getParams(JsonNode next) {
		if (next.has("params")) {
			Iterator<Entry<String, JsonNode>> it2 = next.get("params").fields();
			Map<String, Object> params = new HashMap<String, Object>();
			while (it2.hasNext()) {
				Entry<String, JsonNode> param = it2.next();
				JsonNode value = param.getValue();
				if (value.isTextual()) {
					params.put(param.getKey(), value.asText());
				} else if (value.isInt()) {
					params.put(param.getKey(), value.asInt());
				} else if (value.isBoolean()) {
					params.put(param.getKey(), value.asBoolean());
				} else if (value.isDouble() || value.isFloat() || value.isBigDecimal()) {
					params.put(param.getKey(), value.asDouble());
				} else if (value.isLong() || value.isBigInteger()) {
					params.put(param.getKey(), value.asLong());
				} else {
					params.put(param.getKey(), value);
				}
				params.put(param.getKey(), param.getValue().asText());
			}
			return params;

		}
		return null;
	}

}
