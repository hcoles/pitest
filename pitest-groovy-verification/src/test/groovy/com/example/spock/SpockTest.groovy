package com.example.spock

import groovy.transform.TypeChecked
import spock.lang.Specification

@TypeChecked
class SpockTest extends Specification {

    def "should generate coverage for return3Spock method in TestDummyForPit"() {
        given:
        def testDummyForPit = new TestDummyForPit()

        when:
        int result = testDummyForPit.return3Spock()

        then:
        assert result == 3
    }
}
