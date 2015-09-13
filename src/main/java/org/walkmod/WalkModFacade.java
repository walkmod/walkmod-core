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
import java.io.IOException;
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

	private static final String DEFAULT_WALKMOD_FILE = "walkmod.xml";

	private boolean offline = false;

	private boolean verbose = true;

	private boolean printError = false;

	private boolean throwsException = false;

	private String[] includes;

	private String[] excludes;

	private File executionDir = new File(".");

	private String userDir = ".";

	/**
	 * Walkmod configuration file
	 */
	private File cfg;

	/**
	 * Initializes the configuration of the walkmod context (e.g. classpath)
	 */
	private ConfigurationProvider configurationProvider;

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
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	public WalkModFacade(File cfg, boolean offline, boolean verbose,
			boolean printError) {
		this.cfg = cfg;
		this.offline = offline;
		this.verbose = verbose;
		this.printError = printError;
	}

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
	 * @param includes
	 *            the list of included files.
	 * @param excludes
	 *            the list of excluded files.
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	public WalkModFacade(File cfg, boolean offline, boolean verbose,
			boolean printError, String[] includes, String[] excludes) {
		this.cfg = cfg;
		this.offline = offline;
		this.verbose = verbose;
		this.printError = printError;
		this.includes = includes;
		this.excludes = excludes;
	}

	/**
	 * Facade constructor using the walkmod.xml configuration file.
	 * 
	 * @param cfg
	 *            configuration file
	 * @param offline
	 *            if the missing plugins are downloaded.
	 * @param verbose
	 *            if log messages are printed in the System.out
	 * @param printError
	 *            if the exception trace is printed in the System.out
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	public WalkModFacade(String cfg, boolean offline, boolean verbose,
			boolean printError) {
		this(cfg, offline, verbose, printError, null, null);
	}

	/**
	 * Facade constructor using the walkmod.xml configuration file.
	 * 
	 * @param cfg
	 *            configuration file
	 * @param offline
	 *            if the missing plugins are downloaded.
	 * @param verbose
	 *            if log messages are printed in the System.out
	 * @param printError
	 *            if the exception trace is printed in the System.out
	 * @param includes
	 *            the list of included files.
	 * @param excludes
	 *            the list of excluded files.
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	@Deprecated
	public WalkModFacade(String cfg, boolean offline, boolean verbose,
			boolean printError, String[] includes, String[] excludes) {
		this(cfg, offline, verbose, printError, false, includes, excludes);
	}

	/**
	 * Facade constructor using the walkmod.xml configuration file.
	 * 
	 * @param cfg
	 *            configuration file
	 * @param offline
	 *            if the missing plugins are downloaded.
	 * @param verbose
	 *            if log messages are printed in the System.out
	 * @param printError
	 *            if the exception trace is printed in the System.out
	 * @param throwsException
	 *            if the exception is thrown to the calling process
	 * @param includes
	 *            the list of included files.
	 * @param excludes
	 *            the list of excluded files.
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	@Deprecated
	public WalkModFacade(String cfg, boolean offline, boolean verbose,
			boolean printError, boolean throwsException, String[] includes,
			String[] excludes) {
		this.cfg = new File(cfg);
		this.offline = offline;
		this.verbose = verbose;
		this.throwsException = throwsException;
		this.printError = printError;
		this.includes = includes;
		this.excludes = excludes;
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
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	@Deprecated
	public WalkModFacade(boolean offline, boolean verbose, boolean printError) {
		this(new File(DEFAULT_WALKMOD_FILE), offline, verbose, printError);
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
	 * @param includes
	 *            the list of included files.
	 * @param excludes
	 *            the list of excluded files.
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	@Deprecated
	public WalkModFacade(boolean offline, boolean verbose, boolean printError,
			String[] includes, String[] excludes) {
		this(new File(DEFAULT_WALKMOD_FILE), offline, verbose, printError,
				includes, excludes);
	}

	/**
	 * Default facade constructor. It uses the walkmod.xml configuration file,
	 * it is not off-line, it is verbose and doesn't print the exceptions' stack
	 *
	 * @deprecated Use #{link #WalkModFacade(File,OptionsBuilder,
	 *             ConfigurationProvider} instead
	 */
	@Deprecated
	public WalkModFacade() {
		this(false, true, false);
	}

	/**
	 * Initalizes a Walkmod service
	 *
	 * @param walkmodCfg
	 *            Walkmod configuration file. If null, file named 'walkmod.xml'
	 *            is searched at the root.
	 * @param optionsBuilder
	 *            Map of option. See {@link Options} and {@link OptionsBuilder}
	 *            for available options and default values.
	 * @param configurationProvider
	 *            Configuration provider responsible for the resolution of
	 *            plugins (used to use custom classloading strategies). If null
	 *            Ivy is used.
	 */
	public WalkModFacade(File walkmodCfg, OptionsBuilder optionsBuilder,
			ConfigurationProvider configurationProvider) {

		// process options
		Options options = optionsBuilder.build();

		this.offline = options.isOffline();
		this.verbose = options.isVerbose();
		this.printError = options.isPrintErrors();
		this.throwsException = options.isThrowException();
		List<String> includes = options.getIncludes();
		if (includes != null) {
			this.includes = includes.toArray(new String[includes.size()]);
		}
		List<String> excludes = options.getExcludes();
		if (excludes != null) {
			this.excludes = excludes.toArray(new String[excludes.size()]);
		}
		this.executionDir = options.getExecutionDirectory();

		this.cfg = (walkmodCfg != null) ? walkmodCfg : new File(
				DEFAULT_WALKMOD_FILE);

		if (configurationProvider != null)
			this.configurationProvider = configurationProvider;
	}

	/**
	 * Takes care of chosing the proper configuration provider
	 *
	 * NOTE: this is a first pass, handling a default provider should be
	 * improved
	 */
	private ConfigurationProvider locateConfigurationProvider() {
		if (configurationProvider == null)
			return new IvyConfigurationProvider(offline);
		else
			return configurationProvider;
	}


	/**
	 * Applies a list of transformation chains.
	 * 
	 * @param chains
	 *            the list of applied transformation chains.
	 * @throws InvalidConfigurationException
	 *             if the walkmod configuration is invalid and it is working in
	 *             no verbose mode.
	 * @return The list of modified/created files.
	 */
	public List<File> apply(String... chains)
			throws InvalidConfigurationException {

		userDir = new File(".").getAbsolutePath();
		jnr.posix.JavaLibCHelper.chdir(executionDir.getAbsolutePath());
		
		if (cfg.exists()) {
			if (verbose) {
				log.info(cfg.getAbsoluteFile() + " [ok]");
			}
			ConfigurationProvider cp = locateConfigurationProvider();
			ConfigurationManager cfgManager = null;
			Configuration config = null;
			ChainAdapterFactory apf = null;
			try {
				cfgManager = new ConfigurationManager(cfg, cp);
				config = cfgManager.getConfiguration();
				apf = new DefaultChainAdapterFactory();
			} catch (Exception e) {
				jnr.posix.JavaLibCHelper.chdir(userDir);
				if (verbose) {
					if (!printError) {
						log.error(cfg.getAbsolutePath()
								+ " is invalid. Please, execute walkmod with -e to see the details.");
					} else {
						log.error("Invalid configuration", e);
					}
					if (throwsException) {
						RuntimeException re = new RuntimeException();
						re.setStackTrace(e.getStackTrace());
						throw re;
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
			jnr.posix.JavaLibCHelper.chdir(userDir);
		} else {
			jnr.posix.JavaLibCHelper.chdir(userDir);
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
	 * @return The list of affected files.
	 */
	public List<File> check(String... chains)
			throws InvalidConfigurationException {

		userDir = new File(".").getAbsolutePath();
		jnr.posix.JavaLibCHelper.chdir(executionDir.getAbsolutePath());
		
		if (cfg.exists()) {
			if (verbose) {
				log.info(cfg.getAbsoluteFile() + " [ok]");
			}
			ConfigurationProvider cp = locateConfigurationProvider();
			ConfigurationManager cfgManager = null;
			Configuration config = null;
			ChainAdapterFactory apf = null;
			try {
				cfgManager = new ConfigurationManager(cfg, cp);
				config = cfgManager.getConfiguration();
				apf = new DefaultChainAdapterFactory();
			} catch (Exception e) {
				jnr.posix.JavaLibCHelper.chdir(userDir);
				if (verbose) {
					if (!printError) {
						log.error(cfg.getAbsolutePath()
								+ " is invalid. Please, execute walkmod with -e to see the details.");
					} else {
						log.error("Invalid configuration", e);
					}
					if (throwsException) {
						RuntimeException re = new RuntimeException();
						re.setStackTrace(e.getStackTrace());
						throw re;
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
			jnr.posix.JavaLibCHelper.chdir(userDir);
		} else {
			jnr.posix.JavaLibCHelper.chdir(userDir);
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
	 * Ivy. Ignores the ConfigurationProvided if passed in the constructor.
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
			// Uses Ivy always
			ConfigurationProvider cp = new IvyConfigurationProvider(offline);
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
				userDir = new File(".").getAbsolutePath();
				jnr.posix.JavaLibCHelper.chdir(executionDir.getCanonicalPath());
				ConfigurationManager cfgManager = new ConfigurationManager(cfg,
						cp);
				Configuration cf = cfgManager.getConfiguration();
			} catch (Exception e) {
				jnr.posix.JavaLibCHelper.chdir(userDir);
				if (verbose) {
					error = true;
					endTime = System.currentTimeMillis();
					double time = 0;
					if (endTime > startTime) {
						time = (double) (endTime - startTime) / (double) 1000;
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
					if (throwsException) {
						RuntimeException re = new RuntimeException();
						re.setStackTrace(e.getStackTrace());
						throw re;
					}
				} else {
					throw new InvalidConfigurationException(e);
				}
			}
			if (!error) {
				jnr.posix.JavaLibCHelper.chdir(userDir);
				if (verbose) {
					endTime = System.currentTimeMillis();
					double time = 0;
					if (endTime > startTime) {
						time = (double) (endTime - startTime) / (double) 1000;
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

				if (includes != null) {
					tcfg.getReaderConfig().setIncludes(includes);
				}
				if (excludes != null) {
					tcfg.getReaderConfig().setExcludes(excludes);
				}

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
						time = (double) (endTime - startTime) / (double) 1000;
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
					if (throwsException) {
						RuntimeException re = new RuntimeException();
						re.setStackTrace(e.getStackTrace());
						throw re;
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
				time = (double) (endTime - startTime) / (double) 1000;
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
		if (includes != null || excludes != null) {
			Collection<ChainConfig> chains = conf.getChainConfigs();
			if (chains != null) {
				for (ChainConfig cc : chains) {
					if (includes != null) {

						cc.getReaderConfig().setIncludes(includes);
					}
					if (excludes != null) {
						cc.getReaderConfig().setExcludes(excludes);
					}
				}
			}
		}
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
						time = (double) (endTime - startTime) / (double) 1000;
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
				jnr.posix.JavaLibCHelper.chdir(userDir);
				if (verbose) {
					endTime = System.currentTimeMillis();
					double time = 0;
					if (endTime > startTime) {
						time = (double) (endTime - startTime) / (double) 1000;
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
