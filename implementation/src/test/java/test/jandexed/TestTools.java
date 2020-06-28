package test.jandexed;

import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.jboss.jandex.IndexReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class TestTools {
    public static AnnotationsLoaderImpl buildAnnotationsLoader() {
        try (InputStream inputStream = new FileInputStream("target/test-classes/test/jandexed/META-INF/jandex.idx")) {
            return buildAnnotationsLoader(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("can't load test Jandex file", e);
        }
    }

    public static AnnotationsLoaderImpl buildAnnotationsLoader(InputStream inputStream) throws IOException {
        AnnotationsLoaderImpl.JANDEX = new IndexReader(inputStream).read();
        return new AnnotationsLoaderImpl();
    }
}
