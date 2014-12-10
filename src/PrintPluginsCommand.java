package org.walkmod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.exceptions.WalkModException;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class PrintPluginsCommand {

	public final static String MVN_SEARCH_URL = "https://search.maven.org/solrsearch/select?q=walkmod&wt=json";

	public final static String ARTIFACT_DETAILS_URL = "https://search.maven.org/remotecontent?filepath=";

	
	public void execute() {
		try {
			//TODO: mirar si sta el walkmod_home para mantener una cache de los plugins, sinó, se crea una carpeta .walkmod y se guarda alli (embedded)
			URL searchURL = new URL(MVN_SEARCH_URL);
			InputStream is = null;
			try {
				is = searchURL.openStream();
				
				DefaultJSONParser parser = new DefaultJSONParser(readInputStreamAsString(is));
				List<JSONObject> artifactList = new LinkedList<JSONObject>();
				parser.parseArray(artifactList, "response.docs");
				parser.close();

				for (JSONObject artifact : artifactList) {
					String artifactId = artifact.getString("a");
					if (artifactId.startsWith("walkmod-")
							&& artifactId.endsWith("-plugin")) {
						String groupId = artifact.getString("g");
						String latestVersion = artifact
								.getString("latestVersion");
						String pom = artifactId + "-" + latestVersion + ".pom";
						String directory = groupId.replaceAll(".", "/");

						URL artifactDetailsURL = new URL(ARTIFACT_DETAILS_URL
								+ directory + "/" + artifactId + "/"
								+ latestVersion + "/" + pom);
						
						is = artifactDetailsURL.openStream();
						String content = readInputStreamAsString(is);
						//TODO: parsear content
					}
				}
			} finally {
				is.close();
			}

		} catch (Exception e) {
			throw new WalkModException("Invalid plugins URL", e);
		}
	}
	
	private String readInputStreamAsString(InputStream is) throws IOException{
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		String line = br.readLine();
		String response = "";
		while (line != null) {
			response += line + '\n';
			line = br.readLine();
		}
		return response;
	}

}
