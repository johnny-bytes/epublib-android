package nl.siegmann.epublib.epub;

import nl.siegmann.epublib.domain.Resource;
import java.io.OutputStream;

@SuppressWarnings("unused")
public interface HtmlProcessor {

  void processHtmlResource(Resource resource, OutputStream out);
}
