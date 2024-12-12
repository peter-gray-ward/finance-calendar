package peter.finance_calendar.models;

public class User extends Auth {
    private String id;
    private String name;
    private String password;
    private String accessToken;
    private Double checkingBalance;

    public User() {}

    public User(String id, String name, String password, Double checkingBalance) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.checkingBalance = checkingBalance;
    }

    public User(String id, String name, Double checkingBalance) {
        this.id = id;
        this.name = name;
        this.checkingBalance = checkingBalance;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getAccessToken() { return accessToken; }
    public Double getCheckingBalance() { return checkingBalance; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setCheckingBalance(Double checkingBalance) { this.checkingBalance = checkingBalance; }
}