package output2json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

	public static void main(String[] args) throws IOException {
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();
		options.addOption("p", "path", true, "the path to input file");
		options.addOption("t", "type", true, "type of the input file: epicc");
		options.addOption("o", "output", true, "output file name");

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("p") && line.hasOption("t")) {
				File report = line.hasOption("o") ? new File(line.getOptionValue("o") + ".json")
						: File.createTempFile("output-epicc", ".json", new File("."));

				String type = line.getOptionValue("t");
				String path = line.getOptionValue("p");
				switch (type) {
				case "epicc":
					init(new EpiccOutput(path), report);
					break;
				default:
					formatter.printHelp("java -jar output2json -p FILE -t TYPE [-o FILE]", options);
					break;
				}

			} else {
				formatter.printHelp("java -jar output2json -p FILE -t TYPE [-o FILE]", options);
			}

		} catch (ParseException e) {
			System.err.println("Unexpected exception: " + e.getMessage());
		}

	}

	static void init(Convertable converter, File report) throws IOException {
		List<JsonObject> output = converter.convert();
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(report)));
		pw.write(output.toString());
		pw.close();
	}
}
