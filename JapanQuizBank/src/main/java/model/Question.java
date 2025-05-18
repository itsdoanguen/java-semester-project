package model;

import java.util.Date;

public class Question {
    private int id;
    private String content;
    private String type; // nghe, tuvung, nguphap, doc_hieu
    private String audioPath;
    private Date createdAt;
    private boolean hasAudio;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isHasAudio() { return hasAudio; }
    public void setHasAudio(boolean hasAudio) { this.hasAudio = hasAudio; }
}
