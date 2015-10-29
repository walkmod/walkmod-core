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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.walkmod.conf.ConfigurationManager;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.Initializer;
import org.walkmod.conf.ProjectConfigurationProvider;
import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.InitializerConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
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

	protected static Logger log = Logger.getLogger(WalkModFacade.class);

	private static final String DEFAULT_WALKMOD_FILE_NAME = "walkmod";

	private Options options;

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
	public WalkModFacade(File cfg, boolean offline, boolean verbose, boolean printError) {
		this.cfg = cfg;
		this.options = OptionsBuilder.options().offline(offline).verbose(verbose).printErrors(printError).build();
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
	public WalkModFacade(File cfg, boolean offline, boolean verbose, boolean printError, String[] includes,
			String[] excludes) {
		this.cfg = cfg;
		this.options = OptionsBuilder.options().offline(offline).verbose(verbose).printErrors(printError)
				.includes(includes).excludes(excludes).build();

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
	public WalkModFacade(String cfg, boolean offline, boolean verbose, boolean printError) {
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
	public WalkModFacade(String cfg, boolean offline, boolean verbose, boolean printError, String[] includes,
			String[] excludes) {
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
	public WalkModFacade(String cfg, boolean offline, boolean verbose, boolean printError, boolean throwsException,
			String[] includes, String[] excludes) {
		this.cfg = new File(cfg);
		this.options = OptionsBuilder.options().offline(offline).verbose(verbose).throwException(throwsException)
				.printErrors(printError).includes(includes).excludes(excludes).build();
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
		this(new File(DEFAULT_WALKMOD_FILE_NAME), offline, verbose, printError);
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
	public WalkModFacade(boolean offline, boolean verbose, boolean printError, String[] includes, String[] excludes) {
		this(new File(DEFAULT_WALKMOD_FILE_NAME + ".xml"), offline, verbose, printError, includes, excludes);
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
	 * @param optionsBuilder
	 *            Map of option. See {@link Options} and {@link OptionsBuilder}
	 *            for available options and default values.
	 */
	public WalkModFacade(OptionsBuilder optionsBuilder) {

		this(null, optionsBuilder, null);
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
	public WalkModFacade(File walkmodCfg, OptionsBuilder optionsBuilder, ConfigurationProvider configurationProvider) {

		// process options
		options = optionsBuilder.build();

		if (walkmodCfg != null) {
			this.cfg = walkmodCfg.getAbsoluteFile();
		} else {
			this.cfg = new File(options.getExecutionDirectory().getAbsolutePath(), DEFAULT_WALKMOD_FILE_NAME + "."
					+ options.getConfigurationFormat());
		}

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
			return new IvyConfigurationProvider(options.isOffline());
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
	public List<File> apply(String... chains) throws InvalidConfigurationException {
		List<File> result = new LinkedList<File>();
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());

		if (cfg.exists()) {
			if (options.isVerbose()) {
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
				System.setProperty("user.dir", userDir);

				if (options.isVerbose()) {
					if (!options.isPrintErrors()) {
						log.error(cfg.getAbsolutePath()
								+ " is invalid. Please, execute walkmod with -e to see the details.");
					} else {
						log.error("Invalid configuration", e);
					}
					if (options.isThrowException()) {
						RuntimeException re = new RuntimeException();
						re.setStackTrace(e.getStackTrace());
						throw re;
					}
					return null;
				} else {
					throw new InvalidConfigurationException(e);
				}
			}
			List<String> modules = config.getModules();
			if (modules != null && !modules.isEmpty()) {
				for (String module : modules) {
					File aux = new File(module).getAbsoluteFile();
					if (aux.isDirectory()) {
						if (options.isVerbose()) {
							log.info("** MODULE " + aux.getAbsoluteFile() + " [ok] **");
						}

						WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options(this.options.asMap())
								.executionDirectory(aux), null);
						result.addAll(facade.apply(chains));
					} else {
						log.error("The module " + aux.getAbsolutePath() + " is not an existing directory");
					}
				}
			}

			Summary.getInstance().clear();
			Collection<ChainConfig> chainCfgs = config.getChainConfigs();
			if (chainCfgs != null && !chainCfgs.isEmpty()) {
				Summary.getInstance().clear();
				if (chains == null || chains.length == 0) {
					executeAllChains(apf, config);
				} else {
					for (String chain : chains) {
						executeChainAdapter(apf, config, chain);
					}
				}
			}
			System.setProperty("user.dir", userDir);
		} else {
			System.setProperty("user.dir", userDir);
			if (options.isVerbose()) {
				log.error(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			} else {
				throw new WalkModException(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			}
		}
		result.addAll(Summary.getInstance().getWrittenFiles());
		return result;
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
	public List<File> check(String... chains) throws InvalidConfigurationException {

		List<File> result = new LinkedList<File>();
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());

		if (cfg.exists()) {
			if (options.isVerbose()) {
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
				System.setProperty("user.dir", userDir);
				if (options.isVerbose()) {
					if (!options.isPrintErrors()) {
						log.error(cfg.getAbsolutePath()
								+ " is invalid. Please, execute walkmod with -e to see the details.");
					} else {
						log.error("Invalid configuration", e);
					}
					if (options.isThrowException()) {
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
			if (tcgfs != null) {
				for (ChainConfig tcfg : tcgfs) {
					tcfg.getWriterConfig().setType(VisitorMessagesWriter.class.getName());
					tcfg.getWriterConfig().setModelWriter(new VisitorMessagesWriter());
				}
			}
			List<String> modules = config.getModules();
			if (modules != null && !modules.isEmpty()) {
				for (String module : modules) {
					File aux = new File(module).getAbsoluteFile();
					if (aux.isDirectory()) {
						if (options.isVerbose()) {
							log.info("** MODULE " + aux.getAbsoluteFile() + " [ok] **");
						}

						WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options(this.options.asMap())
								.executionDirectory(aux), null);
						result.addAll(facade.check(chains));
					} else {
						log.error("The module " + aux.getAbsolutePath() + " is not an existing directory");
					}
				}
			}

			Summary.getInstance().clear();
			Collection<ChainConfig> chainCfgs = config.getChainConfigs();
			if (chainCfgs != null && !chainCfgs.isEmpty()) {
				Summary.getInstance().clear();
				if (chains == null || chains.length == 0) {
					executeAllChains(apf, config);
				} else {
					for (String chain : chains) {
						executeChainAdapter(apf, config, chain);
					}
				}
			}
			System.setProperty("user.dir", userDir);
		} else {
			System.setProperty("user.dir", userDir);
			if (options.isVerbose()) {
				log.error(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			} else {
				throw new WalkModException(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			}
		}
		result.addAll(Summary.getInstance().getWrittenFiles());
		return result;
	}

	/**
	 * Initializes an empty walkmod configuration file
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());

		if (!cfg.exists()) {

			ConfigurationManager manager = new ConfigurationManager(cfg, false, locateConfigurationProvider());

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();
			try {
				cfgProvider.createConfig();

				manager.executeConfigurationProviders();

				Configuration conf = manager.getConfiguration();

				List<InitializerConfig> initializers = conf.getInitializers();

				if (initializers != null) {
					for (InitializerConfig initCfg : initializers) {
						String beanId = initCfg.getPluginGroupId() + ":" + initCfg.getPluginArtifactId() + ":"
								+ initCfg.getType();

						if (conf.containsBean(beanId)) {

							Object o = conf.getBean(beanId, initCfg.getParams());
							if (o != null && o instanceof Initializer) {
								((Initializer) o).execute(cfgProvider);
							}
						}
					}
				}

				if (options.isVerbose()) {
					log.info("CONFIGURATION FILE [" + cfg.getAbsolutePath() + "] CREATION COMPLETE");
				}

			} catch (IOException aux) {
				if (options.isVerbose()) {
					log.error("The system can't create the file [ " + cfg.getAbsolutePath() + "]");
				}
				if (options.isThrowException()) {
					System.setProperty("user.dir", userDir);
					throw aux;
				}
			}

		} else {
			if (options.isVerbose()) {
				log.error("The configuration file [" + cfg.getAbsolutePath() + "] already exists");
			}
		}
		System.setProperty("user.dir", userDir);
	}

	/**
	 * Adds a new chain configuration into the configuration file
	 * 
	 * @param chainCfg
	 *            chain configuration to add
	 * @param recursive
	 *            Adds the new chain into all the submodules
	 * @throws Exception
	 *             in case that the walkmod configuration file can't be read.
	 */
	public void addChainConfig(ChainConfig chainCfg, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

			cfgProvider.addChainConfig(chainCfg, recursive);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}
	}

	/**
	 * Adds a new transformation configuration into the configuration file
	 * 
	 * @param chain
	 *            chain identifier where the transformation will be appended. It
	 *            can be null.
	 * @param transformationCfg
	 *            transformation configuration to add
	 * @throws Exception
	 *             in case that the walkmod configuration file can't be read.
	 */
	public void addTransformationConfig(String chain, String path, boolean recursive,
			TransformationConfig transformationCfg) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

			cfgProvider.addTransformationConfig(chain, path, transformationCfg, false);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}
	}

	/**
	 * Adds a new provider configuration into the configuration file
	 * 
	 * @param providerCfg
	 *            provider configuration to add.
	 * @param recursive
	 * @throws Exception
	 *             in case that the walkmod configuration file can't be read.
	 */
	public void addProviderConfig(ProviderConfig providerCfg, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

			cfgProvider.addProviderConfig(providerCfg, recursive);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}
	}

	/**
	 * Adds a new plugin into the configuration file
	 * 
	 * @param pluginConfig
	 *            the plugin to add
	 * @param recursive
	 * @throws Exception
	 *             in case that the walkmod configuration file can't be read.
	 */
	public void addPluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();
			cfgProvider.addPluginConfig(pluginConfig, recursive);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}

	}

	private void updateMsg(long startTime, Exception e) {
		if (options.isVerbose()) {
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
			long endTime = System.currentTimeMillis();
			double time = 0;
			if (endTime > startTime) {
				time = (double) (endTime - startTime) / (double) 1000;
			}
			String timeMsg = myFormatter.format(time);
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");
			if (e == null) {
				log.info("CONFIGURATION UPDATE SUCCESS");
			} else {
				log.info("CONFIGURATION UPDATE FAILS");
			}
			System.out.println();
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");
			log.info("Total time: " + timeMsg + " seconds");
			log.info("Finished at: " + df.format(new Date()));
			log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
					+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
			System.out.print("----------------------------------------");
			System.out.println("----------------------------------------");

			if (e != null) {
				if (options.isPrintErrors()) {
					log.error("Plugin installations fails", e);
				} else {
					log.info("Plugin installations fails. Please, execute walkmod with -e to see the details");
				}
				if (options.isThrowException()) {
					RuntimeException re = new RuntimeException();
					re.setStackTrace(e.getStackTrace());
					throw re;
				}
			}
		}
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
			if (options.isVerbose()) {
				log.info(cfg.getAbsoluteFile() + " [ok]");
			}
			// Uses Ivy always
			ConfigurationProvider cp = new IvyConfigurationProvider(options.isOffline());
			if (options.isVerbose()) {
				log.info("** THE PLUGIN INSTALLATION STARTS **");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
			long startTime = System.currentTimeMillis();
			long endTime = startTime;
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
			boolean error = false;
			try {
				userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
				System.setProperty("user.dir", options.getExecutionDirectory().getCanonicalPath());
				ConfigurationManager cfgManager = new ConfigurationManager(cfg, cp);
				Configuration cf = cfgManager.getConfiguration();

				List<String> modules = cf.getModules();
				if (modules != null && !modules.isEmpty()) {
					for (String module : modules) {
						File aux = new File(module).getAbsoluteFile();
						if (aux.isDirectory()) {
							if (options.isVerbose()) {
								log.info("** MODULE " + aux.getAbsoluteFile() + " [ok] **");
							}

							WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options(options.asMap())
									.executionDirectory(aux), null);
							facade.install();
						} else {
							log.error("The module " + aux.getAbsolutePath() + " is not an existing directory");
						}
					}
				}

			} catch (Exception e) {
				System.setProperty("user.dir", userDir);
				if (options.isVerbose()) {
					error = true;
					endTime = System.currentTimeMillis();
					double time = 0;
					if (endTime > startTime) {
						time = (double) (endTime - startTime) / (double) 1000;
					}
					String timeMsg = myFormatter.format(time);
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					log.info("PLUGIN INSTALLATION FAILS");
					System.out.println();
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					if (options.isPrintErrors()) {
						log.error("Plugin installations fails", e);
					} else {
						log.info("Plugin installations fails. Please, execute walkmod with -e to see the details");
					}
					if (options.isThrowException()) {
						RuntimeException re = new RuntimeException();
						re.setStackTrace(e.getStackTrace());
						throw re;
					}
				} else {
					throw new InvalidConfigurationException(e);
				}
			}
			if (!error) {
				System.setProperty("user.dir", userDir);
				if (options.isVerbose()) {
					endTime = System.currentTimeMillis();
					double time = 0;
					if (endTime > startTime) {
						time = (double) (endTime - startTime) / (double) 1000;
					}
					String timeMsg = myFormatter.format(time);
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					System.out.println();
					log.info("PLUGIN INSTALLATION COMPLETE");
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
				}
			}
		} else {
			if (options.isVerbose()) {
				log.error(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");

			} else {
				throw new WalkModException(cfg.getAbsolutePath()
						+ " does not exist. The root directory of your project must contain a walkmod.xml");
			}
		}
	}

	private void executeAllChains(ChainAdapterFactory apf, Configuration conf) {
		Collection<ChainConfig> tcgfs = conf.getChainConfigs();

		if (tcgfs != null) {
			if (options.isVerbose()) {
				log.info("** STARTING TRANSFORMATIONS CHAINS **");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
			long startTime = System.currentTimeMillis();
			long endTime = startTime;
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
			int num = 0;
			Iterator<ChainConfig> it = tcgfs.iterator();
			int pos = 1;
			while (it.hasNext()) {
				ChainConfig tcfg = it.next();

				if (tcgfs.size() > 1) {
					if (options.isVerbose()) {
						String label = "";
						if (tcfg.getName() != null && !tcfg.getName().startsWith("chain_")) {
							label = "[" + tcfg.getName() + "](" + pos + "/" + tcgfs.size() + ") ";
						} else {
							label = "(" + pos + "/" + tcgfs.size() + ")";
						}
						log.info("TRANSFORMATION CHAIN " + label);
						System.out.println();
					}
				}
				try {

					if (options.getIncludes() != null) {
						String[] includes = options.getIncludes().toArray(new String[options.getIncludes().size()]);
						tcfg.getReaderConfig().setIncludes(includes);
					}
					if (options.getExcludes() != null) {
						String[] excludes = options.getExcludes().toArray(new String[options.getExcludes().size()]);
						tcfg.getReaderConfig().setExcludes(excludes);
					}

					ChainAdapter ap = apf.createChainProxy(conf, tcfg.getName());

					ap.execute();
					num += ap.getWalkerAdapter().getWalker().getNumModifications();
					pos++;
					if (options.isVerbose()) {
						if (Summary.getInstance().getWrittenFiles().isEmpty()) {
							log.info("**No sources changed**");
						}
						if (it.hasNext()) {
							System.out.println();
						}
					}
				} catch (Throwable e) {
					if (options.isVerbose()) {
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
						log.info("TRANSFORMATION CHAIN FAILS");
						System.out.println();
						System.out.print("----------------------------------------");
						System.out.println("----------------------------------------");
						log.info("Total time: " + timeMsg + " seconds");
						log.info("Finished at: " + df.format(new Date()));
						log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
								+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
						System.out.print("----------------------------------------");
						System.out.println("----------------------------------------");
						log.info("Please, see the walkmod log file for details");
						if (options.isPrintErrors()) {
							log.error("TRANSFORMATION CHAIN (" + tcfg.getName() + ") FAILS", e);
						} else {
							log.error("TRANSFORMATION CHAIN (" + tcfg.getName()
									+ ") FAILS. Execute walkmod with -e to see the error details.");
						}
						if (options.isThrowException()) {
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

			if (options.isVerbose()) {
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
				log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
						+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
				log.info("Total modified files: " + num);
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
		}
	}

	private void executeChainAdapter(ChainAdapterFactory apf, Configuration conf, String name) {
		if (options.getIncludes() != null || options.getExcludes() != null) {
			Collection<ChainConfig> chains = conf.getChainConfigs();
			if (chains != null) {
				for (ChainConfig cc : chains) {
					if (options.getIncludes() != null) {
						String[] includes = options.getIncludes().toArray(new String[options.getIncludes().size()]);
						cc.getReaderConfig().setIncludes(includes);
					}
					if (options.getExcludes() != null) {
						String[] excludes = options.getExcludes().toArray(new String[options.getExcludes().size()]);
						cc.getReaderConfig().setExcludes(excludes);
					}
				}
			}
		}
		ChainAdapter ap = apf.createChainProxy(conf, name);
		if (ap == null) {
			if (options.isVerbose()) {
				log.error("The chain " + name + " is not found");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
		} else {
			long startTime = System.currentTimeMillis();
			long endTime = startTime;
			DecimalFormat myFormatter = new DecimalFormat("###.###");
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
			if (options.isVerbose()) {
				log.info("** THE TRANSFORMATION CHAIN " + name + " STARTS **");
				System.out.print("----------------------------------------");
				System.out.println("----------------------------------------");
			}
			int num = 0;
			try {
				ap.execute();
				num = ap.getWalkerAdapter().getWalker().getNumModifications();
				if (options.isVerbose()) {
					endTime = System.currentTimeMillis();
					double time = 0;
					if (endTime > startTime) {
						time = (double) (endTime - startTime) / (double) 1000;
					}
					String timeMsg = myFormatter.format(time);
					if (num != 0) {
						System.out.print("----------------------------------------");
						System.out.println("----------------------------------------");
					} else {
						if (Summary.getInstance().getWrittenFiles().isEmpty()) {
							log.info("**No sources changed**");
						}
					}
					System.out.println();
					log.info("TRANSFORMATION CHAIN SUCCESS");
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
					if (ap.getWalkerAdapter().getWalker().reportChanges()) {
						log.info("Total modified files: " + num);
					}
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
				}
			} catch (Throwable e) {
				System.setProperty("user.dir", userDir);
				if (options.isVerbose()) {
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
					log.info("TRANSFORMATION CHAIN FAILS");
					System.out.println();
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					log.info("Total time: " + timeMsg + " seconds");
					log.info("Finished at: " + df.format(new Date()));
					log.info("Final memory: " + (Runtime.getRuntime().freeMemory()) / 1048576 + " M/ "
							+ (Runtime.getRuntime().totalMemory() / 1048576) + " M");
					System.out.print("----------------------------------------");
					System.out.println("----------------------------------------");
					log.info("Please, see the walkmod log file for details");
					if (options.isPrintErrors()) {
						log.error("TRANSFORMATION CHAIN (" + name + ") FAILS", e);
					} else {
						log.error("TRANSFORMATION CHAIN (" + name
								+ ") FAILS. Execute walkmod with -e to see the error details.");
					}
				} else {
					throw new WalkModException(e);
				}
				return;
			}
		}
	}

	/**
	 * Adds a module into the configuration file
	 * 
	 * @param modules
	 *            Modules to add
	 * @throws Exception
	 *             if the walkmod configuration file can't be read.
	 */
	public void addModules(List<String> modules) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (modules != null) {
			if (!cfg.exists()) {
				init();
			}
			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);

				ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

				cfgProvider.addModules(modules);
			} catch (Exception e) {
				exception = e;
			} finally {
				System.setProperty("user.dir", userDir);
				updateMsg(startTime, exception);
			}
		}

	}

	/**
	 * Remove a list of transformations for an specific chain. If it is
	 * recursive, this removal action is applied into all the submodules.
	 * 
	 * @param chain
	 *            To remove the transformations. By default, it is the "default"
	 *            chain.
	 * @param transformations
	 *            List of transformation names (type ids) to be removed.
	 * @param recursive
	 *            If the action is applied to all the submodules.
	 * @throws Exception
	 *             if the walkmod configuration file can't be read.
	 */
	public void removeTransformations(String chain, List<String> transformations, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (transformations != null) {
			if (!cfg.exists()) {
				init();
			}
			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);

				ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

				cfgProvider.removeTransformations(chain, transformations, recursive);
			} catch (Exception e) {
				exception = e;
			} finally {
				System.setProperty("user.dir", userDir);
				updateMsg(startTime, exception);
			}
		}

	}

	/**
	 * Sets an specific writer for an specific chain. 
	 * @param chain
	 * 		Chain to apply the writer
	 * @param type
	 * 		Writer type to set
	 * @param path
	 * 		Writer path to set
	 * @param recursive
	 * 		If to set the writer to all the submodules.
	 * @throws Exception
	 * 		 if the walkmod configuration file can't be read.
	 */
	public void setWriter(String chain, String type, String path, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if ((type != null && !"".equals(type.trim())) || (path != null && !"".equals(path.trim()))) {
			if (!cfg.exists()) {
				init();
			}
			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);

				ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

				cfgProvider.setWriter(chain, type, path, recursive);
			} catch (Exception e) {
				exception = e;
			} finally {
				System.setProperty("user.dir", userDir);
				updateMsg(startTime, exception);
			}
		}
	}

	/**
	 * Sets an specific reader for an specific chain. 
	 * @param chain
	 * 		Chain to apply the writer
	 * @param type
	 * 		Reader type to set
	 * @param path
	 * 		Reader path to set
	 * @param recursive
	 * 		If to set the reader to all the submodules.
	 * @throws Exception
	 * 		 if the walkmod configuration file can't be read.
	 */
	public void setReader(String chain, String type, String path, boolean recursive) throws Exception {
		if ((type != null && !"".equals(type.trim())) || (path != null && !"".equals(path.trim()))) {
			long startTime = System.currentTimeMillis();
			Exception exception = null;
			if (!cfg.exists()) {
				init();
			}
			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);

				ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();

				cfgProvider.setReader(chain, type, path, recursive);
			} catch (Exception e) {
				exception = e;
			} finally {
				System.setProperty("user.dir", userDir);
				updateMsg(startTime, exception);
			}
		}
	}

	/**
	 * Removes a plugin from the configuration file.
	 * @param pluginConfig
	 * 		Plugin configuration to remove.
	 * @param recursive
	 * 		If it necessary to remove the plugin from all the submodules.
	 * @throws Exception
	 * 		  if the walkmod configuration file can't be read.
	 */
	public void removePluginConfig(PluginConfig pluginConfig, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();
			cfgProvider.removePluginConfig(pluginConfig, recursive);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}
	}

	/**
	 * Removes the module list from the configuration file
	 * @param modules
	 * 		Module names to remove
	 * @throws Exception
	 * 		if the walkmod configuration file can't be read.
	 */
	public void removeModules(List<String> modules) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();
			cfgProvider.removeModules(modules);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}
	}

	/**
	 * Removes the list of configuration providers from the config file.
	 * @param providers
	 * 		Name of the configuration providers to remove.
	 * @param recursive
	 * 		If it necessary to remove the plugin from all the submodules.
	 * @throws Exception
	 * 		If the walkmod configuration file can't be read.
	 */
	public void removeProviders(List<String> providers, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (!cfg.exists()) {
			init();
		}
		userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
		try {
			ConfigurationManager manager = new ConfigurationManager(cfg, false);

			ProjectConfigurationProvider cfgProvider = manager.getProjectConfigurationProvider();
			cfgProvider.removeProviders(providers, recursive);
		} catch (Exception e) {
			exception = e;
		} finally {
			System.setProperty("user.dir", userDir);
			updateMsg(startTime, exception);
		}
	}

	/**
	 * Returns the equivalent configuration representation of the Walkmod config file.
	 * @return Configuration object representation of the config file.
	 * @throws Exception
	 */
	public Configuration getConfiguration() throws Exception {
		Configuration result = null;
		if (cfg.exists()) {

			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);
				manager.executeConfigurationProviders();
				result = manager.getConfiguration();
			} finally {
				System.setProperty("user.dir", userDir);
			}
		}
		return result;
	}

	/**
	 * Removes the chains from the Walkmod config file.
	 * @param chains
	 * 		Chain names to remove
	 * @param recursive
	 * 		If it necessary to remove the chains from all the submodules.
	 * @throws Exception
	 * 		If the walkmod configuration file can't be read.
	 */
	public void removeChains(List<String> chains, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (cfg.exists()) {

			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);
				manager.getProjectConfigurationProvider().removeChains(chains, recursive);
			} catch (Exception e) {
				exception = e;
			} finally {
				System.setProperty("user.dir", userDir);
				updateMsg(startTime, exception);
			}
		}

	}

	/**
	 * Retrieves the bean definitions that contains an specific plugin.
	 * @param plugin 
	 * 		Plugin container of bean definitions.
	 * @return
	 * 		List of bean definitions.
	 */
	public List<BeanDefinition> inspectPlugin(PluginConfig plugin) {
		Configuration conf = new ConfigurationImpl();
		Collection<PluginConfig> plugins = new LinkedList<PluginConfig>();
		plugins.add(plugin);
		conf.setPlugins(plugins);
		ConfigurationManager manager = new ConfigurationManager(conf, locateConfigurationProvider());
		manager.executeConfigurationProviders();
		return conf.getAvailableBeans(plugin);
	}

	/**
	 * Sets an specific parameter value into a bean.
	 * @param param
	 * 		Parameter name
	 * @param value
	 * 		Parameter value
	 * @param type
	 * 		Bean type to set the parameter
	 * @param category
	 * 		Bean category to set the parameter (walker, reader, transformation, writer)
	 * @param name
	 * 		Bean name/alias to set the parameter
	 * @param chain
	 * 		Bean chain to filter the beans to take into account
	 * @param recursive
	 * 		If it necessary to set the parameter to all the submodules.
	 * @throws Exception
	 */
	public void addConfigurationParameter(String param, String value, String type, String category, String name,
			String chain, boolean recursive) throws Exception {
		long startTime = System.currentTimeMillis();
		Exception exception = null;
		if (cfg.exists()) {

			userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
			try {
				ConfigurationManager manager = new ConfigurationManager(cfg, false);
				manager.getProjectConfigurationProvider().addConfigurationParameter(param, value, type, category, name,
						chain, recursive);
			} catch (Exception e) {
				exception = e;
			} finally {
				System.setProperty("user.dir", userDir);
				updateMsg(startTime, exception);
			}
		}

	}

}
