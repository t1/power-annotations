package test.indexed;

import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import com.github.t1.annotations.index.Index;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestTools {
    public static AnnotationsLoaderImpl buildAnnotationsLoader() {
        try (InputStream inputStream = new FileInputStream("target/test-classes/test/indexed/META-INF/jandex.idx")) {
            return buildAnnotationsLoader(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("can't load test Jandex file", e);
        }
    }

    public static AnnotationsLoaderImpl buildAnnotationsLoader(InputStream inputStream) {
        Index index = Index.from(inputStream);
        return new AnnotationsLoaderImpl(index);
    }
}
