package output2json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException {
		String path = "input/epicc-result.txt";
		Convertable c = new EpiccOutput(path);
		List<JsonObject> output = c.convert();

		File report = File.createTempFile("output-epicc", ".json", new File("."));
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(report)));
		pw.write(output.toString());
		pw.close();
	}
}
