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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Downloads and installs the specified walkmod plugins and all their dependencies.")
public class InstallCommand implements Command {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;
	
	@Parameter(names = { "-e", "--verbose" }, description = "Prints the stacktrace of the produced error during the execution")
	private Boolean showException = false;

	private JCommander command;

	public InstallCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("install");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().printErrors(showException));
			facade.install();
		}
	}

}
