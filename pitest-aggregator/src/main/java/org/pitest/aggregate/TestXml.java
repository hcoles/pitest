package org.pitest.aggregate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestXml {
    @JacksonXmlProperty(isAttribute = true)
    String name;
}
