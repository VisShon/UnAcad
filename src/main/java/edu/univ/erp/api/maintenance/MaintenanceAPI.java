package edu.univ.erp.api.maintenance;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.access.MaintenanceManager;

public class MaintenanceAPI {

    public static boolean isReadOnly() {
        return MaintenanceManager.isReadOnly();
    }

    public static APIResponse<Void> setMaintenance(boolean on) {
        try {
            MaintenanceManager.setReadOnly(on);
            return APIResponse.success("Maintenance updated");
        } catch (Exception e) {
            return APIResponse.error("Failed to update maintenance: " + e.getMessage());
        }
    }
}