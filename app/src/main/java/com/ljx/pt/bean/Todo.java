package com.ljx.pt.bean;

/** 待办事项实体类，对应 SQLite todo 表 */
public class Todo {
    private int id;
    private String title;
    private String content;
    private boolean isDone;
    private long createTime;

    public Todo() {}

    public Todo(String title, String content) {
        this.title = title;
        this.content = content;
        this.isDone = false;
        this.createTime = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}
