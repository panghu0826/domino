package com.jule.domino.game.dao.bean;

public class ClubNoteModel {
    private Integer id;

    private Integer clubId;

    private String userId;

    private String noteUserId;

    private String noteName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getNoteUserId() {
        return noteUserId;
    }

    public void setNoteUserId(String noteUserId) {
        this.noteUserId = noteUserId == null ? null : noteUserId.trim();
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName == null ? null : noteName.trim();
    }
}