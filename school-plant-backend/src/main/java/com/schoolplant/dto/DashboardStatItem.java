package com.schoolplant.dto;

import lombok.Data;

@Data
public class DashboardStatItem {
    private String title;
    private long value;
    private String icon;
    private String color;

    public DashboardStatItem(String title, long value, String icon, String color) {
        this.title = title;
        this.value = value;
        this.icon = icon;
        this.color = color;
    }
}
