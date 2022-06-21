package org.pitest.aggregate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class CoverageXml {
    @JacksonXmlProperty(isAttribute = true)
    String classname;
    @JacksonXmlProperty(isAttribute = true)
    String method;
    @JacksonXmlProperty(isAttribute = true)
    int number;

    @JacksonXmlProperty(isAttribute = true)
    int firstInstruction;

    @JacksonXmlProperty(isAttribute = true)
    int lastInstruction;

    @JacksonXmlProperty
    List<TestXml> tests;

}
