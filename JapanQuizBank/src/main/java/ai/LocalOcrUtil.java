package ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LocalOcrUtil {
    /**
     * Gọi file exe xử lý ảnh, trả về JSON kết quả OCR.
     * @param imagePath Đường dẫn file ảnh 
     * @return Chuỗi JSON trả về từ exe
     */
    public static String extractQuestionsFromImageByExe(String imagePath) throws Exception {
        // Gọi exe AI model, truyền path ảnh, nhận kết quả JSON
        String exePath = "src/main/java/ai_model/chat_with_image_file.exe";
        java.io.File imgFile = new java.io.File(imagePath);
        String absImagePath = imgFile.getAbsolutePath();
        System.out.println("[LocalOcrUtil] Đường dẫn exe: " + new java.io.File(exePath).getAbsolutePath());
        System.out.println("[LocalOcrUtil] Đường dẫn ảnh truyền vào: " + absImagePath);
        ProcessBuilder pb = new ProcessBuilder(exePath, absImagePath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("[LocalOcrUtil] OCR exe failed with exit code: " + exitCode);
            System.err.println("[LocalOcrUtil] Output (stdout+stderr):\n" + output);
            throw new RuntimeException("OCR exe failed with exit code " + exitCode + ". Output: " + output);
        }
        return output.toString().trim();
    }
}
