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

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.JSONConfigParser;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.impl.ProviderConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;

@Parameters(separators = "=", commandDescription = "Adds a configuration provider.e.g maven or gradle to calculate the project classpath")
public class AddCfgProviderCommand implements Command {

	@Parameter(description = "The configuration provider type identifier", required = true)
	private String type;

	@Parameter(names = "--params", description = "Transformation parameters as JSON object", converter = JSONConverter.class)
	private JsonNode params;

	private JCommander command;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	public AddCfgProviderCommand(JCommander command) {
		this.command = command;
	}

	public AddCfgProviderCommand(String type, JsonNode params) {
		this.type = type;
		this.params = params;
	}

	public ProviderConfig build() throws Exception {
		ProviderConfig prov = new ProviderConfigImpl();

		prov.setType(type);

		if (params != null) {
			JSONConfigParser parser = new JSONConfigParser();

			prov.setParameters(parser.getParams(params));
		}

		return prov;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("add-provider");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addProviderConfig(build());
		}

	}

}
