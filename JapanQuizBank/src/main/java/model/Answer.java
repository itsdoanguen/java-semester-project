package model;

public class Answer {
    private int id;
    private int questionId;
    private String answerText;
    private boolean isCorrect;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
}
