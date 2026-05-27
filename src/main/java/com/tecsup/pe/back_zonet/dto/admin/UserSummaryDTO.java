package com.tecsup.pe.back_zonet.dto.admin;

public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String plan;
    private boolean active;

    public UserSummaryDTO() {}

    public UserSummaryDTO(Long id, String name, String email, String plan, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.plan = plan;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}