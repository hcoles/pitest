package org.pitest.bytecode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignatureParserTest {

    @Test
    public void noTypesForNullSignature() {
        assertThat(SignatureParser.extractTypes(null)).isEmpty();
    }

    @Test
    public void parsesSupplierOfStrings() {
        String signature = "Ljava/util/function/Supplier<Ljava/lang/String;>;";
        assertThat(SignatureParser.extractTypes(signature)).containsExactlyInAnyOrder("java/util/function/Supplier", "java/lang/String");
    }

    @Test
    public void parsesListsOfFunctions() {
        String signature = "Ljava/util/List<Ljava/util/function/Function<Ljava/lang/Integer;Ljava/lang/Integer;>;>;";
        assertThat(SignatureParser.extractTypes(signature)).containsExactlyInAnyOrder("java/util/List", "java/util/function/Function", "java/lang/Integer");
    }
}