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
import java.util.Iterator;
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
		System.out.println("version 1.0 - March 2014 -");
		System.out.print("----------------------------------------");
		System.out.println("----------------------------------------");
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0 || "--help".equals(args[0])) {
			
			if(args== null || args.length == 0){
				printHeader();
				log.error("You must specify at least one goal to apply code transformations.");
			}
			System.out
					.println("The following list ilustrates some commonly used build commands.");
			System.out.println("walkmod install");
			System.out
					.println("        Downloads and installs a walkmod plugin");
			System.out.println("walkmod apply [chain]");
			System.out
					.println("        Upgrade your code to apply your development conventions");
			System.out.println("walkmod check [chain]");
			System.out
					.println("        Checks and shows witch classes must be reviewed");
			System.out
					.println("Please, see http://www.walkmod.com for more information.");
			if(args== null || args.length == 0){
				System.out.println("Use \"walkmod --help\" to show general usage information about WalkMod command's line");
			}
			// TODO: backup, restore, search
		} else {

			List<String> paramsList = new LinkedList<String>(
					Arrays.asList(args));

			boolean offline = paramsList.remove("--offline");
			boolean showException = paramsList.remove("-e");

			if (paramsList.contains("--version")) {
				System.out.println("Walkmod version \"1.0\"");
				System.out.println("Java version: "
						+ System.getProperty("java.version"));
				System.out.println("Java Home: "
						+ System.getProperty("java.home"));
				System.out.println("OS: " + System.getProperty("os.name")
						+ ", Vesion: " + System.getProperty("os.version"));
			} else if (paramsList.contains("apply")) {
				paramsList.remove("apply");
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

					if (paramsList.size() > 0) {
						executeChainAdapter(apf, config, paramsList.get(0),
								showException);
					} else {
						executeAllChains(apf, config, showException);
					}
				} else {
					log.error(cfg.getAbsolutePath()
							+ " does not exist. The root directory of your project must contain a walkmod.xml");
				}
			} else if (paramsList.contains("check")) {
				paramsList.remove("check");
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
					if (paramsList.size() > 0) {
						executeChainAdapter(apf, config, paramsList.get(0),
								showException);
					} else {
						executeAllChains(apf, config, showException);
					}
				} else {
					log.error(cfg.getAbsolutePath()
							+ " does not exist. The root directory of your project must contain a walkmod.xml");
				}
			} else if (paramsList.contains("install")) {
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
						if (showException) {
							log.error("Plugin installations fails", e);
						} else {
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
		Iterator<ChainConfig> it = tcgfs.iterator();
		int pos = 1;
		while (it.hasNext()) {
			ChainConfig tcfg = it.next();
			
			if(tcgfs.size() > 1){
				String label = "";
				if(tcfg.getName() != null && !tcfg.getName().startsWith("chain_")){
					label = "["+tcfg.getName()+"]("+pos+"/"+tcgfs.size()+") ";
				}
				else{
					label="("+pos+"/"+tcgfs.size()+")";
				}
				log.info("TRANSFORMATION CHAIN "+label+" STARTS");
			}
			try {
				ChainAdapter ap = apf.createChainProxy(conf, tcfg.getName());
				ap.execute();
				num += ap.getWalkerAdapter().getWalker().getNumModifications();
				pos++;
				if(it.hasNext()){
					System.out
					.print("----------------------------------------");
				}
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
				if (printException) {
					log.error("TRANSFORMATION CHAIN (" + tcfg.getName()
							+ ") FAILS", e);
				} else {
					log.error("TRANSFORMATION CHAIN ("
							+ tcfg.getName()
							+ ") FAILS. Execute walkmod with -e to see the error details.");
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
				if (printException) {
					log.error("TRANSFORMATION CHAIN (" + name + ") FAILS", e);
				} else {
					log.error("TRANSFORMATION CHAIN ("
							+ name
							+ ") FAILS. Execute walkmod with -e to see the error details.");
				}
				return;
			}
		}
	}
}
