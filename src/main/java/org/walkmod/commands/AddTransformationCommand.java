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
package org.walkmod.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;

@Parameters(separators = "=", commandDescription = "Adds a code transformation/convention.")
public class AddTransformationCommand implements Command {

	@Parameter(names = "--params", description = "Transformation parameters as JSON object", converter = JSONConverter.class)
	private JsonNode params = null;

	@Parameter(names = "--merge-policy", description = "Merge policy to apply after executing the transformation")
	private String mergePolicy = null;

	@Parameter(names = { "--chain" }, description = "The chain identifier", required = true)
	private String chain = null;

	@Parameter(names = { "--isMergeabe" }, description = "Sets if the changes made by the transformation requires to be merged")
	private boolean isMergeable = false;

	@Parameter(description = "The transformatio type identifier", required = true)
	private String type = null;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;

	public AddTransformationCommand(String type, String chain, boolean isMergeable, String mergePolicy, JsonNode params) {
		this.type = type;
		this.chain = chain;
		this.isMergeable = isMergeable;
		this.mergePolicy = mergePolicy;
		this.params = params;
	}

	public AddTransformationCommand(JCommander jcommander) {
		this.jcommander = jcommander;
	}

	public TransformationConfig buildTransformationCfg() {
		TransformationConfig tconfig = new TransformationConfigImpl();
		tconfig.setType(type);
		tconfig.isMergeable(isMergeable);
		tconfig.setMergePolicy(mergePolicy);

		if (params != null) {
			Map<String, Object> transformationParams = new HashMap<String, Object>();

			Iterator<Entry<String, JsonNode>> it = params.fields();
			while (it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				JsonNode value = entry.getValue();
				if (value.isTextual()) {
					transformationParams.put(entry.getKey(), value.asText());
				} else if (value.isInt()) {
					transformationParams.put(entry.getKey(), value.asInt());
				} else if (value.isBoolean()) {
					transformationParams.put(entry.getKey(), value.asBoolean());
				} else if (value.isDouble() || value.isFloat() || value.isBigDecimal()) {
					transformationParams.put(entry.getKey(), value.asDouble());
				} else if (value.isLong() || value.isBigInteger()) {
					transformationParams.put(entry.getKey(), value.asLong());
				} else {
					transformationParams.put(entry.getKey(), value);
				}
			}

			tconfig.setParameters(transformationParams);
		}

		return tconfig;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jcommander.usage("add");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addTransformationConfig(chain, buildTransformationCfg());
		}
	}

}
