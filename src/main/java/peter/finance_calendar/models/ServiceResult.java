package peter.finance_calendar.models;

public class ServiceResult {
    public String status;
    public Exception exception;
    public Object data;

    public ServiceResult(String status) {
        this.status = status;
    }

    public ServiceResult(String status, Object data) {
        this.status = status;
        this.data = data;
    }

    public ServiceResult(String status, Exception exception) {
        this.status = status;
        this.exception = exception;
    }

    public ServiceResult(String status, Exception exception, Object data) {
        this.status = status;
        this.exception = exception;
        this.data = data;
    }
}