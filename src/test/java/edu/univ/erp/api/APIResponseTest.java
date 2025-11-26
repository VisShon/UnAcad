package edu.univ.erp.api;

import edu.univ.erp.api.common.APIResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("APIResponse Tests")
class APIResponseTest {

    @Test
    @DisplayName("Should create success response")
    void testSuccessResponse() {
        APIResponse<String> response = APIResponse.success("Operation successful");
        
        assertTrue(response.success);
        assertEquals("Operation successful", response.message);
        assertNull(response.data);
    }

    @Test
    @DisplayName("Should create error response")
    void testErrorResponse() {
        APIResponse<String> response = APIResponse.error("Operation failed");
        
        assertFalse(response.success);
        assertEquals("Operation failed", response.message);
        assertNull(response.data);
    }

    @Test
    @DisplayName("Should add data to response")
    void testResponseWithData() {
        APIResponse<String> response = APIResponse.<String>success("Data loaded")
                                                  .withData("Test Data");
        
        assertTrue(response.success);
        assertEquals("Data loaded", response.message);
        assertEquals("Test Data", response.data);
    }

    @Test
    @DisplayName("Should handle typed response with List")
    void testTypedListResponse() {
        List<String> data = List.of("item1", "item2", "item3");
        APIResponse<List<String>> response = APIResponse.<List<String>>success("List loaded")
                                                        .withData(data);
        
        assertTrue(response.success);
        assertNotNull(response.data);
        assertThat(response.data, hasSize(3));
        assertThat(response.data, contains("item1", "item2", "item3"));
    }

    @Test
    @DisplayName("Should handle typed response with Map")
    void testTypedMapResponse() {
        Map<String, Object> data = Map.of("key1", "value1", "key2", 42);
        APIResponse<Map<String, Object>> response = APIResponse.<Map<String, Object>>success("Map loaded")
                                                               .withData(data);
        
        assertTrue(response.success);
        assertNotNull(response.data);
        assertThat(response.data, hasEntry("key1", "value1"));
        assertThat(response.data, hasEntry("key2", 42));
    }

    @Test
    @DisplayName("Should handle void response type")
    void testVoidResponse() {
        APIResponse<Void> response = APIResponse.success("Operation completed");
        
        assertTrue(response.success);
        assertNull(response.data);
    }

    @Test
    @DisplayName("Should chain withData method")
    void testMethodChaining() {
        APIResponse<Integer> response = APIResponse.<Integer>success("Number loaded")
                                                   .withData(42);
        
        assertTrue(response.success);
        assertEquals(42, response.data);
        assertThat(response.data, is(42));
    }

    @Test
    @DisplayName("Should handle custom object as data")
    void testCustomObjectData() {
        class TestData {
            String name;
            int value;
            TestData(String name, int value) {
                this.name = name;
                this.value = value;
            }
        }
        
        TestData testData = new TestData("Test", 100);
        APIResponse<TestData> response = APIResponse.<TestData>success("Custom object loaded")
                                                    .withData(testData);
        
        assertTrue(response.success);
        assertNotNull(response.data);
        assertEquals("Test", response.data.name);
        assertEquals(100, response.data.value);
    }
}
