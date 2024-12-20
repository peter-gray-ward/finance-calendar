package peter.finance_calendar.models;

public final class ControllerResponse<T> {
    String status;
    String template;
    T data;

    public ControllerResponse(String status) {
        this.status = status;
    }

    public ControllerResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public ControllerResponse(String status, T data, String template) {
        this.status = status;
        this.data = data;
        this.template = template;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
