package model;

public class ExamPaperQuestion {
    private int id;
    private int examPaperId;
    private int questionId;
    private int orderNumber;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExamPaperId() { return examPaperId; }
    public void setExamPaperId(int examPaperId) { this.examPaperId = examPaperId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }
}
