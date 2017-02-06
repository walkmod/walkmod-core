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
package org.walkmod.conf.entities.impl;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.walkmod.ChainAdapter;
import org.walkmod.ChainAdapterFactory;
import org.walkmod.ChainReader;
import org.walkmod.ChainWalker;
import org.walkmod.ChainWriter;
import org.walkmod.Options;
import org.walkmod.WalkmodCommand;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.ExecutionModeEnum;
import org.walkmod.conf.Initializer;
import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.InitializerConfig;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.PropertyDefinition;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.impl.DefaultChainAdapterFactory;
import org.walkmod.merger.MergeEngine;
import org.walkmod.merger.MergePolicy;
import org.walkmod.walkers.VisitorMessage;
import org.walkmod.writers.Summary;

public class ConfigurationImpl implements Configuration {

    private Map<String, Object> parameters;

    private Map<String, ChainConfig> chainConfigs;

    private BeanFactory beanFactory;

    private Collection<PluginConfig> plugins;

    private ClassLoader classLoader = null;

    private Collection<MergePolicyConfig> mergePolicies;

    private Map<String, MergeEngine> mergeEngines;

    private String defaultLanguage;

    private Collection<ProviderConfig> providers;

    private List<InitializerConfig> initializers;

    private List<String> modules;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    private ExecutionModeEnum executionMode = ExecutionModeEnum.APPLY;

    public static Logger log = Logger.getLogger(ConfigurationImpl.class);

    public ConfigurationImpl() {
        this.parameters = new LinkedHashMap<String, Object>();
        this.chainConfigs = new LinkedHashMap<String, ChainConfig>();
        this.mergeEngines = new LinkedHashMap<String, MergeEngine>();

        this.beanFactory = null;
        this.defaultLanguage = null;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public Collection<ChainConfig> getChainConfigs() {
        return chainConfigs.values();
    }

    public void setChainConfigs(Collection<ChainConfig> chainConfigs) {

        this.chainConfigs.clear();
        Iterator<ChainConfig> it = chainConfigs.iterator();
        while (it.hasNext()) {
            ChainConfig current = it.next();
            current.setConfiguration(this);
            this.chainConfigs.put(current.getName(), current);
        }
    }

    public boolean addChainConfig(ChainConfig architecture) {
        boolean result = chainConfigs.containsKey(architecture.getName());
        if (!result) {
            architecture.setConfiguration(this);
            chainConfigs.put(architecture.getName(), architecture);

        }
        return result;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void preparePlugins() {
        Collection<PluginConfig> pluginCfg = getPlugins();
        HashSet<String> plugins = new HashSet<String>();

        HashSet<String> previousPlugins = new HashSet<String>();

        if (pluginCfg == null || pluginCfg.isEmpty()) {
            pluginCfg = new LinkedList<PluginConfig>();
        } else {
            for (PluginConfig pc : pluginCfg) {
                previousPlugins.add(pc.getGroupId() + ":" + pc.getArtifactId());
            }
        }
        Collection<ChainConfig> chains = getChainConfigs();
        if (chains != null) {

            Iterator<ChainConfig> it = chains.iterator();
            while (it.hasNext()) {
                ChainConfig cc = it.next();
                composeName(cc.getReaderConfig().getType(), plugins);
                composeName(cc.getWalkerConfig().getParserConfig().getType(), plugins);
                List<TransformationConfig> trans = cc.getWalkerConfig().getTransformations();
                if (trans != null) {
                    for (TransformationConfig transformation : trans) {
                        String type = transformation.getType();
                        if (!type.equals("script") && !type.equals("template")) {
                            composeName(type, plugins);
                        }
                    }
                }
                composeName(cc.getWriterConfig().getType(), plugins);
            }

        }
        Collection<ProviderConfig> providers = getProviderConfigurations();
        if (providers != null) {
            for (ProviderConfig provider : providers) {
                String type = provider.getType();
                composeName(type, plugins);
            }
        }

        Collection<InitializerConfig> initializers = getInitializers();
        if (initializers != null) {
            for (InitializerConfig initializer : initializers) {
                plugins.add(
                        initializer.getPluginGroupId() + ":walkmod-" + initializer.getPluginArtifactId() + "-plugin");
            }
        }

        for (String id : plugins) {
            if (!previousPlugins.contains(id)) {
                String[] parts = id.split(":");
                PluginConfig cfg = new PluginConfigImpl();
                cfg.setGroupId(parts[0].trim());
                cfg.setArtifactId(parts[1].trim());
                cfg.setVersion("latest.integration");
                pluginCfg.add(cfg);
            }
        }
        setPlugins(pluginCfg);

    }

    private void composeName(String type, HashSet<String> plugins) {
        if (type != null && !type.startsWith("walkmod:commons")) {
            String[] parts = type.split(":");
            if (parts.length == 3) {
                if (!parts[1].startsWith("walkmod-")) {
                    parts[1] = "walkmod-" + parts[1];
                }
                if (!parts[1].endsWith("-plugin")) {
                    parts[1] = parts[1] + "-plugin";
                }
                plugins.add(parts[0] + ":" + parts[1]);
            } else if (parts.length <= 2) {
                String aux = parts[0].trim();
                if (aux.length() > 0) {
                    plugins.add("org.walkmod:walkmod-" + aux + "-plugin");
                }
            }
        }
    }

    @Override
    public Object getBean(String name, Map<?, ?> parameters) {
        Object result = null;
        if (name == null || "".equals(name)) {
            return result;
        }
        if (name.equals("script")) {
            name = "walkmod:commons:scripting";
        } else if (name.equals("template")) {
            name = "walkmod:commons:template";
        }
        if (beanFactory != null && beanFactory.containsBean(name)) {
            result = beanFactory.getBean(name);
        }
        if (result == null) {
            String fullName = "org.walkmod:walkmod-" + name + "-plugin:" + name;
            if (!name.contains(":") && beanFactory.containsBean(fullName)) {
                result = beanFactory.getBean(fullName);
            } else {
                String[] parts = name.split(":");
                if (parts.length == 2) {
                    String pluginId = parts[0].trim();
                    String beanId = parts[1].trim();
                    String compositeName = "org.walkmod:walkmod-" + pluginId + "-plugin:" + beanId;
                    if (pluginId.length() > 0 && beanId.length() > 0 && beanFactory.containsBean(compositeName)) {
                        result = beanFactory.getBean(compositeName);
                    }
                }
            }
        }
        if (result == null) {
            try {
                Class<?> clazz = getClassLoader().loadClass(name);
                result = clazz.newInstance();
            } catch (Exception e) {
                throw new WalkModException(
                        "Sorry, it is impossible to load the bean " + name
                                + ". Please, assure that it is a valid class name and the library which contains it is in the classpath",
                        e);
            }
        }
        if (result != null) {
            BeanWrapper bw = new BeanWrapperImpl(result);
            if (this.parameters != null) {
                MutablePropertyValues pvs = new MutablePropertyValues(this.parameters);
                bw.setPropertyValues(pvs, true, true);
            }
            if (parameters != null) {
                MutablePropertyValues pvs = new MutablePropertyValues(parameters);
                bw.setPropertyValues(pvs, true, true);
            }
        }
        return result;
    }

    public void populate(Object element, Map<?, ?> parameters) {
        if (element != null) {
            BeanWrapper bw = new BeanWrapperImpl(element);
            if (this.parameters != null) {
                bw.setPropertyValues(this.parameters);
            }
            bw.setPropertyValues(parameters);
        }
    }

    public Collection<VisitorMessage> getVisitorMessages() {
        Collection<VisitorMessage> result = new LinkedList<VisitorMessage>();
        if (getChainConfigs() != null) {
            for (ChainConfig aqConfig : getChainConfigs()) {
                result.addAll(aqConfig.getWalkerConfig().getWalker().getVisitorMessages());
            }
        }
        return result;
    }

    @Override
    public Collection<PluginConfig> getPlugins() {

        return plugins;
    }

    @Override
    public void setPlugins(Collection<PluginConfig> plugins) {
        this.plugins = plugins;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Collection<MergePolicyConfig> getMergePolicies() {
        return mergePolicies;
    }

    @Override
    public void setMergePolicies(Collection<MergePolicyConfig> mergePolicies) {
        this.mergePolicies = mergePolicies;
    }

    @Override
    public void setMergeEngines(Map<String, MergeEngine> mergeEngines) {
        this.mergeEngines = mergeEngines;
    }

    @Override
    public MergeEngine getMergeEngine(String name) {
        return mergeEngines.get(name);
    }

    @Override
    public String getDefaultLanguage() {

        return defaultLanguage;
    }

    @Override
    public void setDefaultLanguage(String defaults) {
        this.defaultLanguage = defaults;
    }

    @Override
    public Collection<ProviderConfig> getProviderConfigurations() {
        return providers;
    }

    @Override
    public void setProviderConfigurations(Collection<ProviderConfig> providers) {
        this.providers = providers;
    }

    @Override
    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    @Override
    public List<String> getModules() {
        return modules;
    }

    @Override
    public void setInitializers(List<InitializerConfig> initializers) {
        this.initializers = initializers;
    }

    @Override
    public List<InitializerConfig> getInitializers() {
        return initializers;
    }

    @Override
    public boolean containsBean(String beanId) {
        if (beanFactory != null) {
            if (!beanId.contains(":")) {
                return beanFactory.containsBean("org.walkmod:walkmod-" + beanId + "-plugin:" + beanId);
            }
            return beanFactory.containsBean(beanId);
        }
        return false;
    }

    public List<BeanDefinition> getAvailableBeans(PluginConfig pc) {
        List<BeanDefinition> result = new LinkedList<BeanDefinition>();

        String[] names = beanDefinitionRegistry.getBeanDefinitionNames();
        if (names != null) {
            for (String name : names) {
                if (name.startsWith(pc.getGroupId() + ":" + pc.getArtifactId())) {
                    Object o = beanFactory.getBean(name);
                    String classification = "transformation";
                    if (o instanceof ChainReader) {
                        classification = "reader";
                    } else if (o instanceof ChainWriter) {
                        classification = "writer";
                    } else if (o instanceof ChainWalker) {
                        classification = "walker";
                    } else if (o instanceof Initializer) {
                        classification = "initializer";
                    } else if (o instanceof ConfigurationProvider) {
                        classification = "conf-provider";
                    } else if (o instanceof MergePolicy) {
                        classification = "policy-entry";
                    }

                    String id = name;
                    int index0 = "walkmod-".length();
                    int index1 = pc.getArtifactId().indexOf("-plugin");
                    if (index1 != -1) {
                        id = pc.getArtifactId().substring(index0, index1);
                        String uniqueFullName = pc.getGroupId() + ":" + pc.getArtifactId() + ":" + id;
                        if (!name.equals(uniqueFullName)) {
                            if (!beanDefinitionRegistry.isAlias(name)) {
                                String[] aliases = beanDefinitionRegistry.getAliases(name);
                                boolean add = true;
                                if (aliases != null) {
                                    for (int i = 0; i < aliases.length && add; i++) {
                                        add = !(aliases[i].equals(uniqueFullName));
                                    }
                                }
                                if (add) {
                                    result.add(new BeanDefinitionImpl(classification, name,
                                            beanDefinitionRegistry.getBeanDefinition(name).getDescription(),
                                            getProperties(o)));
                                } else {
                                    result.add(new BeanDefinitionImpl(classification, id,
                                            beanDefinitionRegistry.getBeanDefinition(name).getDescription(),
                                            getProperties(o)));
                                }
                            }
                        } else {
                            result.add(new BeanDefinitionImpl(classification, id,
                                    beanDefinitionRegistry.getBeanDefinition(name).getDescription(), getProperties(o)));
                        }
                    }

                }
            }
        }
        return result;
    }

    private List<PropertyDefinition> getProperties(Object o) {
        List<PropertyDefinition> result = new LinkedList<PropertyDefinition>();
        PropertyDescriptor[] properties = BeanUtils.getPropertyDescriptors(o.getClass());
        if (properties != null) {
            for (PropertyDescriptor pd : properties) {
                if (pd.getWriteMethod() != null) {
                    String name = pd.getDisplayName();
                    Class<?> clazz = pd.getPropertyType();
                    String type = clazz.getSimpleName();
                    String value = "";
                    if (String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
                            || clazz.isPrimitive()) {
                        if (pd.getReadMethod() != null) {
                            try {
                                value = pd.getReadMethod().invoke(o).toString();
                            } catch (Exception e) {
                            }
                        } else {
                            Field[] fields = o.getClass().getDeclaredFields();
                            boolean found = false;
                            for (int i = 0; i < fields.length && !found; i++) {
                                if (fields[i].getName().equals(name)) {
                                    found = true;
                                    fields[i].setAccessible(true);
                                    try {
                                        value = fields[i].get(o).toString();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                    }

                    PropertyDefinition item = new PropertyDefinitionImpl(type, name, value);

                    result.add(item);
                }
            }
        }
        return result;
    }

    @Override
    public void setBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public ExecutionModeEnum getExecutionMode() {

        return executionMode;
    }

    @Override
    public void setExecutionMode(ExecutionModeEnum executionMode) {
        this.executionMode = executionMode;

    }

    @Override
    public void executeModuleChains(Options options, WalkmodCommand command, String... chains) throws Exception {
        List<String> modules = getModules();
        if (modules != null && !modules.isEmpty()) {
            for (String module : modules) {
                File aux = new File(module).getAbsoluteFile();
                if (aux.isDirectory()) {
                    if (options.isVerbose()) {
                        log.info("** MODULE " + aux.getAbsoluteFile() + " [ok] **");
                    }
                    command.execute(options, aux, chains);

                } else {
                    log.error("The module " + aux.getAbsolutePath() + " is not an existing directory");
                }
            }
        }
    }

    public void executeAllChains(Options options, ChainAdapterFactory apf) {
        Collection<ChainConfig> tcgfs = getChainConfigs();

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

                    ChainAdapter ap = apf.createChainProxy(this, tcfg.getName());

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

                if (!it.hasNext()) {

                    if (tcgfs.size() >= pos) {
                        LinkedList<ChainConfig> aux = new LinkedList<ChainConfig>(tcgfs);
                        it = aux.listIterator(pos - 1);

                    }
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

    public void executeChain(String userDir, Options options, ChainAdapterFactory apf, String name) {
        if (options.getIncludes() != null || options.getExcludes() != null) {
            Collection<ChainConfig> chains = getChainConfigs();
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
        ChainAdapter ap = apf.createChainProxy(this, name);
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
                int size = getChainConfigs().size();
                ap.execute();

                //we check if some other chain config has been added and execute them
                if (getChainConfigs().size() > size) {
                    LinkedList<ChainConfig> aux = new LinkedList<ChainConfig>(getChainConfigs());
                    Iterator<ChainConfig> it = aux.listIterator(size);
                    while (it.hasNext()) {
                        ChainConfig tcfg = it.next();
                        ChainAdapter auxAp = apf.createChainProxy(this, tcfg.getName());
                        auxAp.execute();
                    }
                }

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

    @Override
    public void execute(String userDir, Options options, String... chains) {
        ChainAdapterFactory apf = new DefaultChainAdapterFactory();
        Summary.getInstance().clear();
        Collection<ChainConfig> chainCfgs = getChainConfigs();

        if (chainCfgs != null && !chainCfgs.isEmpty()) {

            if (chains == null || chains.length == 0) {

                executeAllChains(options, apf);
            } else {
                for (String chain : chains) {
                    executeChain(userDir, options, apf, chain);
                }
            }
        }
    }

    @Override
    public ChainConfig getChainConfig(String chainConfig) {

        return chainConfigs.get(chainConfig);
    }

    @Override
    public void prepareInitializers() {
        InitializerConfig init = null;
        File pom = new File("pom.xml").getAbsoluteFile();
        if (pom.exists()) {
            init = new InitializerConfigImpl();
            init.setType("maven-initializer");
        } else {
            File gradle = new File("settings.gradle");
            if (gradle.exists()) {
                init = new InitializerConfigImpl();
                init.setType("gradle-initializer");
            } else {
                gradle = new File("build.gradle");
                if (gradle.exists()) {
                    init = new InitializerConfigImpl();
                    init.setType("gradle-initializer");
                }
            }
        }
        if (init != null) {
            List<InitializerConfig> list = getInitializers();
            if (list == null) {
                list = new LinkedList<InitializerConfig>();
            }
            list.add(init);
            setInitializers(list);
        }

    }

}
