package dao;

import model.ExamPaper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamPaperDAO {
    public List<ExamPaper> getAllExamPapers() {
        List<ExamPaper> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamPapers";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ExamPaper ep = new ExamPaper();
                ep.setId(rs.getInt("Id"));
                ep.setName(rs.getString("Name"));
                ep.setCreatedAt(rs.getTimestamp("CreatedAt"));
                list.add(ep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertExamPaper(ExamPaper ep) {
        String sql = "INSERT INTO ExamPapers(Name, CreatedAt) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ep.getName());
            ps.setTimestamp(2, new java.sql.Timestamp(ep.getCreatedAt().getTime()));
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ep.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
