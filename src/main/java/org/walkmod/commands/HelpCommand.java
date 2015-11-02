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

import java.util.Map;
import java.util.Set;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

@Parameters(hidden = true, separators = "=", commandDescription = "Shows the command line tool commands")
public class HelpCommand implements Command {
	private JCommander command;

	public HelpCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		Map<String, JCommander> commandList = command.getCommands();
		Set<String> goals = commandList.keySet();
		System.out.println("Usage: walkmod COMMAND [arg...]");
		System.out.println("       walkmod [ -h | --help | -v | --version ]");
		System.out.println();
		System.out.println("Options:");
		for (String goal : goals) {
			if (goal.startsWith("--")) {
				System.out.printf("  %-13.40s  %-40.100s%n", goal, command.getCommandDescription(goal));
			}
		}
		System.out.println();
		System.out.println("Commands:");
		for (String goal : goals) {
			if (!goal.startsWith("--")) {
				System.out.printf("  %-13.40s  %-40.100s%n", goal, command.getCommandDescription(goal));
			}
		}
		System.out.println();
		System.out.println("Run 'walkmod COMMAND --help' for more information on a command.");
		// command.usage();
	}

}
