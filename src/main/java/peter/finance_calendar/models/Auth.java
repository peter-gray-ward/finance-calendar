package peter.finance_calendar.models;

public class Auth extends Model {
    private boolean authenticated;

    public boolean getAuthenticated() { return this.authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
}
