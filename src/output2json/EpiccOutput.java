package output2json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import output2json.JsonObject.FieldsLabel;

public class EpiccOutput implements Convertable {

	private File file;

	public EpiccOutput(String path) {
		this.file = new File(path);
	}

	public List<JsonObject> convert() throws IOException {
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

		} finally {
			if (br != null)
				br.close();
		}
		return jsonObjs;
	}

	private JsonObject convertFrom(String scope, String valuesLine) {
		JsonObject json = new JsonObject();
		json.add(FieldsLabel.SCOPE, scope);

		String values = valuesLine.replaceAll("( )+", " ");
		Scanner sc = new Scanner(values);
		while (sc.hasNext()) {
			String attribute = sc.next();
			if (Pattern.matches("Action:", attribute)) {
				String value = sc.next().replace(",", "");
				json.add(FieldsLabel.ACTION, value);

			} else if (Pattern.matches("Package:", attribute)) {
				String value = sc.next().replace(",", "");
				json.add(FieldsLabel.COMPONENT, normalizeEntityName(value.replaceAll(",", "")));

			} else if (Pattern.matches("Class:", attribute)) {
				String className = sc.next().replace(",", "").replaceAll("(.)+/", "");
				json.add(FieldsLabel.COMPONENT, className + ".class");

			} else if (Pattern.matches("Extras:", attribute)) {
				String extrasSequence = sc.skip("( )*\\[").useDelimiter("]").next();
				json.add(FieldsLabel.EXTRAS, extrasSequence);

				// Back to default delimiter so it won't disturb next
				// readings
				sc.reset();

			} else if (Pattern.matches("Flags:", attribute)) {
				json.add(FieldsLabel.FLAGS, sc.next());
			} else {
				System.err.printf("Warning: Ignored content \"%s\"\n", attribute);
			}
		}
		sc.close();

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
