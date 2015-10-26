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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.walkmod.commands.AddCfgProviderCommand;
import org.walkmod.commands.AddModuleCommand;
import org.walkmod.commands.AddPluginCommand;
import org.walkmod.commands.AddTransformationCommand;
import org.walkmod.commands.ApplyCommand;
import org.walkmod.commands.CheckCommand;
import org.walkmod.commands.Command;
import org.walkmod.commands.HelpCommand;
import org.walkmod.commands.InitCommand;
import org.walkmod.commands.InstallCommand;
import org.walkmod.commands.PrintPluginsCommand;
import org.walkmod.commands.RemoveModuleCommand;
import org.walkmod.commands.RemovePluginCommand;
import org.walkmod.commands.RemoveTransformationCommand;
import org.walkmod.commands.SetReaderCommand;
import org.walkmod.commands.SetWriterCommand;
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

	private Map<String, Command> commands = new LinkedHashMap<String, Command>();

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
		commands.put("add", new AddTransformationCommand(jcommander));
		commands.put("add-module", new AddModuleCommand(jcommander));
		commands.put("add-plugin", new AddPluginCommand(jcommander));
		commands.put("add-provider", new AddCfgProviderCommand(jcommander));
		commands.put("apply", new ApplyCommand(jcommander));
		commands.put("check", new CheckCommand(jcommander));
		commands.put("init", new InitCommand(jcommander));
		commands.put("install", new InstallCommand(jcommander));
		commands.put("rm", new RemoveTransformationCommand(jcommander));
		commands.put("rm-module", new RemoveModuleCommand(jcommander));
		commands.put("rm-plugin", new RemovePluginCommand(jcommander));
		commands.put("set-reader", new SetReaderCommand(jcommander));
		commands.put("set-writer", new SetWriterCommand(jcommander));
		commands.put("plugins", new PrintPluginsCommand(jcommander));
		commands.put("--version", new VersionCommand());
		commands.put("--help", new HelpCommand(jcommander));

		Set<String> keys = commands.keySet();
		
		
		for (String key : keys) {
			if (!key.startsWith("--")) {
				jcommander.addCommand(key, commands.get(key));
			} else {
				jcommander.addCommand(key, commands.get(key), "-" + key.charAt(2));
			}
			JCommander aux = jcommander.getCommands().get(key);

			aux.setProgramName("walkmod " + key);
			aux.setAcceptUnknownOptions(false);

		}

		if (args == null || args.length == 0) {
			printHeader();
			new HelpCommand(jcommander).execute();
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
			commands.get(command.substring("walkmod ".length(), command.length())).execute();

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
