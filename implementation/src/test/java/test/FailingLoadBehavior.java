package test;

import com.github.t1.annotations.impl.PowerAnnotationsLoader;
import com.github.t1.annotations.index.Index;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

class FailingLoadBehavior {
    @Test void shouldSilentlySkipUnknownIndexResource() {
        PowerAnnotationsLoader loader = new PowerAnnotationsLoader();

        then(loader).isNotNull();
    }

    @Test void shouldFailToLoadInvalidIndexInputStream() throws Exception {
        FileInputStream inputStream = new FileInputStream("pom.xml");

        Throwable throwable = catchThrowable(() -> Index.from(inputStream));

        then(throwable)
            .hasRootCauseInstanceOf(IllegalArgumentException.class)
            .hasRootCauseMessage("Not a jandex index");
    }
}
