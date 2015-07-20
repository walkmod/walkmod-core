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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Walkmod shell
 * 
 * @author Raquel Pau
 *
 */
public class WalkModDispatcher {

	private static Logger log = Logger.getLogger(WalkModDispatcher.class);

	public static void printHeader() {
		log.info("Java version: " + System.getProperty("java.version"));
		log.info("Java Home: " + System.getProperty("java.home"));
		log.info("OS: " + System.getProperty("os.name") + ", Vesion: "
				+ System.getProperty("os.version"));
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
		System.out.println("version 1.2 - July 2015 -");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0 || "--help".equals(args[0])) {

			if (args == null || args.length == 0) {
				printHeader();
				log.error("You must specify at least one goal to apply code transformations.");
			}
			System.out
					.println("The following list ilustrates some commonly used Walkmod commands.");
			System.out.println("walkmod install");
			System.out
					.println("        Downloads and installs a walkmod plugin");
			System.out.println("walkmod apply [chain] [-i|--includes <path>] [-x|--excludes <path>]");
			System.out
					.println("        Upgrades your code to apply your development conventions");
			System.out.println("walkmod check [chain] [-i|--includes <path>] [-x|--excludes <path>]");
			System.out
					.println("        Checks and shows witch classes must be reviewed");
			System.out
					.println("Please, see http://www.walkmod.com for more information.");
			if (args == null || args.length == 0) {
				System.out
						.println("Use \"walkmod --help\" to show general usage information about WalkMod command's line");
			}
			// TODO: backup, restore, search
		} else {

			List<String> paramsList = new ArrayList<String>(Arrays.asList(args));

			boolean offline = false;
			boolean showException = false;
			ArrayList<String> includes = new ArrayList<String>();
			ArrayList<String> excludes = new ArrayList<String>();

			Iterator<String> it = paramsList.iterator();
			int pos = 0;
			while (it.hasNext()) {
				String elem = it.next();
				if ("--offline".equals(elem)) {
					it.remove();
					offline = true;
					pos++;
				} else if ("-e".equals(elem)) {
					it.remove();
					showException = true;
					pos++;
				} else if ("-i".equals(elem) || "--includes".equals(elem)) {
					it.remove();
					boolean finish = pos == paramsList.size()
							|| paramsList.get(pos).startsWith("-");
					while (!finish) {
						String fileName = it.next();
						File file = new File(fileName);
						if(file.exists()){
							fileName = file.getAbsolutePath();
							includes.add(fileName);
						}
						it.remove();
						finish = pos == paramsList.size()
								|| paramsList.get(pos).startsWith("-");

					}
				} else if ("-x".equals(elem) || "--excludes".equals(elem)) {
					it.remove();
					boolean finish = pos == paramsList.size()
							|| paramsList.get(pos).startsWith("-");
					while (!finish) {
						String fileName = it.next();
						File file = new File(fileName);
						if(file.exists()){
							fileName = file.getAbsolutePath();
							excludes.add(fileName);
						}
						it.remove();
						finish = pos == paramsList.size()
								|| paramsList.get(pos).startsWith("-");
					}
				} else {
					pos++;
				}

			}
			String[] includesArray = null;
			String[] excludesArray = null;
			
			if (!includes.isEmpty()) {
				includesArray = new String[includes.size()];
				includes.toArray(includesArray);
			}
			if(!excludes.isEmpty()){
				excludesArray = new String[excludes.size()];
				excludes.toArray(excludesArray);
			}

			

			WalkModFacade facade = new WalkModFacade(offline, true,
					showException, includesArray, excludesArray);

			if (paramsList.contains("--version")) {
				System.out.println("Walkmod version \"1.2\"");
				System.out.println("Java version: "
						+ System.getProperty("java.version"));
				System.out.println("Java Home: "
						+ System.getProperty("java.home"));
				System.out.println("OS: " + System.getProperty("os.name")
						+ ", Vesion: " + System.getProperty("os.version"));
			} else if (paramsList.contains("apply")) {
				paramsList.remove("apply");
				printHeader();
				String[] params = new String[paramsList.size()];
				facade.apply(paramsList.toArray(params));

			} else if (paramsList.contains("check")) {
				paramsList.remove("check");
				printHeader();
				String[] params = new String[paramsList.size()];
				facade.check(paramsList.toArray(params));

			} else if (paramsList.contains("install")) {
				printHeader();
				facade.install();
			}
		}
	}

}
