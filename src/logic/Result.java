package logic;


// handles success message 
public class Result {
    private boolean success;
    private String message;
    private Object data;
    
    public Result(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public boolean isSuccess() { 
        return success; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public Object getData() { 
        return data; 
    }
}