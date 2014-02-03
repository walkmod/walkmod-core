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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.walkmod.conf.ConfigurationManager;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.providers.IvyConfigurationProvider;
import org.walkmod.impl.DefaultChainAdapterFactory;
import org.walkmod.writers.VisitorMessagesWriter;

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
		System.out
				.println("An open source tool for apply code conventions into your project");
		System.out.println("version 1.0 - May 2013 -");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
	}

	public static void main(String[] args){
		if (args == null || args.length == 0) {
			printHeader();
			log.error("You must specify at least one goal to apply code transformations.");
			System.out
					.println("The following list ilustrates some commonly used build commands.");
			System.out.println("walkmod install");
			System.out
					.println("        Downloads and installes a walkmod plugin");
			System.out.println("walkmod apply");
			System.out
					.println("        Upgrade your code to apply your development conventions");
			System.out.println("walkmod check");
			System.out
					.println("        Checks and shows witch classes must be reviewed");
			System.out
					.println("Please, see URL for a complete description of available plugins");
			System.out
					.println("Use \"walkmod --help\" to show general usage information about WalkMod command's line");
			//TODO: backup, restore, search
		} else {
			
			List<String> paramsList;
			if(args.length > 1){
				paramsList = Arrays.asList(args).subList(1, args.length-1);
			}
			else{
				paramsList = new LinkedList<String>();
			}

			boolean offline = args.length > 1
					&& args[1].equals("--offline");
			boolean showException = paramsList.contains("-e");
			
			if ("--version".equals(args[0])) {
				System.out.println("Walkmod version \"1.0\"");
				System.out.println("Java version: "
						+ System.getProperty("java.version"));
				System.out.println("Java Home: "
						+ System.getProperty("java.home"));
				System.out.println("OS: " + System.getProperty("os.name")
						+ ", Vesion: " + System.getProperty("os.version"));
			} else if ("apply".equals(args[0])) {
				printHeader();
				File cfg = new File("walkmod.xml");
				if (cfg.exists()) {
					log.info(cfg.getAbsoluteFile() + " [ok]");
					
					ConfigurationProvider cp = new IvyConfigurationProvider(
							offline);
					ConfigurationManager cfgManager = new ConfigurationManager(
							cfg, cp);
					Configuration config = cfgManager.getConfiguration();
					ChainAdapterFactory apf = new DefaultChainAdapterFactory();
					if (args.length > 1) {
						if (offline) {
							if (args.length > 2) {
								executeChainAdapter(apf, config, args[2], showException);
							} else {
								executeAllChains(apf, config, showException);
							}
						}
						executeChainAdapter(apf, config, args[1], showException);
					} else {
						executeAllChains(apf, config, showException);
					}
				} else {
					log.error(cfg.getAbsolutePath()
							+ " does not exist. The root directory of your project must contain a walkmod.xml");
				}
			} else if ("check".equals(args[0])) {
				printHeader();
				File cfg = new File("walkmod.xml");
				if (cfg.exists()) {
					ConfigurationProvider cp = new IvyConfigurationProvider(
							offline);
					ConfigurationManager cfgManager = new ConfigurationManager(
							cfg, cp);
					Configuration config = cfgManager.getConfiguration();
					ChainAdapterFactory apf = new DefaultChainAdapterFactory();
					Collection<ChainConfig> tcgfs = config.getChainConfigs();
					for (ChainConfig tcfg : tcgfs) {
						tcfg.getWriterConfig().setType(
								VisitorMessagesWriter.class.getName());
						tcfg.getWriterConfig().setModelWriter(
								new VisitorMessagesWriter());
					}
					log.info(cfg.getAbsoluteFile() + " [ok]");
					if (args.length > 1) {
						if (offline) {
							if (args.length > 2) {
								executeChainAdapter(apf, config, args[2],showException);
							} else {
								executeAllChains(apf, config, showException);
							}
						}
						executeChainAdapter(apf, config, args[1], showException);
					} else {
						executeAllChains(apf, config, showException);
					}
				} else {
					log.error(cfg.getAbsolutePath()
							+ " does not exist. The root directory of your project must contain a walkmod.xml");
				}
			} else if ("install".equals(args[0])) {
				printHeader();
				File cfg = new File("walkmod.xml");
				if (cfg.exists()) {
					ConfigurationProvider cp = new IvyConfigurationProvider();
					log.info("** THE PLUGIN INSTALLATION STARTS **");
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					long startTime = System.currentTimeMillis();
					long endTime = startTime;
					DecimalFormat myFormatter = new DecimalFormat("###.###");
					DateFormat df = new SimpleDateFormat(
							"EEE, d MMM yyyy HH:mm:ss", Locale.US);
					boolean error = false;
					try {
						ConfigurationManager cfgManager = new ConfigurationManager(
								cfg, cp);
						Configuration cf = cfgManager.getConfiguration();
					} catch (Exception e) {
						error = true;
						endTime = System.currentTimeMillis();
						double time = 0;
						if (endTime > startTime) {
							time = (double) ((double) (endTime - startTime) / (double) 1000);
						}
						String timeMsg = myFormatter.format(time);
						System.out
								.print("----------------------------------------");
						System.out
								.println("----------------------------------------");
						log.info("PLUGIN INSTALLATION FAILS");
						System.out.println();
						System.out
								.print("----------------------------------------");
						System.out
								.println("----------------------------------------");
						log.info("Total time: " + timeMsg + " seconds");
						log.info("Finished at: " + df.format(new Date()));
						log.info("Final memory: "
								+ (Runtime.getRuntime().freeMemory())
								/ 1048576
								+ " M/ "
								+ (Runtime.getRuntime().totalMemory() / 1048576)
								+ " M");
						System.out
								.print("----------------------------------------");
						System.out
								.println("----------------------------------------");
						if(showException){
							log.error("Plugin installations fails", e);
						}
						else{
							log.info("Plugin installations fails. Please, execute walkmod with -e to see the details");
						}
					}
					if (!error) {
						endTime = System.currentTimeMillis();
						double time = 0;
						if (endTime > startTime) {
							time = (double) ((double) (endTime - startTime) / (double) 1000);
						}
						String timeMsg = myFormatter.format(time);
						System.out
								.print("----------------------------------------");
						System.out
								.println("----------------------------------------");
						System.out.println();
						log.info("PLUGIN INSTALLATION COMPLETE");
						System.out
								.print("----------------------------------------");
						System.out
								.println("----------------------------------------");
						log.info("Total time: " + timeMsg + " seconds");
						log.info("Finished at: " + df.format(new Date()));
						log.info("Final memory: "
								+ (Runtime.getRuntime().freeMemory())
								/ 1048576
								+ " M/ "
								+ (Runtime.getRuntime().totalMemory() / 1048576)
								+ " M");
						System.out
								.print("----------------------------------------");
						System.out
								.println("----------------------------------------");
					}
				} else {
					log.error(cfg.getAbsolutePath()
							+ " does not exist. The root directory of your project must contain a walkmod.xml");
				}
			}
		}
	}

	private static void executeAllChains(ChainAdapterFactory apf,
			Configuration conf, boolean printException) {
		Collection<ChainConfig> tcgfs = conf.getChainConfigs();
		log.info("** THE TRANSFORMATIONS CHAINS START **");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
		long startTime = System.currentTimeMillis();
		long endTime = startTime;
		DecimalFormat myFormatter = new DecimalFormat("###.###");
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss",
				Locale.US);
		int num = 0;
		for (ChainConfig tcfg : tcgfs) {
			try {
				ChainAdapter ap = apf.createChainProxy(conf, tcfg.getName());
				ap.execute();
				num += ap.getWalkerAdapter().getWalker().getNumModifications();
			} catch (Throwable e) {
				endTime = System.currentTimeMillis();
				double time = 0;
				if (endTime > startTime) {
					time = (double) ((double) (endTime - startTime) / (double) 1000);
				}
				String timeMsg = myFormatter.format(time);
				if (num != 0) {
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
				}
				log.info("TRANSFORMATION CHAIN FAILS");
				System.out.println();
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
				log.info("Total time: " + timeMsg + " seconds");
				log.info("Finished at: " + df.format(new Date()));
				log.info("Final memory: " + (Runtime.getRuntime().freeMemory())
						/ 1048576 + " M/ "
						+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
				log.info("Please, see the walkmod log file for details");
				if(printException){
					log.error("TRANSFORMATION CHAIN (" +  tcfg.getName() + ") FAILS", e);
				}
				else{
					log.error("TRANSFORMATION CHAIN (" +  tcfg.getName() + ") FAILS. Execute walkmod with -e to see the error details.");
				}
				return;
			}
		}
		endTime = System.currentTimeMillis();
		double time = 0;
		if (endTime > startTime) {
			time = (double) ((double) (endTime - startTime) / (double) 1000);
		}
		String timeMsg = myFormatter.format(time);
		if (num != 0) {
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");
		}
		System.out.println();
		log.info("TRANSFORMATION CHAIN SUCCESS");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
		log.info("Total time: " + timeMsg + " seconds");
		log.info("Finished at: " + df.format(new Date()));
		log.info("Final memory: " + (Runtime.getRuntime().freeMemory())
				/ 1048576 + " M/ "
				+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
		log.info("Total modified files: " + num);
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
	}

	private static void executeChainAdapter(ChainAdapterFactory apf,
			Configuration conf, String name, boolean printException) {
		ChainAdapter ap = apf.createChainProxy(conf, name);
		if (ap == null) {
			log.error("The chain " + name + " is not found");
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");
		} else {
			log.info("** THE TRANSFORMATION CHAIN " + name + " STARTS **");
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");
			long startTime = System.currentTimeMillis();
			long endTime = startTime;
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss",
					Locale.US);
			int num = 0;
			try {
				ap.execute();
				num = ap.getWalkerAdapter().getWalker().getNumModifications();
				endTime = System.currentTimeMillis();
				double time = 0;
				if (endTime > startTime) {
					time = (double) ((double) (endTime - startTime) / (double) 1000);
				}
				String timeMsg = myFormatter.format(time);
				if (num != 0) {
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
				} else {
					if (ap.getWalkerAdapter().getWalker().reportChanges()) {
						log.info("No sources changed");
					}
				}
				System.out.println();
				log.info("TRANSFORMATION CHAIN SUCCESS");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
				log.info("Total time: " + timeMsg + " seconds");
				log.info("Finished at: " + df.format(new Date()));
				log.info("Final memory: " + (Runtime.getRuntime().freeMemory())
						/ 1048576 + " M/ "
						+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
				if (ap.getWalkerAdapter().getWalker().reportChanges()) {
					log.info("Total modified files: " + num);
				}
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			} catch (Throwable e) {
				endTime = System.currentTimeMillis();
				double time = 0;
				if (endTime > startTime) {
					time = (double) ((double) (endTime - startTime) / (double) 1000);
				}
				String timeMsg = myFormatter.format(time);
				if (num != 0) {
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
				}
				log.info("TRANSFORMATION CHAIN FAILS");
				System.out.println();
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
				log.info("Total time: " + timeMsg + " seconds");
				log.info("Finished at: " + df.format(new Date()));
				log.info("Final memory: " + (Runtime.getRuntime().freeMemory())
						/ 1048576 + " M/ "
						+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
				log.info("Please, see the walkmod log file for details");				
				if(printException){
					log.error("TRANSFORMATION CHAIN (" + name + ") FAILS", e);
				}
				else{
					log.error("TRANSFORMATION CHAIN (" + name + ") FAILS. Execute walkmod with -e to see the error details.");
				}
				return;
			}
		}
	}
}
