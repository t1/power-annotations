package test;

import java.nio.file.Paths;

import org.jboss.jandex.IndexView;

import com.github.t1.powerannotations.common.JandexPrinter;

public class JandexPrinterMain {

    public static void main(String[] args) {
        JandexPrinter printer = (args.length > 0 && "--classpath".equals(args[0]))
            ? new JandexPrinter(fromClassPath())
            : new JandexPrinter(Paths.get("implementation/target/test-classes/test/indexed/META-INF/jandex.idx"));
        printer.run();
    }

    @SuppressWarnings("deprecation") private static IndexView fromClassPath() {
        return com.github.t1.annotations.index.Index.fromClassPath().getJandex();
    }
}
