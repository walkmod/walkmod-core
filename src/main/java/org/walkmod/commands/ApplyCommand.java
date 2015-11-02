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

import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Upgrades your code to apply all your code transformations.")
public class ApplyCommand extends AbstractChainCommand implements Command {

	private JCommander command;

	public ApplyCommand(JCommander command) {
		this.command = command;
	}
	
	@Override
	public void execute() throws Exception {
		if (isHelpNeeded()) {
			command.usage("apply");
		} else {
			WalkModFacade facade = new WalkModFacade(buildOptions());
			String[] params = new String[getParameters().size()];
			if (params.length == 0) {
				facade.apply();
			} else {
				facade.apply(getParameters().toArray(params));
			}
		}
	}
}
