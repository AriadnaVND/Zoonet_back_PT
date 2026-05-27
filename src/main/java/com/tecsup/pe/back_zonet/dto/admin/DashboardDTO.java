package com.tecsup.pe.back_zonet.dto.admin;

import java.util.HashMap;
import java.util.Map;

public class DashboardDTO {
    private long totalUsers;
    private long totalPets;
    private long activePremium;
    private long pendingTickets;
    private double successRate = 94.8;
    private Map<String, Long> devicesStatus = new HashMap<>();

    public DashboardDTO() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalPets() { return totalPets; }
    public void setTotalPets(long totalPets) { this.totalPets = totalPets; }

    public long getActivePremium() { return activePremium; }
    public void setActivePremium(long activePremium) { this.activePremium = activePremium; }

    public long getPendingTickets() { return pendingTickets; }
    public void setPendingTickets(long pendingTickets) { this.pendingTickets = pendingTickets; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public Map<String, Long> getDevicesStatus() { return devicesStatus; }
    public void setDevicesStatus(Map<String, Long> devicesStatus) { this.devicesStatus = devicesStatus; }
}