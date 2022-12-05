package nl.siegmann.epublib.epub;

import android.util.Log;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.MediaTypes;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.util.ResourceUtil;
import nl.siegmann.epublib.util.StringUtil;

/**
 * Reads an epub file.
 *
 * @author paul
 */
public class EpubReader {

    private static final String TAG = EpubReader.class.getName();
    private final BookProcessor bookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR;

    public Book readEpub(InputStream in) throws IOException {
        return readEpub(in, Constants.CHARACTER_ENCODING);
    }

    @SuppressWarnings("unused")
    public Book readEpub(ZipArchiveInputStream in) throws IOException {
        return readEpub(in, Constants.CHARACTER_ENCODING);
    }

    @SuppressWarnings("unused")
    public Book readEpub(ZipFile zipfile) throws IOException {
        return readEpub(zipfile, Constants.CHARACTER_ENCODING);
    }

    /**
     * Read epub from inputstream
     *
     * @param in       the inputstream from which to read the epub
     * @param encoding the encoding to use for the html files within the epub
     * @return the Book as read from the inputstream
     * @throws IOException IOException
     */
    public Book readEpub(InputStream in, String encoding) throws IOException {
        return readEpub(new ZipArchiveInputStream(in, encoding, false, true), encoding);
    }


    /**
     * Reads this EPUB without loading any resources into memory.
     *
     * @param zipFile  the file to load
     * @param encoding the encoding for XHTML files
     * @return this Book without loading all resources into memory.
     * @throws IOException IOException
     */
    @SuppressWarnings("unused")
    public Book readEpubLazy(ZipFile zipFile, String encoding)
            throws IOException {
        return readEpubLazy(zipFile, encoding,
                Arrays.asList(MediaTypes.mediaTypes));
    }

    public Book readEpub(ZipArchiveInputStream in, String encoding) throws IOException {
        return readEpub(ResourcesLoader.loadResources(in, encoding));
    }

    public Book readEpub(ZipFile in, String encoding) throws IOException {
        return readEpub(ResourcesLoader.loadResources(in, encoding));
    }

    /**
     * Reads this EPUB without loading all resources into memory.
     *
     * @param zipFile         the file to load
     * @param encoding        the encoding for XHTML files
     * @param lazyLoadedTypes a list of the MediaType to load lazily
     * @return this Book without loading all resources into memory.
     * @throws IOException IOException
     */
    public Book readEpubLazy(ZipFile zipFile, String encoding,
                             List<MediaType> lazyLoadedTypes) throws IOException {
        Resources resources = ResourcesLoader
                .loadResources(zipFile, encoding, lazyLoadedTypes);
        return readEpub(resources);
    }

    public Book readEpub(Resources resources) {
        return readEpub(resources, new Book());
    }

    public Book readEpub(Resources resources, Book result) {
        if (result == null) {
            result = new Book();
        }
        handleMimeType(result, resources);
        String packageResourceHref = getPackageResourceHref(resources);
        Resource packageResource = processPackageResource(packageResourceHref,
                result, resources);
        result.setOpfResource(packageResource);
        Resource ncxResource = processNcxResource(packageResource, result);
        result.setNcxResource(ncxResource);
        result = postProcessBook(result);
        return result;
    }


    private Book postProcessBook(Book book) {
        if (bookProcessor != null) {
            book = bookProcessor.processBook(book);
        }
        return book;
    }

    private Resource processNcxResource(Resource packageResource, Book book) {
        Log.d(TAG, "OPF:getHref()" + packageResource.getHref());
        if (book.isEpub3()) {
            return NCXDocumentV3.read(book, this);
        } else {
            return NCXDocumentV2.read(book, this);
        }

    }

    private Resource processPackageResource(String packageResourceHref, Book book,
                                            Resources resources) {
        Resource packageResource = resources.remove(packageResourceHref);
        try {
            PackageDocumentReader.read(packageResource, this, book, resources);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return packageResource;
    }

    private String getPackageResourceHref(Resources resources) {
        String defaultResult = "OEBPS/content.opf";
        String result = defaultResult;

        Resource containerResource = resources.remove("META-INF/container.xml");
        if (containerResource == null) {
            return result;
        }
        try {
            Document document = ResourceUtil.getAsDocument(containerResource);
            Element rootFileElement = (Element) ((Element) document
                    .getDocumentElement().getElementsByTagName("rootfiles").item(0))
                    .getElementsByTagName("rootfile").item(0);
            result = rootFileElement.getAttribute("full-path");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (StringUtil.isBlank(result)) {
            result = defaultResult;
        }
        return result;
    }

    @SuppressWarnings("unused")
    private void handleMimeType(Book result, Resources resources) {
        resources.remove("mimetype");
        //result.setResources(resources);
    }
}
