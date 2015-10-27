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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.impl.PluginConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Prints the components of a walkmod plugin")
public class InspectCommand implements Command {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	@Parameter(description = "The plugin id to inspect (e.g imports-cleaner)")
	private List<String> pluginId;
	
	@Parameter(names = "--offline", description = "Resolves the walkmod plugins and their dependencies in offline mode")
	private boolean offline = false;

	public InspectCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("inspect");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().offline(offline));
			List<BeanDefinition> beans = facade.inspectPlugin(new PluginConfigImpl(pluginId.get(0)));
			if (beans != null) {
				String line = "";

				for (int i = 0; i < 2 + 53 + 33 + 52; i++) {
					line = line + "-";
				}
				System.out.println(line);
				System.out.printf("| %50s | %30s | %50s |%n", StringUtils.center("TYPE NAME (ID)", 50),
						StringUtils.center("CATEGORY", 30), StringUtils.center("DESCRIPTION", 50));
				System.out.println(line);

				for (BeanDefinition bean : beans) {
					System.out.printf("| %50s | %30s | %50s |%n", "", "", "");

					System.out.printf("| %50s | %30s | %50s |%n", StringUtils.center(bean.getType(), 50),
							StringUtils.center(bean.getCategory(), 30), StringUtils.center(bean.getDescription(), 50));

				}
				System.out.printf("| %50s | %30s | %50s |%n", "", "", "");
				System.out.println(line);
			}
		}
	}

}
