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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.walkmod.conf.ConfigurationManager;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.providers.IvyConfigurationProvider;
import org.walkmod.exceptions.InvalidConfigurationException;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.impl.DefaultChainAdapterFactory;
import org.walkmod.writers.Summary;
import org.walkmod.writers.VisitorMessagesWriter;

/**
 * Facade to execute walkmod services.
 * 
 * @author Raquel Pau
 *
 */
public class WalkModFacade {

	private static Logger log = Logger.getLogger(WalkModFacade.class);

	private boolean offline = false;

	private boolean verbose = true;

	private boolean printError = false;

	private static final String DEFAULT_WALKMOD_FILE = "walkmod.xml";

	private File cfg;

	/**
	 * Facade constructor
	 * 
	 * @param cfg
	 *            configuration file.
	 * 
	 * @param offline
	 *            if the missing plugins are downloaded.
	 * @param verbose
	 *            if log messages are printed in the System.out
	 * @param printError
	 *            if the exception stack trace is printed in the System.out
	 */
	public WalkModFacade(File cfg, boolean offline, boolean verbose,
			boolean printError) {
		this.cfg = cfg;
		this.offline = offline;
		this.verbose = verbose;
		this.printError = printError;
	}

	public WalkModFacade(String cfg, boolean offline, boolean verbose,
			boolean printError) {
		this.cfg = new File(cfg);
		this.offline = offline;
		this.verbose = verbose;
		this.printError = printError;
	}

	/**
	 * Facade constructor using the walkmod.xml configuration file.
	 * 
	 * @param offline
	 *            if the missing plugins are downloaded.
	 * @param verbose
	 *            if log messages are printed in the System.out
	 * @param printError
	 *            if the exception trace is printed in the System.out
	 */
	public WalkModFacade(boolean offline, boolean verbose, boolean printError) {
		this(new File(DEFAULT_WALKMOD_FILE), offline, verbose, printError);
	}

	/**
	 * Default facade constructor. It uses the walkmod.xml configuration file,
	 * it is not off-line, it is verbose and doesn't print the exceptions' stack
	 * traces.
	 */
	public WalkModFacade() {
		this(false, true, false);
	}

	/**
	 * Applies a list of transformation chains.
	 * 
	 * @param chains
	 *            the list of applied transformation chains.
	 * @throws InvalidConfigurationException
	 *             if the walkmod configuration is invalid and it is working in
	 *             no verbose mode.
	 */
	public List<File> apply(String... chains)
			throws InvalidConfigurationException {
		if (cfg.exists()) {
			if (verbose) {
				log.info(cfg.getAbsoluteFile() + " [ok]");
			}
			ConfigurationProvider cp = new IvyConfigurationProvider(offline);
			ConfigurationManager cfgManager = null;
			Configuration config = null;
			ChainAdapterFactory apf = null;
			try {
				cfgManager = new ConfigurationManager(cfg, cp);
				config = cfgManager.getConfiguration();
				apf = new DefaultChainAdapterFactory();
			} catch (Exception e) {
				if (verbose) {
					if (!printError) {
						log.error(cfg.getAbsolutePath()
								+ " is invalid. Please, execute walkmod with -e to see the details.");
					} else {
						log.error("Invalid configuration", e);
					}
					return null;
				} else {
					throw new InvalidConfigurationException(e);
				}
			}
			Summary.getInstance().clear();
			if (chains == null || chains.length == 0) {
				executeAllChains(apf, config);
			} else {
				for (String chain : chains) {
					executeChainAdapter(apf, config, chain);
				}
			}
		} else {
			if (verbose) {
				log.error(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			} else {
				throw new WalkModException(
						cfg.getAbsolutePath()
								+ " does not exist. The root directory of your project must contain a walkmod.xml");
			}
		}
		return Summary.getInstance().getWrittenFiles();
	}

	/**
	 * Applies a list of transformation chains without updating the source
	 * files.
	 * 
	 * @param chains
	 *            the list of applied transformation chains.
	 * @throws InvalidConfigurationException
	 *             if the walkmod configuration is invalid and it is working in
	 *             no verbose mode.
	 */
	public List<File> check(String... chains)
			throws InvalidConfigurationException {

		if (cfg.exists()) {
			if (verbose) {
				log.info(cfg.getAbsoluteFile() + " [ok]");
			}
			ConfigurationProvider cp = new IvyConfigurationProvider(offline);
			ConfigurationManager cfgManager = null;
			Configuration config = null;
			ChainAdapterFactory apf = null;
			try {
				cfgManager = new ConfigurationManager(cfg, cp);
				config = cfgManager.getConfiguration();
				apf = new DefaultChainAdapterFactory();
			} catch (Exception e) {
				if (verbose) {
					if (!printError) {
						log.error(cfg.getAbsolutePath()
								+ " is invalid. Please, execute walkmod with -e to see the details.");
					} else {
						log.error("Invalid configuration", e);
					}
					return null;
				} else {
					throw new InvalidConfigurationException(e);
				}
			}
			Collection<ChainConfig> tcgfs = config.getChainConfigs();
			for (ChainConfig tcfg : tcgfs) {
				tcfg.getWriterConfig().setType(
						VisitorMessagesWriter.class.getName());
				tcfg.getWriterConfig().setModelWriter(
						new VisitorMessagesWriter());
			}
			Summary.getInstance().clear();
			if (chains == null || chains.length == 0) {
				executeAllChains(apf, config);
			} else {
				for (String chain : chains) {
					executeChainAdapter(apf, config, chain);
				}
			}
		} else {
			if (verbose) {
				log.error(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			} else {
				throw new WalkModException(
						cfg.getAbsolutePath()
								+ " does not exist. The root directory of your project must contain a walkmod.xml");
			}
		}
		return Summary.getInstance().getWrittenFiles();
	}

	/**
	 * Downloads the list of declared plugins in the configuration file using
	 * Ivy.
	 * 
	 * @throws InvalidConfigurationException
	 *             if the walkmod configuration is invalid and it is working in
	 *             no verbose mode.
	 */
	public void install() throws InvalidConfigurationException {

		if (cfg.exists()) {
			if (verbose) {
				log.info(cfg.getAbsoluteFile() + " [ok]");
			}
			ConfigurationProvider cp = new IvyConfigurationProvider();
			if (verbose) {
				log.info("** THE PLUGIN INSTALLATION STARTS **");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
			long startTime = System.currentTimeMillis();
			long endTime = startTime;
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss",
					Locale.US);
			boolean error = false;
			try {
				ConfigurationManager cfgManager = new ConfigurationManager(cfg,
						cp);
				Configuration cf = cfgManager.getConfiguration();
			} catch (Exception e) {
				if (verbose) {
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
							+ (Runtime.getRuntime().freeMemory()) / 1048576
							+ " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576)
							+ " M");
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					if (printError) {
						log.error("Plugin installations fails", e);
					} else {
						log.info("Plugin installations fails. Please, execute walkmod with -e to see the details");
					}
				} else {
					throw new InvalidConfigurationException(e);
				}
			}
			if (!error) {
				if (verbose) {
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
							+ (Runtime.getRuntime().freeMemory()) / 1048576
							+ " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576)
							+ " M");
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
				}
			}
		} else {
			if (verbose) {
				log.error(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			} else {
				throw new WalkModException(
						cfg.getAbsolutePath()
								+ " does not exist. The root directory of your project must contain a walkmod.xml");
			}
		}
	}

	private void executeAllChains(ChainAdapterFactory apf, Configuration conf) {
		Collection<ChainConfig> tcgfs = conf.getChainConfigs();
		if (verbose) {
			log.info("** STARTING TRANSFORMATIONS CHAINS **");
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");
		}
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

			if (tcgfs.size() > 1) {
				if (verbose) {
					String label = "";
					if (tcfg.getName() != null
							&& !tcfg.getName().startsWith("chain_")) {
						label = "[" + tcfg.getName() + "](" + pos + "/"
								+ tcgfs.size() + ") ";
					} else {
						label = "(" + pos + "/" + tcgfs.size() + ")";
					}
					log.info("TRANSFORMATION CHAIN " + label);
					System.out.println();
				}
			}
			try {
				ChainAdapter ap = apf.createChainProxy(conf, tcfg.getName());
				ap.execute();
				num += ap.getWalkerAdapter().getWalker().getNumModifications();
				pos++;
				if (verbose) {
					if (Summary.getInstance().getWrittenFiles().isEmpty()) {
						log.info("**No sources changed**");
					}
					if (it.hasNext()) {
						System.out.println();
					}
				}
			} catch (Throwable e) {
				if (verbose) {
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
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: "
							+ (Runtime.getRuntime().freeMemory()) / 1048576
							+ " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576)
							+ " M");
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					log.info("Please, see the walkmod log file for details");
					if (printError) {
						log.error("TRANSFORMATION CHAIN (" + tcfg.getName()
								+ ") FAILS", e);
					} else {
						log.error("TRANSFORMATION CHAIN ("
								+ tcfg.getName()
								+ ") FAILS. Execute walkmod with -e to see the error details.");
					}
				} else {
					throw new WalkModException(e);
				}
				return;
			}
		}
		if (verbose) {
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
	}

	private void executeChainAdapter(ChainAdapterFactory apf,
			Configuration conf, String name) {

		ChainAdapter ap = apf.createChainProxy(conf, name);
		if (ap == null) {
			if (verbose) {
				log.error("The chain " + name + " is not found");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
		} else {
			long startTime = System.currentTimeMillis();
			long endTime = startTime;
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss",
					Locale.US);
			if (verbose) {
				log.info("** THE TRANSFORMATION CHAIN " + name + " STARTS **");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
			int num = 0;
			try {
				ap.execute();
				num = ap.getWalkerAdapter().getWalker().getNumModifications();
				if (verbose) {
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
						if (Summary.getInstance().getWrittenFiles().isEmpty()) {
							log.info("**No sources changed**");
						}
					}
					System.out.println();
					log.info("TRANSFORMATION CHAIN SUCCESS");
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: "
							+ (Runtime.getRuntime().freeMemory()) / 1048576
							+ " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576)
							+ " M");
					if (ap.getWalkerAdapter().getWalker().reportChanges()) {
						log.info("Total modified files: " + num);
					}
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
				}
			} catch (Throwable e) {
				if (verbose) {
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
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: "
							+ (Runtime.getRuntime().freeMemory()) / 1048576
							+ " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576)
							+ " M");
					System.out
							.print("----------------------------------------");
					System.out
							.println("----------------------------------------");
					log.info("Please, see the walkmod log file for details");
					if (printError) {
						log.error("TRANSFORMATION CHAIN (" + name + ") FAILS",
								e);
					} else {
						log.error("TRANSFORMATION CHAIN ("
								+ name
								+ ") FAILS. Execute walkmod with -e to see the error details.");
					}
				} else {
					throw new WalkModException(e);
				}
				return;
			}
		}
	}

}
