package it.si3p.supwsd.mns;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import it.si3p.supwsd.data.Lexel;


/**
 * @author papandrea
 *
 */
public class MNS {

	private final Map<String, List<String>> mNames;
	private final String mFile;

	MNS(String file) throws IOException {

		mFile = file;
		mNames = new HashMap<String, List<String>>();
	}

	public void load() throws Exception {

		String line, id, sense;
		List<String> names;
		StringTokenizer tokenizer;

		try (BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(mFile)))){

			while ((line = reader.readLine()) != null) {

				tokenizer = new StringTokenizer(line);
			
				if (tokenizer.countTokens() > 1) {

					id = tokenizer.nextToken();
					names = new ArrayList<String>();

					while (tokenizer.hasMoreTokens()) {
						
						sense=tokenizer.nextToken();
						
						if(!sense.equals("U"))
							names.add(sense);
					}
					
					if(!names.isEmpty())
						mNames.put(id, names);
				}
			}
		}
	}

	public List<String> resolve(Lexel lexel, String pos) {

		List<String> names;
		String id;

		id = lexel.getID();
		
		if (mNames.containsKey(id))
			names = get(id);
		else
			names = Arrays.asList(new String[] { lexel.toString() });

		return names;
	}

	final boolean isEnabled(){
		
		return this.mFile!=null;
	}
	
	protected List<String> get(String id) {

		return mNames.get(id);
	}

	public void unload() {

	}
}
