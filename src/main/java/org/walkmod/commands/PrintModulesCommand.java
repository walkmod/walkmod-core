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

import org.apache.log4j.Logger;
import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.Configuration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.V2_AsciiTable;

@Parameters(separators = "=", commandDescription = "Shows the list of modules.")
public class PrintModulesCommand implements Command, AsciiTableAware {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	private V2_AsciiTable at = null;
	
	private static Logger log = Logger.getLogger(PrintModulesCommand.class);

	public PrintModulesCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("modules");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			Configuration cfg = facade.getConfiguration();
			if(cfg == null){
				log.error("Sorry, the current directory does not contain a walkmod configuration file or it is invalid.");
			}
			at = new V2_AsciiTable();
			at.addRule();
			at.addRow("MODULE");
			at.addRule();

			if (cfg != null) {
				List<String> modules = cfg.getModules();
				if (modules != null) {
					for (String module : modules) {
						at.addRow(module);
					}
				}
			}
			at.addRule();

		}
	}

	@Override
	public V2_AsciiTable getTable() {

		return at;
	}

}
