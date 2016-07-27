package com.jonlenes.app.Modelo;

import java.sql.Date;
import java.sql.Time;

/**
 * Created by Jonlenes on 17/07/2016.
 */
public class ScheduledTime {
    private Long id;
    private Date dateDay;
    private Time startTime;
    private Time endTime;
    private Long duration;
    private User user;
    private Local local;
    private Client client;

    public ScheduledTime() {

    }

    public ScheduledTime(Time startTime, Time endTime, Long duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public ScheduledTime(Date dateDay, Time startTime, Time endTime, Long duration) {
        this.dateDay = dateDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public ScheduledTime(Long id, Date dateDay, Time startTime, Time endTime, Long duration, User user, Local local) {
        this.id = id;
        this.dateDay = dateDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.user = user;
        this.local = local;
    }

    public ScheduledTime(Long id, Date dateDay, Time startTime, Time endTime, Long duration, User user, Local local, Client client) {
        this.id = id;
        this.dateDay = dateDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.user = user;
        this.local = local;
        this.client = client;
    }

    public Date getDateDay() {
        return dateDay;
    }

    public void setDateDay(Date dateDay) {
        this.dateDay = dateDay;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
