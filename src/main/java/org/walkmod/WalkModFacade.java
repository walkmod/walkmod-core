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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.walkmod.conf.ConfigurationManager;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.ExecutionModeEnum;
import org.walkmod.conf.ProjectConfigurationProvider;
import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.providers.DynamicConfigurationProvider;
import org.walkmod.conf.providers.DynamicModulesConfigurationProvider;
import org.walkmod.conf.providers.ExecutionModeProvider;
import org.walkmod.conf.providers.IvyConfigurationProvider;
import org.walkmod.exceptions.InvalidConfigurationException;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.writers.Summary;

/**
 * Facade to execute walkmod services.
 * 
 * @author Raquel Pau
 *
 */
public class WalkModFacade {

    protected static Logger log = Logger.getLogger(WalkModFacade.class);

    private static final String DEFAULT_WALKMOD_FILE_NAME = "walkmod";

    private final Options options;

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
     * Initalizes a Walkmod service
     *
     * @param optionsBuilder
     *            Map of option. See {@link Options} and {@link OptionsBuilder} for available
     *            options and default values.
     * @deprecated use {@link #WalkModFacade(Options)}
     */
    public WalkModFacade(OptionsBuilder optionsBuilder) {

        this(null, optionsBuilder.build(), null);
    }

    /**
     * Initalizes a Walkmod service
     *
     * @param options
     *            Map of option. See {@link Options} and {@link OptionsBuilder} for available
     *            options and default values.
     */
    public WalkModFacade(Options options) {

        this(options.getConfigurationFile(), options, null);
    }

    /**
     * Initalizes a Walkmod service
     *  @param walkmodCfg
     *            Walkmod configuration file. If null, file named 'walkmod.xml' is searched at the
     *            root.
     * @param optionsArg
     *            Map of option. See {@link Options} and {@link OptionsBuilder} for available
     *            options and default values.
     * @param configurationProvider
     *            Configuration provider responsible for the resolution of plugins (used to use
     * @deprecated use constructor with Options parameter
     */
    public WalkModFacade(/* @Nullable */ File walkmodCfg, OptionsBuilder optionsArg, ConfigurationProvider configurationProvider) {
        this(walkmodCfg, optionsArg.build(), configurationProvider);
    }

    /**
     * Initalizes a Walkmod service
     *  @param walkmodCfg
     *            Walkmod configuration file. If null, file named 'walkmod.xml' is searched at the
     *            root.
     * @param optionsArg
     *            Map of option. See {@link Options} and {@link OptionsBuilder} for available
     *            options and default values.
     * @param configurationProvider
     *            Configuration provider responsible for the resolution of plugins (used to use
     */
    public WalkModFacade(/* @Nullable */ File walkmodCfg, Options optionsArg, ConfigurationProvider configurationProvider) {

        this.options = optionsArg;

        if (walkmodCfg != null) {
            this.cfg = walkmodCfg.getAbsoluteFile();
        } else {
            this.cfg = new File(options.getExecutionDirectory().getAbsolutePath(),
                    DEFAULT_WALKMOD_FILE_NAME + "." + options.getConfigurationFormat());
        }

        if (configurationProvider != null)
            this.configurationProvider = configurationProvider;
    }

    /**
     * Takes care of chosing the proper configuration provider
     *
     * NOTE: this is a first pass, handling a default provider should be improved
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
     *             if the walkmod configuration is invalid and it is working in no verbose mode.
     * @return The list of modified/created files.
     */
    public List<File> apply(String... chains) throws InvalidConfigurationException {

        final List<File> result = new LinkedList<File>();

        run(result, new WalkmodCommand() {
            @Override
            public void execute(Options options, File executionDir, String... chains) throws Exception {
                WalkModFacade facade = new WalkModFacade(OptionsBuilder.options(options).executionDirectory(executionDir).build());

                result.addAll(facade.apply(chains));
            }
        }, ExecutionModeEnum.APPLY, chains);

        return result;

    }

    private void printConfigError(Exception e) throws InvalidConfigurationException {
        System.setProperty("user.dir", userDir);

        if (options.isVerbose()) {
            if (!options.isPrintErrors()) {
                log.error(cfg.getAbsolutePath() + " is invalid. Please, execute walkmod with -e to see the details.");
            } else {
                log.error("Invalid configuration", e);
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

    private Configuration readConfig(ConfigurationProvider... cp) throws InvalidConfigurationException {

        Configuration config = null;

        try {
            ConfigurationManager cfgManager = new ConfigurationManager(cfg, cp);
            config = cfgManager.getConfiguration();
            config.setParameters(options.getMutableCopyOfDynamicArgs());

        } catch (Exception e) {
            printConfigError(e);
        }
        return config;
    }

   

    private Configuration createConfig(String[] chains, ConfigurationProvider... cp)
            throws InvalidConfigurationException {
        Configuration config = new ConfigurationImpl();
        try {
            DynamicConfigurationProvider prov = new DynamicConfigurationProvider(options, chains);
            prov.init(config);
            prov.load();

            ConfigurationManager cfgManager = new ConfigurationManager(config, cp);

            DynamicModulesConfigurationProvider prov2 = new DynamicModulesConfigurationProvider();
            prov2.init(config);
            prov2.load();

            config = cfgManager.getConfiguration();
            config.setParameters(options.getMutableCopyOfDynamicArgs());

        } catch (Exception e) {
            printConfigError(e);
        }
        return config;
    }

    /**
     * Generates a list of patches according the transformation chains
     * 
     * @param chains
     *            the list of applied transformation chains.
     * @throws InvalidConfigurationException
     *             if the walkmod configuration is invalid and it is working in no verbose mode.
     * @return The list of affected files.
     */
    public List<File> patch(String... chains) throws InvalidConfigurationException {

        final List<File> result = new LinkedList<File>();

        run(result, new WalkmodCommand() {
            @Override
            public void execute(Options options, File executionDir, String... chains) throws Exception {
                WalkModFacade facade = new WalkModFacade(OptionsBuilder.options(options).executionDirectory(executionDir).build());

                result.addAll(facade.patch(chains));
            }
        }, ExecutionModeEnum.PATCH, chains);

        return result;
    }

    private void run(List<File> result, WalkmodCommand command, ExecutionModeEnum execMode, String... chains)
            throws InvalidConfigurationException {

        userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
        System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
        Configuration config = null;

        if (cfg.exists()) {
            if (options.isVerbose()) {
                log.info(cfg.getAbsoluteFile() + " [ok]");
            }
            config = readConfig(locateConfigurationProvider(), new ExecutionModeProvider(execMode));
        } else {

            config = createConfig(chains, locateConfigurationProvider(),

                    new ExecutionModeProvider(execMode));
        }
        try {
            config.executeModuleChains(options, command, chains);
        } catch (Exception e) {
            System.setProperty("user.dir", userDir);
            if (options.isVerbose()) {
                if (!options.isPrintErrors()) {
                    log.error(
                            cfg.getAbsolutePath() + " is invalid. Please, execute walkmod with -e to see the details.");
                } else {
                    log.error("Invalid configuration", e);
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

        config.execute(userDir, options, chains);
        result.addAll(Summary.getInstance().getWrittenFiles());
        System.setProperty("user.dir", userDir);

    }

    /**
     * Applies a list of transformation chains without updating the source files.
     * 
     * @param chains
     *            the list of applied transformation chains.
     * @throws InvalidConfigurationException
     *             if the walkmod configuration is invalid and it is working in no verbose mode.
     * @return The list of affected files.
     */
    public List<File> check(String... chains) throws InvalidConfigurationException {

        final List<File> result = new LinkedList<File>();

        run(result, new WalkmodCommand() {
            @Override
            public void execute(Options options, File executionDir, String... chains) throws Exception {
                WalkModFacade facade = new WalkModFacade(OptionsBuilder.options(options).executionDirectory(executionDir).build());

                result.addAll(facade.check(chains));
            }
        }, ExecutionModeEnum.CHECK, chains);

        return result;

    }

    /**
     * Initializes an empty walkmod configuration file
     * 
     * @throws Exception
     *             in case that the walkmod configuration file can't be created.
     */
    public void init() throws Exception {
        userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
        System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());

        if (!cfg.exists()) {

            ConfigurationManager manager = new ConfigurationManager(cfg, false, locateConfigurationProvider());

            try {

                manager.runProjectConfigurationInitializers();

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
     * @param before
     *            Decides which is the next chain to execute.
     * @throws Exception
     *             in case that the walkmod configuration file can't be read.
     */
    public void addChainConfig(ChainConfig chainCfg, boolean recursive, String before) throws Exception {
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

            cfgProvider.addChainConfig(chainCfg, recursive, before);
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
     *            chain identifier where the transformation will be appended. It can be null.
     * @param path
     *            the path where the transformation config will be applied if the chain does not
     *            exists or is null.
     * @param recursive
     *            if the transformation config is added recursively to all the submodules.
     * @param transformationCfg
     *            transformation configuration to add
     * @param order
     *            priority order
     * @param before
     *            defines which is the next chain to execute
     * @throws Exception
     *             in case that the walkmod configuration file can't be read.
     */
    public void addTransformationConfig(String chain, String path, boolean recursive,
            TransformationConfig transformationCfg, Integer order, String before) throws Exception {
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

            cfgProvider.addTransformationConfig(chain, path, transformationCfg, recursive, order, before);
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
     *            if the provider config is added recursively to all the submodules.
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
     *            if the plugin config is added recursively to all the submodules.
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
     * Downloads the list of declared plugins in the configuration file using Ivy. Ignores the
     * ConfigurationProvided if passed in the constructor.
     * 
     * @throws InvalidConfigurationException
     *             if the walkmod configuration is invalid and it is working in no verbose mode.
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

                            WalkModFacade facade = new WalkModFacade(OptionsBuilder.options(options).executionDirectory(aux).build());
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
     * Remove a list of transformations for an specific chain. If it is recursive, this removal
     * action is applied into all the submodules.
     * 
     * @param chain
     *            To remove the transformations. By default, it is the "default" chain.
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
     * 
     * @param chain
     *            Chain to apply the writer
     * @param type
     *            Writer type to set
     * @param path
     *            Writer path to set
     * @param recursive
     *            If to set the writer to all the submodules.
     * @param params
     *            Writer parameters
     * @throws Exception
     *             if the walkmod configuration file can't be read.
     */
    public void setWriter(String chain, String type, String path, boolean recursive, Map<String, String> params)
            throws Exception {
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

                cfgProvider.setWriter(chain, type, path, recursive, params);
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
     * 
     * @param chain
     *            Chain to apply the writer
     * @param type
     *            Reader type to set
     * @param path
     *            Reader path to set
     * @param recursive
     *            If to set the reader to all the submodules.
     * @param params
     *            Reader parameters
     * @throws Exception
     *             if the walkmod configuration file can't be read.
     */
    public void setReader(String chain, String type, String path, boolean recursive, Map<String, String> params)
            throws Exception {
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

                cfgProvider.setReader(chain, type, path, recursive, params);
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
     * 
     * @param pluginConfig
     *            Plugin configuration to remove.
     * @param recursive
     *            If it necessary to remove the plugin from all the submodules.
     * @throws Exception
     *             if the walkmod configuration file can't be read.
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
     * 
     * @param modules
     *            Module names to remove
     * @throws Exception
     *             if the walkmod configuration file can't be read.
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
     * 
     * @param providers
     *            Name of the configuration providers to remove.
     * @param recursive
     *            If it necessary to remove the plugin from all the submodules.
     * @throws Exception
     *             If the walkmod configuration file can't be read.
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
     * 
     * @return Configuration object representation of the config file.
     * @throws Exception
     *             If the walkmod configuration file can't be read.
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
     * 
     * @param chains
     *            Chain names to remove
     * @param recursive
     *            If it necessary to remove the chains from all the submodules.
     * @throws Exception
     *             If the walkmod configuration file can't be read.
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
     * 
     * @param plugin
     *            Plugin container of bean definitions.
     * @return List of bean definitions.
     */
    public List<BeanDefinition> inspectPlugin(PluginConfig plugin) {
        Configuration conf = new ConfigurationImpl();
        Collection<PluginConfig> plugins = new LinkedList<PluginConfig>();
        plugins.add(plugin);
        conf.setPlugins(plugins);
        ConfigurationManager manager = new ConfigurationManager(conf, false, locateConfigurationProvider());
        manager.executeConfigurationProviders();
        return conf.getAvailableBeans(plugin);
    }

    /**
     * Sets an specific parameter value into a bean.
     * 
     * @param param
     *            Parameter name
     * @param value
     *            Parameter value
     * @param type
     *            Bean type to set the parameter
     * @param category
     *            Bean category to set the parameter (walker, reader, transformation, writer)
     * @param name
     *            Bean name/alias to set the parameter
     * @param chain
     *            Bean chain to filter the beans to take into account
     * @param recursive
     *            If it necessary to set the parameter to all the submodules.
     * @throws Exception
     *             If the walkmod configuration file can't be read.
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

    /**
     * Adds a list of includes rules into a chain
     * 
     * @param chain
     *            Chain to apply the includes list
     * @param includes
     *            List of includes
     * @param recursive
     *            If it necessary to set the parameter to all the submodules.
     * @param setToReader
     *            If it is added into the reader includes list
     * @param setToWriter
     *            If it is added into the writer includes list
     */
    public void addIncludesToChain(String chain, List<String> includes, boolean recursive, boolean setToReader,
            boolean setToWriter) {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        if (cfg.exists()) {

            userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
            try {
                ConfigurationManager manager = new ConfigurationManager(cfg, false);
                manager.getProjectConfigurationProvider().addIncludesToChain(chain, includes, recursive, setToReader,
                        setToWriter);
            } catch (Exception e) {
                exception = e;
            } finally {
                System.setProperty("user.dir", userDir);
                updateMsg(startTime, exception);
            }
        }
    }

    /**
     * Adds a list of excludes rules into a chain
     * 
     * @param chain
     *            Chain to apply the excludes list
     * @param excludes
     *            List of excludes
     * @param recursive
     *            If it necessary to set the parameter to all the submodules.
     * @param setToReader
     *            If it is added into the reader includes list
     * @param setToWriter
     *            If it is added into the writer includes list
     */
    public void addExcludesToChain(String chain, List<String> excludes, boolean recursive, boolean setToReader,
            boolean setToWriter) {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        if (cfg.exists()) {

            userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
            try {
                ConfigurationManager manager = new ConfigurationManager(cfg, false);
                manager.getProjectConfigurationProvider().addExcludesToChain(chain, excludes, recursive, setToReader,
                        setToWriter);
            } catch (Exception e) {
                exception = e;
            } finally {
                System.setProperty("user.dir", userDir);
                updateMsg(startTime, exception);
            }
        }
    }

    /**
     * Removes a list of excludes rules into a chain
     * 
     * @param chain
     *            Chain to apply the excludes list
     * @param excludes
     *            List of excludes
     * @param recursive
     *            If it necessary to set the parameter to all the submodules.
     * @param setToReader
     *            If it is added into the reader includes list
     * @param setToWriter
     *            If it is added into the writer includes list
     */
    public void removeExcludesToChain(String chain, List<String> excludes, boolean recursive, boolean setToReader,
            boolean setToWriter) {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        if (cfg.exists()) {

            userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
            try {
                ConfigurationManager manager = new ConfigurationManager(cfg, false);
                manager.getProjectConfigurationProvider().removeExcludesFromChain(chain, excludes, recursive,
                        setToReader, setToWriter);
            } catch (Exception e) {
                exception = e;
            } finally {
                System.setProperty("user.dir", userDir);
                updateMsg(startTime, exception);
            }
        }
    }

    /**
     * Removes a list of excludes rules into a chain
     * 
     * @param chain
     *            Chain to apply the excludes list
     * @param includes
     *            List of includes
     * @param recursive
     *            If it necessary to set the parameter to all the submodules.
     * @param setToReader
     *            If it is added into the reader includes list
     * @param setToWriter
     *            If it is added into the writer includes list
     */
    public void removeIncludesToChain(String chain, List<String> includes, boolean recursive, boolean setToReader,
            boolean setToWriter) {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        if (cfg.exists()) {

            userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", options.getExecutionDirectory().getAbsolutePath());
            try {
                ConfigurationManager manager = new ConfigurationManager(cfg, false);
                manager.getProjectConfigurationProvider().removeIncludesFromChain(chain, includes, recursive,
                        setToReader, setToWriter);
            } catch (Exception e) {
                exception = e;
            } finally {
                System.setProperty("user.dir", userDir);
                updateMsg(startTime, exception);
            }
        }
    }

}
