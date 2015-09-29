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

package org.walkmod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.walkmod.commands.ApplyCommand;
import org.walkmod.commands.CheckCommand;
import org.walkmod.commands.Command;
import org.walkmod.commands.HelpCommand;
import org.walkmod.commands.InitCommand;
import org.walkmod.commands.InstallCommand;
import org.walkmod.commands.PrintPluginsCommand;
import org.walkmod.commands.VersionCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Walkmod shell
 * 
 * @author Raquel Pau
 *
 */
public class WalkModDispatcher {

	private static Logger log = Logger.getLogger(WalkModDispatcher.class);

	private Map<String, Command> commands = new HashMap<String, Command>();

	public WalkModDispatcher() {

	}

	public static void printHeader() {
		log.info("Java version: " + System.getProperty("java.version"));
		log.info("Java Home: " + System.getProperty("java.home"));
		log.info("OS: " + System.getProperty("os.name") + ", Vesion: " + System.getProperty("os.version"));
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
		System.out.print("                    ");
		System.out.print(" _    _       _ _   ___  ___          _ ");
		System.out.println("                    ");
		System.out.print("                    ");
		System.out.print("| |  | |     | | |  |  \\/  |         | |");
		System.out.println("                    ");
		System.out.print("                    ");
		System.out.print("| |  | | __ _| | | _| .  . | ___   __| |");
		System.out.println("                    ");
		System.out.print("                    ");
		System.out.print("| |/\\| |/ _` | | |/ / |\\/| |/ _ \\ / _` |");
		System.out.println("                    ");
		System.out.print("                    ");
		System.out.print("\\  /\\  / (_| | |   <| |  | | (_) | (_| |");
		System.out.println("                    ");
		System.out.print("                    ");
		System.out.print(" \\/  \\/ \\__,_|_|_|\\_\\_|  |_/\\___/ \\__,_|");
		System.out.println("                    ");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
		System.out.println("An open source tool to apply code conventions");
		System.out.println("version 1.4 - October 2015 -");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
	}

	public void execute(JCommander jcommander, String[] args) throws Exception {
		commands.put("apply", new ApplyCommand());
		commands.put("check", new CheckCommand());
		commands.put("install", new InstallCommand());
		commands.put("plugins", new PrintPluginsCommand());
		commands.put("init", new InitCommand());
		commands.put("--version", new VersionCommand());
		commands.put("--help", new HelpCommand(jcommander));

		Set<String> keys = commands.keySet();
		for (String key : keys) {
			jcommander.addCommand(key, commands.get(key));
		}

		if (args == null || args.length == 0) {
			printHeader();
			jcommander.usage();
		} else {
			try {
				jcommander.parse(args);
			} catch (ParameterException e) {
				
				System.out.println(e.getMessage());
				System.out.println("Run walkmod --help to see the accepted parameters");
				return;
			}
			String command = jcommander.getParsedCommand();
			printHeader();
			commands.get(command).execute();

		}
	}

	public static void main(String[] args) throws Exception {

		WalkModDispatcher instance = new WalkModDispatcher();
		JCommander command = new JCommander(instance);

		command.setProgramName("walkmod");
		command.setAcceptUnknownOptions(false);

		instance.execute(command, args);

	}

}
