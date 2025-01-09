package request;

import java.io.*;
import lombok.*;
@Getter

public class JsonImportRequest implements Serializable{
    private String jsonData;
    public JsonImportRequest(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON data cannot be null or empty.");
        }
        this.jsonData = jsonData;
    }

}
