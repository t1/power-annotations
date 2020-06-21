package test.jandexed;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.newInputStream;

public class JandexPrinter {
    public static void main(String[] args) {
        new JandexPrinter(Paths.get("implementation/target/test-classes/test/jandexed/META-INF/jandex.idx")).run();
        new JandexPrinter(Paths.get("implementation/target/classes/META-INF/jandex.idx")).run();
    }

    private final Path indexFile;
    private final Index index;

    public JandexPrinter(Path indexFile) {
        this.indexFile = indexFile;
        this.index = load(indexFile);
    }

    private Index load(Path indexFile) {
        try (InputStream inputStream = new BufferedInputStream(newInputStream(indexFile))) {
            return new IndexReader(inputStream).read();
        } catch (IOException e) {
            throw new RuntimeException("can't load Jandex index file", e);
        }
    }

    private void run() {
        System.out.println("| " + indexFile);
        System.out.println("------------------------------------------------------------");
        index.printAnnotations();
        System.out.println("------------------------------------------------------------");
        index.printSubclasses();
        System.out.println("------------------------------------------------------------");
    }
}
