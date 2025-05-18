package dao;

import model.Answer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDAO {
    public List<Answer> getAnswersByQuestionId(int questionId) {
        List<Answer> list = new ArrayList<>();
        String sql = "SELECT * FROM Answers WHERE QuestionId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer();
                    a.setId(rs.getInt("Id"));
                    a.setQuestionId(rs.getInt("QuestionId"));
                    a.setAnswerText(rs.getString("AnswerText"));
                    a.setCorrect(rs.getBoolean("IsCorrect"));
                    list.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertAnswer(Answer a) {
        String sql = "INSERT INTO Answers(QuestionId, AnswerText, IsCorrect) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getQuestionId());
            ps.setString(2, a.getAnswerText());
            ps.setBoolean(3, a.isCorrect());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAnswer(Answer a) {
        String sql = "UPDATE Answers SET AnswerText=?, IsCorrect=? WHERE Id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAnswerText());
            ps.setBoolean(2, a.isCorrect());
            ps.setInt(3, a.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAnswer(int id) {
        String sql = "DELETE FROM Answers WHERE Id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAnswersByQuestionId(int questionId) {
        String sql = "DELETE FROM Answers WHERE QuestionId=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
