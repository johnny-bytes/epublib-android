package nl.siegmann.epublib;


import static org.junit.Assert.assertNotNull;

import com.johnnybytes.epublib.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.io.InputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

@RunWith(RobolectricTestRunner.class)
public class EpubReaderTest {

    @Test
    public void testProblematicEpub() throws IOException {

//        InputStream is = RuntimeEnvironment.getApplication().getResources().openRawResource(R.raw.test_572250); // Add file to raw resources in main
//
//        EpubReader epubReader = new EpubReader();
//        Book book = epubReader.readEpub(is);
//
//        Resource test = book.getCoverImage();
//        assertNotNull(book);
    }

}