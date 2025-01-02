package request;

import java.io.*;
import lombok.*;
@Getter
@AllArgsConstructor
public class JsonImportRequest implements Serializable{
    private String jsonData;
}
