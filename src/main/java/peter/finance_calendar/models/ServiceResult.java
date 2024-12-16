package peter.finance_calendar.models;

public class ServiceResult<T> {
    public String status;
    public Exception exception;
    public T data;

    public ServiceResult(String status) {
        this.status = status;
    }

    public ServiceResult(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public ServiceResult(String status, Exception exception) {
        this.status = status;
        this.exception = exception;
    }

    public ServiceResult(String status, Exception exception, T data) {
        this.status = status;
        this.exception = exception;
        this.data = data;
    }
}