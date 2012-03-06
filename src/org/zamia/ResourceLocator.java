package org.zamia;

import java.io.IOException;
import java.net.URL;

/**
 * @author Anton Chepurov
 */
public interface ResourceLocator {

	URL resolve(URL aUrl) throws IOException;
}
