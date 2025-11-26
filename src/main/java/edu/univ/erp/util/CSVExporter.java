package edu.univ.erp.util;

import edu.univ.erp.data.EnrollmentDAO;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

public class CSVExporter {

    public static String exportClassList(int sectionId) throws Exception {
        String fileName = "classlist_" + sectionId + ".csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("Student ID,Name\n");

        List<Map<String, Object>> enrollments = EnrollmentDAO.listEnrollmentsForSection(sectionId); // Assume you add this method
        for (Map<String, Object> e : enrollments) {
            writer.append(e.get("student_id") + "," + e.get("name") + "\n");
        }

        writer.flush();
        writer.close();
        return fileName;
    }
}