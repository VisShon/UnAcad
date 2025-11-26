package edu.univ.erp.api.common;

public class APIResponse<T> {
    public boolean success;
    public String message;
    public T data;

    private APIResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static <T> APIResponse<T> success(String message) {
        return new APIResponse<>(true, message);
    }

    public static <T> APIResponse<T> error(String message) {
        return new APIResponse<>(false, message);
    }

    public APIResponse<T> withData(T data) {
        this.data = data;
        return this;
    }
}