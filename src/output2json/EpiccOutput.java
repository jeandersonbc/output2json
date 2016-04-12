package output2json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import output2json.JsonObject.FieldsLabel;

public class EpiccOutput implements Convertable {

	private File file;

	public EpiccOutput(String path) {
		this.file = new File(path);
	}

	public List<JsonObject> convert() {
		ArrayList<JsonObject> jsonObjs = new ArrayList<JsonObject>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(this.file));
			while (br.ready()) {
				String currentLine = br.readLine();
				String currentScope = null;

				// Pattern Example: " - ..."
				if (Pattern.matches("( )+\\-(.)*", currentLine)) {
					String withoutParamList = normalizeEntityName(currentLine);
					currentScope = withoutParamList;
					currentLine = br.readLine();

					// Number of possible values for intent
					if (Pattern.matches("(.)+: \\d(.)+", currentLine)) {
						int intentValues = Integer.parseInt(currentLine.split(" ")[2]);
						for (int i = 0; i < intentValues; i++) {
							String possibleValuesLine = br.readLine();
							jsonObjs.add(convertFrom(currentScope, possibleValuesLine));
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while processing lines from " + this.file.getName());

		}
		return jsonObjs;
	}

	private JsonObject convertFrom(String scope, String valuesLine) {
		JsonObject json = new JsonObject();
		json.add(FieldsLabel.SCOPE, scope);

		// FIXME: breaks on cases like "Package: com/commonsware/android/arXiv,
		// Class: com/commonsware/android/arXiv/SearchListWindow, Extras:
		// [keyquery, keyname, keyurl], "
		valuesLine = "Scope: " + scope + ", "  + valuesLine;
		String values = valuesLine.replaceAll("( )+", " ");
		System.out.println(values);
//		for (String value : values) {
//			if (Pattern.matches("Action:(.)+", value)) {
//				json.add(FieldsLabel.ACTION, value.replaceAll("Action:", ""));
//
//			} else if (Pattern.matches("Package:(.)+", value)) {
//				json.add(FieldsLabel.COMPONENT, normalizeEntityName(value.replaceAll("Package:", "")));
//			} else if (Pattern.matches("Class:(.)+", value)) {
//				String className = value.replaceAll("(.)+/", "");
//				json.add(FieldsLabel.COMPONENT, className + ".class");
//			} else if (Pattern.matches("Flags:(.)+", value)) {
//				json.add(FieldsLabel.FLAGS, value);
//			}
//		}
		return json;
	}

	/**
	 * Normalize entity names:
	 * 
	 * 
	 * <ul>
	 * <li>Before:</li> <blockquote>
	 * <code> - com/commonsware/android/arXiv/HistoryWindow/onListItemClick(Landroid/
	 * widget/ListView;Landroid/view/View;IJ)</code> </blockquote>
	 * <li>After:</li> <blockquote>
	 * <code>com.commonsware.android.arXiv.HistoryWindow.onListItemClick</code>
	 * </ul>
	 * </blockquote>
	 * 
	 * @param unformedName
	 * @return A normalized name.
	 */
	private String normalizeEntityName(String unformedName) {
		String withoutBeginningDash = unformedName.replaceAll("( )+\\-( )+", "");
		String canonicalNotation = withoutBeginningDash.replace("/", ".");
		String withoutParamList = canonicalNotation.replaceAll("\\((.)*\\)", "");
		return withoutParamList;
	}

}
