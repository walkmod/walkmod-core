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

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class RemoveProviderCommand implements Command{

	@Parameter(description = "List of provider identifiers separated by spaces.", required = true)
	private List<String> providers = null;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	public RemoveProviderCommand(JCommander command) {
		this.command = command;
	}
	
	
	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("rm-provider");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.removeProviders(providers);
		}
	}

}
