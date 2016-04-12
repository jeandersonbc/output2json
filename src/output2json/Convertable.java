package output2json;

import java.io.IOException;
import java.util.List;

public interface Convertable {

	List<JsonObject> convert() throws IOException;

}
