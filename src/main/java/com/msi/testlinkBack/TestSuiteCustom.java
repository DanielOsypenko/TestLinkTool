package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;

public class TestSuiteCustom extends TestSuite {

    TestSuite testSuite;

    public TestSuiteCustom(TestSuite ts) {
        this.testSuite = ts;
    }

    public String toString() {
        return testSuite.getId() + " " + testSuite.getName();
    }
}

