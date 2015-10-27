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

import java.util.LinkedList;
import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.PluginConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Adds plugin to the configuration file.")
public class AddPluginCommand implements Command {

	@Parameter(description = "List of plugin identifiers separated by spaces.", required = true)
	private List<String> plugins = null;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	public AddPluginCommand(JCommander command) {
		this.command = command;
	}

	public AddPluginCommand(List<String> plugins) {
		this.plugins = plugins;
	}

	public List<PluginConfig> build() {
		List<PluginConfig> result = new LinkedList<PluginConfig>();
		for (String plugin : plugins) {
			PluginConfig pluginConfig = new PluginConfigImpl(plugin);

			result.add(pluginConfig);

		}
		return result;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("add-plugin");
		} else {

			List<PluginConfig> list = build();
			for (PluginConfig pc : list) {
				WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
				facade.addPluginConfig(pc);
			}

		}
	}

}
