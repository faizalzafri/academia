package com.academia.platform.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "activity_results")
public class ActivityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false, length = 100)
    private String eventName; // e.g. "100m Sprint", "Solo Classical Dance", "Debate Final"

    @Column(nullable = false, length = 100)
    private String winnerName;

    @Column(length = 50)
    private String winnerClass; // e.g. "Class 10-A"

    @Column(nullable = false, length = 50)
    private String position; // e.g. "1st Place", "Gold", "Best Speaker"

    @Column(length = 50)
    private String score; // e.g. "10.42s", "98/100"

    public ActivityResult() {
    }

    public ActivityResult(Activity activity, String eventName, String winnerName, String winnerClass, String position, String score) {
        this.activity = activity;
        this.eventName = eventName;
        this.winnerName = winnerName;
        this.winnerClass = winnerClass;
        this.position = position;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getWinnerClass() {
        return winnerClass;
    }

    public void setWinnerClass(String winnerClass) {
        this.winnerClass = winnerClass;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
