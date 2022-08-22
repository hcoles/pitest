package org.pitest.mutationtest.autoconfig;

import org.pitest.mutationtest.config.ConfigurationUpdater;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;
import org.pitest.util.Log;

import java.util.logging.Logger;

/**
 * Autosets number of threads based on the number of processors reported
 * by the runtime.
 *
 * The optimum number to use will vary hugely with each codebase. Simplistic
 * formula used here is unlikely to find the best setting, but will make a
 * coarse guess for the current machine based on the number of reported
 * cores. This number itself might be wrong in virtual environments, so
 * feature is best used for local development only.
 *
 * Disabled by default to ensure build is consistent.
 */
public class AutoSetThreads implements ConfigurationUpdater {
    private static final Logger LOG = Log.getLogger();

    @Override
    public void updateConfig(FeatureSetting conf, ReportOptions toModify) {
        // this will be wrong in some environments, feature best used
        // only for local dev
        int cores = getCores();

        // Based on experiments on a macbook, advantage of more threads
        // tails off using a little over half of them
        if (cores >= 8) {
            cores = Math.round(cores / 1.5f);
        } else {
            // For fewer cores rule of thumb of cores - 1 seems to hold
            cores = Math.max(1, cores - 1);
        }


        LOG.info("Overriding configured number of threads (" + toModify.getNumberOfThreads() + ") to be " + cores);
        toModify.setNumberOfThreads(cores);
    }


    @Override
    public Feature provides() {
        return Feature.named("auto_threads")
                .withOnByDefault(false)
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Auto set number of threads based on machine";
    }

    int getCores() {
        return Runtime.getRuntime().availableProcessors();
    }

}
