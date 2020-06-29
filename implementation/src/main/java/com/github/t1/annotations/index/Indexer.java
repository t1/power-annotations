package com.github.t1.annotations.index;

import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class Indexer {
    private static final Logger LOG = Logger.getLogger(Indexer.class.getName());

    static Index init() {
        try (InputStream inputStream = getClassLoader().getResourceAsStream("META-INF/jandex.idx")) {
            IndexView indexView = initFrom(inputStream);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("------------------------------------------------------------");
                indexView.getKnownClasses().forEach(classInfo ->
                    LOG.fine(classInfo.name() + " :: " + classInfo.classAnnotations().stream()
                        .map(Object::toString).collect(joining(", ")))
                );
                LOG.fine("------------------------------------------------------------");
            }
            return new Index(indexView);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read index file", e);
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (classLoader == null) ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    private static IndexView initFrom(InputStream inputStream) {
        if (inputStream == null)
            return new Indexer().build();
        try {
            return new IndexReader(inputStream).read();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read Jandex input stream", e);
        }
    }

    private final org.jboss.jandex.Indexer indexer = new org.jboss.jandex.Indexer();

    private IndexView build() {
        urls().distinct().forEach(this::index);
        return indexer.complete();
    }

    private Stream<URL> urls() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader instanceof URLClassLoader) {
            return Stream.of(((URLClassLoader) classLoader).getURLs());
        } else {
            return classPath().map(Indexer::toUrl);
        }
    }

    private Stream<String> classPath() {
        return Stream.of(System.getProperty("java.class.path").split(System.getProperty("path.separator")));
    }

    private static URL toUrl(String url) {
        try {
            return Paths.get(url).toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("invalid classpath url " + url, e);
        }
    }

    private void index(URL url) {
        try {
            LOG.fine("index " + url);
            if (url.toString().endsWith(".jar") || url.toString().endsWith(".war"))
                indexZip(url.openStream());
            else
                indexFolder(url);
        } catch (IOException e) {
            throw new RuntimeException("can't index " + url, e);
        }
    }

    private void indexZip(InputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream, UTF_8);
        while (true) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null)
                break;
            String entryName = entry.getName();
            indexFile(entryName, zipInputStream);
        }
    }

    private void indexFile(String fileName, InputStream inputStream) throws IOException {
        if (fileName.endsWith(".class")) {
            indexer.index(inputStream);
        } else if (fileName.endsWith(".war")) {
            // necessary because of the Thorntail arquillian adapter
            indexZip(inputStream);
        }
    }

    private void indexFolder(URL url) throws IOException {
        try {
            Path folderPath = Paths.get(url.toURI());
            if (Files.isDirectory(folderPath)) {
                try (Stream<Path> walk = Files.walk(folderPath)) {
                    walk.filter(Files::isRegularFile)
                        .forEach(this::indexFile);
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("invalid folder url " + url, e);
        }
    }

    private void indexFile(Path path) {
        try {
            String entryName = path.getFileName().toString();
            indexFile(entryName, Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("can't index path " + path, e);
        }
    }
}
