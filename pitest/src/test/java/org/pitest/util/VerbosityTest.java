package org.pitest.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.pitest.util.Verbosity.DEFAULT;
import static org.pitest.util.Verbosity.QUIET;

public class VerbosityTest {

    @Test
    public void isCaseInsensitiveWhenParsingFromString() {
        assertThat(Verbosity.fromString("quIeT")).isEqualTo(QUIET);
    }

    @Test
    public void parsesNullAsDefault() {
        assertThat(Verbosity.fromString(null)).isEqualTo(DEFAULT);
    }

    @Test
    public void givesMeaningfulErrorForGarbageInput() {
        assertThatCode(() -> Verbosity.fromString("rubbish"))
                .hasMessageStartingWith("Unrecognised verbosity rubbish");
    }
}