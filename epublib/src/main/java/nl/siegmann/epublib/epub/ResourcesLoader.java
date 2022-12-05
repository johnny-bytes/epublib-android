package nl.siegmann.epublib.epub;

import android.util.Log;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipException;

import nl.siegmann.epublib.domain.LazyResourceProvider;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.MediaTypes;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.util.CollectionUtil;
import nl.siegmann.epublib.util.ResourceUtil;


/**
 * Loads Resources from inputStreams, ZipFiles, etc
 *
 * @author paul
 */
public class ResourcesLoader {

    private static final String TAG = ResourcesLoader.class.getName();


    /**
     * Loads the entries of the zipFile as resources.
     * <p>
     * The MediaTypes that are in the lazyLoadedTypes will not get their
     * contents loaded, but are stored as references to entries into the
     * ZipFile and are loaded on demand by the Resource system.
     *
     * @param zipFile             import epub zipfile
     * @param defaultHtmlEncoding epub xhtml default encoding
     * @param lazyLoadedTypes     lazyLoadedTypes
     * @return Resources
     * @throws IOException IOException
     */
    public static Resources loadResources(ZipFile zipFile,
                                          String defaultHtmlEncoding,
                                          List<MediaType> lazyLoadedTypes) throws IOException {

        throw new IOException("Not yet implemented.");

//        LazyResourceProvider resourceProvider =
//                new EpubResourceProvider(zipFile.getName());
//
//        Resources result = new Resources();
//        Enumeration<? extends ZipEntry> entries = zipFile.entries();
//
//        while (entries.hasMoreElements()) {
//            ZipEntry zipEntry = entries.nextElement();
//
//            if (zipEntry == null || zipEntry.isDirectory()) {
//                continue;
//            }
//
//            String href = zipEntry.getName();
//
//            Resource resource;
//
//            if (shouldLoadLazy(href, lazyLoadedTypes)) {
//                resource = new LazyResource(resourceProvider, zipEntry.getSize(), href);
//            } else {
//                resource = ResourceUtil
//                        .createResource(zipEntry, zipFile.getInputStream(zipEntry));
//            }
//
//            if (resource.getMediaType() == MediaTypes.XHTML) {
//                resource.setInputEncoding(defaultHtmlEncoding);
//            }
//            result.add(resource);
//        }
//
//        return result;
    }

    /**
     * Whether the given href will load a mediaType that is in the
     * collection of lazilyLoadedMediaTypes.
     *
     * @param href                   href
     * @param lazilyLoadedMediaTypes lazilyLoadedMediaTypes
     * @return Whether the given href will load a mediaType that is
     * in the collection of lazilyLoadedMediaTypes.
     */
    private static boolean shouldLoadLazy(String href,
                                          Collection<MediaType> lazilyLoadedMediaTypes) {
        if (CollectionUtil.isEmpty(lazilyLoadedMediaTypes)) {
            return false;
        }
        MediaType mediaType = MediaTypes.determineMediaType(href);
        return lazilyLoadedMediaTypes.contains(mediaType);
    }

    /**
     * Loads all entries from the ZipInputStream as Resources.
     * <p>
     * Loads the contents of all ZipEntries into memory.
     * Is fast, but may lead to memory problems when reading large books
     * on devices with small amounts of memory.
     *
     * @param zipInputStream      zipInputStream
     * @param defaultHtmlEncoding defaultHtmlEncoding
     * @return Resources
     * @throws IOException IOException
     */
    public static Resources loadResources(ZipArchiveInputStream zipInputStream,
                                          String defaultHtmlEncoding) throws IOException {
        Resources result = new Resources();
        ArchiveEntry zipEntry;
        do {
            // get next valid zipEntry
            zipEntry = getNextZipEntry(zipInputStream);
            if ((zipEntry == null) || zipEntry.isDirectory()) {
                continue;
            }

            // store resource
            Resource resource = ResourceUtil.createResource(zipEntry, zipInputStream);
            if (resource.getMediaType() == MediaTypes.XHTML) {
                resource.setInputEncoding(defaultHtmlEncoding);
            }
            result.add(resource);
        } while (zipEntry != null);

        return result;
    }


    private static ArchiveEntry getNextZipEntry(ZipArchiveInputStream zipInputStream)
            throws IOException {
        try {
            return zipInputStream.getNextEntry();
        } catch (ZipException e) {
            //see <a href="https://github.com/psiegman/epublib/issues/122">Issue #122 Infinite loop</a>.
            //when reading a file that is not a real zip archive or a zero length file, zipInputStream.getNextEntry()
            //throws an exception and does not advance, so loadResources enters an infinite loop
            //log.error("Invalid or damaged zip file.", e);
            Log.e(TAG, e.getLocalizedMessage());
            try {
                zipInputStream.close();
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    /**
     * Loads all entries from the ZipInputStream as Resources.
     * <p>
     * Loads the contents of all ZipEntries into memory.
     * Is fast, but may lead to memory problems when reading large books
     * on devices with small amounts of memory.
     *
     * @param zipFile             zipFile
     * @param defaultHtmlEncoding defaultHtmlEncoding
     * @return Resources
     * @throws IOException IOException
     */
    public static Resources loadResources(ZipFile zipFile, String defaultHtmlEncoding) throws IOException {
        List<MediaType> ls = new ArrayList<>();
        return loadResources(zipFile, defaultHtmlEncoding, ls);
    }
}
