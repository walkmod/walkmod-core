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
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.PluginConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Adds plugin in the walkmod configuration file.")
public class AddPluginCommand implements Command {

	@Parameter(names = { "--groupId", "-g" }, description = "Plugin groupId", required = true)
	private String groupId = "";

	@Parameter(names = { "--artifactId", "-a" }, description = "Plugin artifactId", required = true)
	private String artifactId = "";

	@Parameter(names = { "--version", "-v" }, description = "Plugin version", required = true)
	private String version = "";

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	public AddPluginCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("add-plugin");
		} else {
			PluginConfig pluginConfig = new PluginConfigImpl();
			pluginConfig.setGroupId(groupId);
			pluginConfig.setArtifactId(artifactId);
			pluginConfig.setVersion(version);
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addPluginConfig(pluginConfig);
		}
	}

}
