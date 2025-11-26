package edu.univ.erp;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Master Test Suite for ERP System
 * 
 * Runs all tests across the application:
 * - Domain model tests
 * - Service layer tests
 * - API tests
 * - Authentication tests
 * - Utility tests
 * - Integration tests
 */
@Suite
@SuiteDisplayName("ERP System Test Suite")
@SelectPackages({
    "edu.univ.erp.domain",
    "edu.univ.erp.service",
    "edu.univ.erp.api",
    "edu.univ.erp.auth",
    "edu.univ.erp.util",
    "edu.univ.erp.integration"
})
public class TestSuite {
    // This class remains empty, it's used only as a holder for the annotations
}
