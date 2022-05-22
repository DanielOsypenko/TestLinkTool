package com.msi.testlinkBack;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;

public class TestCaseCustom extends TestCase {

    TestCase testCase;

    public TestCaseCustom(TestCase testCase){
        this.testCase = testCase;
    }

    public String toString() {
        return testCase.getId() + " " + testCase.getName();
    }
}
