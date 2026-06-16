package com.ljx.pt.bean;

/** 用户实体类，对应 SQLite userinfo 表 */
public class User {
    private int id;
    private String name;
    private String psw;
    private String email;

    public User() {}

    public User(String name, String psw) {
        this(name, psw, "");
    }

    public User(String name, String psw, String email) {
        this.name = name;
        this.psw = psw;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPsw() { return psw; }
    public void setPsw(String psw) { this.psw = psw; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "'}";
    }
}
