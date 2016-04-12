package output2json;

import java.util.HashMap;
import java.util.Map;

public class JsonObject {

	public enum FieldsLabel {
		SCOPE("scope"), METHOD_TYPE("methodType"), IDENTIFIER("identifier"), COMPONENT("component"), ACTION(
				"action"), DATA("data"), MIME_TYPE("mimeType"), CATEGORY("category"), FLAGS("flags"), EXTRAS("extras");

		private String label;

		private FieldsLabel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private Map<FieldsLabel, String> fieldsMap;
	private final static String DEFAULT_VALUE = "-";
	private StringBuilder builder;

	public JsonObject() {
		this.builder = new StringBuilder();
		this.fieldsMap = new HashMap<>();
		for (FieldsLabel label : FieldsLabel.values()) {
			this.fieldsMap.put(label, DEFAULT_VALUE);
		}

	}

	public JsonObject add(FieldsLabel label, String value) {
		String previousContent = this.fieldsMap.put(label, value);
		if (!DEFAULT_VALUE.equals(previousContent)) {
			System.err.println(String.format("\nPREVIOUS CONTENT WAS OVERWRITTEN FOR %s!\n\tOld: %s\n\tNew: %s\n",
					label, previousContent, value));
		}
		return this;
	}

	@Override
	public String toString() {
		builder.append("\n{");
		FieldsLabel[] labels = FieldsLabel.values();

		FieldsLabel key = null;
		for (int i = 0; i < labels.length - 1; i++) {
			key = labels[i];
			builder.append("\n    \"" + key.toString() + "\": \"").append(this.fieldsMap.get(key)).append("\",");
		}
		key = labels[labels.length - 1];
		builder.append("\n    \"" + key.toString() + "\": \"").append(this.fieldsMap.get(key)).append("\"");

		return builder.append("\n}").toString();
	}
}
