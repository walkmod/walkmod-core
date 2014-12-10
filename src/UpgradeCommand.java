package org.walkmod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.walkmod.exceptions.WalkModException;

public class UpgradeCommand {

	public final static String UPDATE_URL = "latest-walkmod.zip";
	
	public final static String WALKMOD_HOME = "WALKMOD_HOME";
	
	public final static String LIB_DIR = "lib";

	public void execute() {

		String installationDirectory = System.getenv(WALKMOD_HOME);
		if (installationDirectory == null) {
			throw new WalkModException(
					"Please, define set the environment variable ${"+WALKMOD_HOME+"} with your walkmod installation directory");
		}

		File walkmodHome = new File(installationDirectory);
		boolean canUpgrade = walkmodHome.exists();

		if (!canUpgrade) {
			throw new WalkModException(
					"The ${"+WALKMOD_HOME+"} directory does not exist");
		}
		canUpgrade = walkmodHome.canRead() && walkmodHome.canWrite();
		if (!canUpgrade) {
			throw new WalkModException(
					"Invalid permissions for upgrading the ${WALKMOD_HOME} directory. Please give write & read grants");
		}

		URL downloadURL = null;
		try {
			downloadURL = new URL(UPDATE_URL);
		} catch (MalformedURLException e) {
			throw new WalkModException(UPDATE_URL + ": is invalid", e);
		}

		URLConnection connection = null;
		try {

			connection = downloadURL.openConnection();

		} catch (IOException e) {
			throw new WalkModException(UPDATE_URL
					+ " is unreachable. Please, verify your connection", e);
		}
		try {

			InputStream stream = connection.getInputStream();
			File output = File.createTempFile("walkmod-last-version", ".zip");
			try {
				FileUtils.copyInputStreamToFile(stream, output);
			} finally {
				stream.close();
			}
			ZipFile zip = new ZipFile(output);
			ZipEntry entry = zip.getEntry(LIB_DIR);
			InputStream is = zip.getInputStream(entry);

			File tmp = new File(walkmodHome, "tmp");
			if (tmp.exists()) {
				tmp.delete();
			}
			tmp.mkdir();
			try {
				FileUtils.copyInputStreamToFile(is, tmp);
			} finally {
				is.close();
			}
			File lib = new File(walkmodHome, LIB_DIR);
			lib.delete();
			tmp.renameTo(lib);

		} catch (IOException e) {
			throw new WalkModException(UPDATE_URL
					+ " is unreachable. Please, verify your connection", e);
		}
	}
}
