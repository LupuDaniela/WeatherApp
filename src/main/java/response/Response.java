package response;

import java.io.Serializable;


public class Response<T> implements Serializable {
    private boolean success;
    private T data;
    private String message;


    public Response(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }


    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}

