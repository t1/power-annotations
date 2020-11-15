package com.github.t1.jandexplus;

import com.github.t1.powerannotations.common.Jandex;
import com.github.t1.powerannotations.common.Logger;
import com.github.t1.powerannotations.common.PowerAnnotations;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;

@Mojo(name = "power-jandex", defaultPhase = PROCESS_CLASSES, threadSafe = true)
public class JandexPlusMojo extends AbstractMojo {

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    MavenProject project;

    @Override
    public void execute() {
        Jandex jandex = Jandex.scan(project.getBasedir().toPath().resolve("target/classes"));

        new PowerAnnotations(jandex, new MojoLogger()).resolveAnnotations();

        jandex.write();
    }

    private class MojoLogger implements Logger {
        @Override public void info(String message) {
            getLog().info(message);
        }
    }
}
