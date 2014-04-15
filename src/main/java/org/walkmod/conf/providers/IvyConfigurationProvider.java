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
package org.walkmod.conf.providers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.matcher.ExactPatternMatcher;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;

public class IvyConfigurationProvider implements ConfigurationProvider {

	private Configuration configuration;

	private boolean isOffLine;

	private Ivy ivy = null;

	private File ivyfile;

	private ResolveOptions resolveOptions;

	private DefaultModuleDescriptor md;

	private static final String IVY_SETTINGS_FILE = "ivysettings.xml";

	public IvyConfigurationProvider() {
		this(false);
	}

	public IvyConfigurationProvider(boolean isOffLine) {
		setOffLine(isOffLine);
	}

	@Override
	public void init(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Ivy configuration initialization
	 * 
	 * @throws ConfigurationException
	 *             if ivy settings file (ivysettings.xml) is not found in
	 *             classpath
	 * */
	public void initIvy() throws URISyntaxException, ParseException,
			IOException, ConfigurationException {
		// creates clear ivy settings
		IvySettings ivySettings = new IvySettings();
		File settingsFile = new File(IVY_SETTINGS_FILE);
		if (settingsFile.exists()) {
			ivySettings.load(settingsFile);
		} else {
			URL settingsURL = ClassLoader.getSystemResource(IVY_SETTINGS_FILE);
			if (settingsURL == null) {
				// file not found in System classloader, we try the current one
				settingsURL = this.getClass().getClassLoader()
						.getResource(IVY_SETTINGS_FILE);
				// extra validation to avoid uncontrolled NullPointerException
				// when invoking toURI()
				if (settingsURL == null)
					throw new ConfigurationException("Ivy settings file ("
							+ IVY_SETTINGS_FILE
							+ ") could not be found in classpath");
			}
			ivySettings.load(settingsURL);
		}
		// creates an Ivy instance with settings
		ivy = Ivy.newInstance(ivySettings);

		ivyfile = File.createTempFile("ivy", ".xml");
		ivyfile.deleteOnExit();

		String[] confs = new String[] { "default" };
		resolveOptions = new ResolveOptions().setConfs(confs);
		if (isOffLine) {
			resolveOptions = resolveOptions.setUseCacheOnly(true);
		} else {
			Map<String, Object> params = configuration.getParameters();
			if (params != null) {
				Object value = params.get("offline");
				if (value != null) {
					String offlineOpt = value.toString();
					if (offlineOpt != null) {
						boolean offline = Boolean.parseBoolean(offlineOpt);
						if (offline) {
							resolveOptions = resolveOptions
									.setUseCacheOnly(true);
						}
					}
				}
			}
		}
	}

	@Override
	public void load() throws ConfigurationException {
		Collection<PluginConfig> plugins = configuration.getPlugins();
		PluginConfig plugin = null;
		Collection<File> jarsToLoad = new LinkedList<File>();
		try {
			if (plugins != null) {
				Iterator<PluginConfig> it = plugins.iterator();
				initIvy();
				while (it.hasNext()) {
					plugin = it.next();
					addArtifact(plugin.getGroupId(), plugin.getArtifactId(),
							plugin.getVersion());

				}
				jarsToLoad = resolveArtifacts();
				URL[] urls = new URL[jarsToLoad.size()];
				int i = 0;
				for (File jar : jarsToLoad) {
					urls[i] = jar.toURI().toURL();
					i++;
				}
				URLClassLoader childClassLoader = new URLClassLoader(urls,
						configuration.getClassLoader());
				configuration.setClassLoader(childClassLoader);
			}
		} catch (ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			if (plugin == null) {
				throw new ConfigurationException(
						"Unable to initialize ivy configuration", e);
			} else {
				throw new ConfigurationException(
						"Unable to resolve the plugin: " + plugin.getGroupId()
								+ " : " + plugin.getArtifactId() + " : "
								+ plugin.getVersion(), e);
			}
		}
	}

	public void addArtifact(String groupId, String artifactId, String version)
			throws Exception {
		String[] dep = null;
		dep = new String[] { groupId, artifactId, version };
		if (md == null) {
			md = DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId
					.newInstance(dep[0], dep[1] + "-caller", "working"));
		}
		DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
				ModuleRevisionId.newInstance(dep[0], dep[1], dep[2]), false,
				false, true);
		md.addDependency(dd);
		ExcludeRule er = new DefaultExcludeRule(new ArtifactId(new ModuleId(
				"org.walkmod", "walkmod-core"), PatternMatcher.ANY_EXPRESSION,
				PatternMatcher.ANY_EXPRESSION, PatternMatcher.ANY_EXPRESSION),
				ExactPatternMatcher.INSTANCE, null);
		dd.addExcludeRule(null, er);
	}

	public Collection<File> resolveArtifacts() throws Exception {

		if (ivy != null) {
			XmlModuleDescriptorWriter.write(md, ivyfile);
			ResolveReport report = ivy.resolve(ivyfile.toURL(), resolveOptions);
			ArtifactDownloadReport[] artifacts = report
					.getAllArtifactsReports();
			Collection<File> result = new LinkedList<File>();

			for (ArtifactDownloadReport item : artifacts) {
				result.add(item.getLocalFile());

			}
			return result;
		}
		return null;
	}

	public void setOffLine(boolean isOffLine) {
		this.isOffLine = isOffLine;
	}

	public boolean isOffLine() {
		return isOffLine;
	}
}
