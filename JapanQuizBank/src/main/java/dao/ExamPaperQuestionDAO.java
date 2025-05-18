package dao;

import model.ExamPaperQuestion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamPaperQuestionDAO {
    public boolean insertExamPaperQuestion(ExamPaperQuestion epq) {
        String sql = "INSERT INTO ExamPaperQuestions(ExamPaperId, QuestionId, OrderNumber) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, epq.getExamPaperId());
            ps.setInt(2, epq.getQuestionId());
            ps.setInt(3, epq.getOrderNumber());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ExamPaperQuestion> getQuestionsByExamPaperId(int examPaperId) {
        List<ExamPaperQuestion> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamPaperQuestions WHERE ExamPaperId = ? ORDER BY OrderNumber";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examPaperId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamPaperQuestion epq = new ExamPaperQuestion();
                    epq.setId(rs.getInt("Id"));
                    epq.setExamPaperId(rs.getInt("ExamPaperId"));
                    epq.setQuestionId(rs.getInt("QuestionId"));
                    epq.setOrderNumber(rs.getInt("OrderNumber"));
                    list.add(epq);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
