package models;

import java.util.List;

public class User {
    UserRole role;
    Gender gender;
    String name;
    String username;
    String password;
    int age;
    String profileId;
    List<Integer> routeIds;
    long timestamp;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public List<Integer> getRouteIds() {
        return this.routeIds;
    }

    public void setRouteIds(List<Integer> routeIds) {
        this.routeIds = routeIds;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
