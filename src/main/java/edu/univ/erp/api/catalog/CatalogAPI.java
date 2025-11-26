package edu.univ.erp.api.catalog;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.service.StudentService;

import java.util.List;
import java.util.Map;

public class CatalogAPI {

    public static APIResponse<List<Map<String, Object>>> listCourses() {
        return StudentService.listCatalog();
    }

    public static APIResponse<List<Map<String, Object>>> listSections(int courseId) {
        return StudentService.listSections(courseId);
    }
}