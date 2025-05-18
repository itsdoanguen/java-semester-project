package dao;

import model.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    public List<Question> getAllQuestions() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM Questions";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("Id"));
                q.setContent(rs.getString("Content"));
                q.setType(rs.getString("Type"));
                q.setAudioPath(rs.getString("AudioPath"));
                q.setCreatedAt(rs.getTimestamp("CreatedAt"));
                q.setHasAudio(rs.getBoolean("HasAudio"));
                list.add(q);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }    public boolean insertQuestion(Question q) {
        String sql = "INSERT INTO Questions(Content, Type, AudioPath, CreatedAt, HasAudio) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getContent());
            ps.setString(2, q.getType());
            ps.setString(3, q.getAudioPath());
            ps.setTimestamp(4, new java.sql.Timestamp(q.getCreatedAt().getTime()));
            ps.setBoolean(5, q.isHasAudio());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        q.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateQuestion(Question q) {
        String sql = "UPDATE Questions SET Content=?, Type=?, AudioPath=?, CreatedAt=?, HasAudio=? WHERE Id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getContent());
            ps.setString(2, q.getType());
            ps.setString(3, q.getAudioPath());
            ps.setTimestamp(4, new java.sql.Timestamp(q.getCreatedAt().getTime()));
            ps.setBoolean(5, q.isHasAudio());
            ps.setInt(6, q.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteQuestion(int id) {
        String sql = "DELETE FROM Questions WHERE Id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
