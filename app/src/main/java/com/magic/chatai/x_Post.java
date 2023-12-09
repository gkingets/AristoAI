package com.magic.chatai;

public class x_Post {

    private String question;
    private String answer;
    private String timeStamp;
    private String uid;
    private String style;

    public x_Post() {
    }

    public x_Post(String question, String answer, String timeStamp, String uid, String style) {
        this.question = question;
        this.answer = answer;
        this.timeStamp = timeStamp;
        this.uid = uid;
        this.style = style;
    }

    public String getQuestion() {
        return question;
    }


    public String getAnswer() {
        return answer;
    }


    public String getTimeStamp() {
        return timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public String getStyle() {
        return style;
    }

}
