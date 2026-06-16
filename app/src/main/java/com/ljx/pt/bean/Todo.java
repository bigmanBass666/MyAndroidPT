package com.ljx.pt.bean;

/** 待办事项实体类，对应 SQLite todo 表 */
public class Todo {
    private long id;
    private String title;
    private String content;
    private boolean done;
    private long createTime;
    private long userId;

    public Todo() {}

    public static Todo of(String title, String content) {
        Todo t = new Todo();
        t.setTitle(title);
        t.setContent(content);
        return t;
    }


    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) {
        this.done = done;
    }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
