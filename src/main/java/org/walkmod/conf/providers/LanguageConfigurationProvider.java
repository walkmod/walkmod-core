package org.walkmod.conf.providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.MergePolicyConfigImpl;
import org.walkmod.util.DomHelper;
import org.xml.sax.InputSource;

public class LanguageConfigurationProvider implements ConfigurationProvider {

    /**
	 * Configuration file.
	 */
    private String suffixFileName;

    /**
	 * Error if configuration file is not found.
	 */
    private boolean errorIfMissing;

    /**
	 * Loaded configuration
	 */
    private Configuration configuration;

    /**
	 * Set of supported versions of dtdMappings
	 */
    private Map<String, String> dtdMappings;

    private Document document;

    private static final String DEFAULT_MERGE_ENGINE_NAME = "default";

    private static final Log LOG = LogFactory.getLog(LanguageConfigurationProvider.class);

    public LanguageConfigurationProvider() {
        this("defaults.xml", true);
    }

    public LanguageConfigurationProvider(String suffixFileName, boolean errorIfMissing) {
        this.suffixFileName = suffixFileName;
        this.errorIfMissing = errorIfMissing;
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("-//WALKMOD//WalkMod 1.0//EN", "walkmod-lang-1.0.dtd");
        setDtdMappings(mappings);
    }

    public void setDtdMappings(Map<String, String> mappings) {
        this.dtdMappings = Collections.unmodifiableMap(mappings);
    }

    public Map<String, String> getDtdMappings() {
        return dtdMappings;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = configuration;
        this.document = lookUpDocument();
    }

    private Document lookUpDocument() {
        Document doc = null;
        URL url = null;
        if (configuration == null) {
            throw new ConfigurationException("Missing default values configuration");
        }
        String defaults = configuration.getDefaultLanguage();
        String fileName;
        if (defaults == null) {
            fileName = "default-config.xml";
        } else {
            fileName = "META-INF/walkmod/walkmod-" + defaults + "-" + suffixFileName;
        }
        File f = new File(fileName);
        if (f.exists()) {
            try {
                url = f.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new ConfigurationException("Unable to load " + fileName, e);
            }
        }
        if (url == null) {
            url = configuration.getClassLoader().getResource(fileName);
        }
        InputStream is = null;
        if (url == null) {
            if (errorIfMissing) {
                throw new ConfigurationException("Could not open files of the name " + fileName);
            } else {
                LOG.info("Unable to locate default values configuration of the name " + f.getName() + ", skipping");
                return doc;
            }
        }
        try {
            is = url.openStream();
            InputSource in = new InputSource(is);
            in.setSystemId(url.toString());
            doc = DomHelper.parse(in, dtdMappings);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load " + fileName, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LOG.error("Unable to close input stream", e);
            }
        }
        if (doc != null) {
            LOG.debug("Walkmod configuration parsed");
        }
        return doc;
    }

    @Override
    public void load() throws ConfigurationException {
        updateNulls();
        loadMergePolicies();
    }

    private void updateNulls() {
        Collection<ChainConfig> ccs = configuration.getChainConfigs();
        Element rootElement = document.getDocumentElement();
        if (ccs != null) {
            for (ChainConfig cc : ccs) {
                ReaderConfig rc = cc.getReaderConfig();
                if (rc.getType() == null) {
                    rc.setType(rootElement.getAttribute("reader"));
                }
                if (rc.getPath() == null) {
                    rc.setPath(rootElement.getAttribute("path"));
                }
                WriterConfig wc = cc.getWriterConfig();
                if (wc.getType() == null) {
                    wc.setType(rootElement.getAttribute("writer"));
                }
                if (wc.getPath() == null) {
                    wc.setPath(rootElement.getAttribute("path"));
                }
                WalkerConfig walkc = cc.getWalkerConfig();
                if (walkc.getType() == null) {
                    walkc.setType(rootElement.getAttribute("walker"));
                }
                if (walkc.getParserConfig().getType() == null) {
                    if (!"".equals(rootElement.getAttribute("parser"))) {
                        walkc.getParserConfig().setType(rootElement.getAttribute("parser"));
                    }
                }
                List<TransformationConfig> transformations = walkc.getTransformations();
                if (transformations != null) {
                    for (TransformationConfig tc : transformations) {
                        if (tc.isMergeable()) {
                            if (tc.getMergePolicy() == null) {
                                tc.setMergePolicy(DEFAULT_MERGE_ENGINE_NAME);
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadMergePolicies() {
        Element rootElement = document.getDocumentElement();
        NodeList children = rootElement.getChildNodes();
        int childSize = children.getLength();
        Collection<MergePolicyConfig> mergePolicies = configuration.getMergePolicies();
        if (mergePolicies == null) {
            mergePolicies = new LinkedList<MergePolicyConfig>();
            configuration.setMergePolicies(mergePolicies);
        }
        MergePolicyConfig policy = null;
        for (int j = 0; j < childSize; j++) {
            Node childNode = children.item(j);
            if ("policy".equals(childNode.getNodeName())) {
                Element policyElem = (Element) childNode;
                policy = new MergePolicyConfigImpl();
                policy.setName(DEFAULT_MERGE_ENGINE_NAME);
                String defaultOP = policyElem.getAttribute("default-object-policy");
                if (!"".equals(defaultOP.trim())) {
                    policy.setDefaultObjectPolicy(defaultOP);
                } else {
                    policy.setDefaultObjectPolicy(null);
                }
                String defaultTP = policyElem.getAttribute("default-type-policy");
                if (!"".equals(defaultTP)) {
                    policy.setDefaultTypePolicy(defaultTP);
                } else {
                    policy.setDefaultTypePolicy(null);
                }
                NodeList entriesNodes = policyElem.getChildNodes();
                int entriesSize = entriesNodes.getLength();
                Map<String, String> policyEntries = new HashMap<String, String>();
                policy.setPolicyEntries(policyEntries);
                for (int k = 0; k < entriesSize; k++) {
                    Node entry = entriesNodes.item(k);
                    if ("policy-entry".equals(entry.getNodeName())) {
                        Element entryElem = (Element) entry;
                        String otype = entryElem.getAttribute("object-type");
                        String ptype = entryElem.getAttribute("policy-type");
                        if (!("".equals(otype.trim())) && !("".equals(ptype.trim()))) {
                            policyEntries.put(otype, ptype);
                        }
                    }
                }
            }
        }
        if (policy != null) {
            mergePolicies.add(policy);
        }
    }
}
