package org.pitest.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.aether.RepositorySystem;

import javax.inject.Inject;

/**
 * Goal which runs a coverage mutation report
 */
@Mojo(name = "mutationCoverage", 
      defaultPhase = LifecyclePhase.VERIFY,
      requiresDependencyResolution = ResolutionScope.TEST, 
      threadSafe = true)
public class PitMojo extends AbstractPitMojo {

    @Inject
    public PitMojo(RepositorySystem repositorySystem) {
        super(repositorySystem);
    }
}
