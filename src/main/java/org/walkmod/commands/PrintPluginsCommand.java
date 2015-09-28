package org.walkmod.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.walkmod.exceptions.WalkModException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "shows the available walkmod plugins created by the community")
public class PrintPluginsCommand implements Command {

	public final static String MVN_SEARCH_URL = "https://search.maven.org/solrsearch/select?q=walkmod&&rows=50&&wt=json";

	public final static String ARTIFACT_DETAILS_URL = "https://search.maven.org/remotecontent?filepath=";

	public void execute() {
		try {
			URL searchURL = new URL(MVN_SEARCH_URL);
			InputStream is = null;
			try {
				is = searchURL.openStream();

				String content = readInputStreamAsString(is);
				DefaultJSONParser parser = new DefaultJSONParser(content);
				JSONObject object = parser.parseObject();
				parser.close();

				JSONArray artifactList = (object.getJSONObject("response")).getJSONArray("docs");

				for (int i = 0; i < artifactList.size(); i++) {
					JSONObject artifact = artifactList.getJSONObject(i);
					String artifactId = artifact.getString("a");
					if (artifactId.startsWith("walkmod-") && artifactId.endsWith("-plugin")) {
						String groupId = artifact.getString("g");
						String latestVersion = artifact.getString("latestVersion");
						String pom = artifactId + "-" + latestVersion + ".pom";
						String directory = groupId.replaceAll("\\.", "/");

						URL artifactDetailsURL = new URL(ARTIFACT_DETAILS_URL + directory + "/" + artifactId + "/"
								+ latestVersion + "/" + pom);

						InputStream projectIs = artifactDetailsURL.openStream();
						// String content = readInputStreamAsString(projectIs);
						try {
							DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
							Document doc = dBuilder.parse(projectIs);

							NodeList nList = doc.getElementsByTagName("description");
							String description = "unavailable description";
							if (nList.getLength() == 1) {
								description = nList.item(0).getTextContent();
							}
							System.out.println();
							System.out.println(groupId + ":" + artifactId + ":" + latestVersion + " => " + description);
							System.out.println();
						} finally {
							projectIs.close();
						}
					}
				}
			} finally {
				is.close();
			}

		} catch (Exception e) {
			throw new WalkModException("Invalid plugins URL", e);
		}
	}

	private String readInputStreamAsString(InputStream is) throws IOException {
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
