package com.example.wao_be.util;

public class VectorUtils {

    /**
     * Chuyển chuỗi dạng '1.0, 0.5, 0.8' thành mảng double[].
     * Xử lý cẩn thận case chuỗi rỗng hoặc null.
     */
    public static double[] parseVector(String vectorStr) {
        if (vectorStr == null || vectorStr.trim().isEmpty()) {
            return new double[0];
        }

        String[] parts = vectorStr.split(",");
        double[] vector = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                vector[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                vector[i] = 0.0; // Xử lý fallback nếu dữ liệu lỗi
            }
        }

        return vector;
    }

    /**
     * Tính Cosine Similarity.
     * Xử lý an toàn nếu 2 mảng lệch size hoặc độ dài bằng 0 (trả về 0.0).
     */
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null) {
            return 0.0;
        }
        if (vectorA.length == 0 || vectorB.length == 0) {
            return 0.0;
        }
        if (vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Nhận mảng số thực và ghép lại thành chuỗi phân cách bằng dấu phẩy.
     */
    public static String formatVector(double[] vector) {
        if (vector == null || vector.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Áp dụng công thức Exponential Moving Average: new_val = current_val * learningRate + food_val * (1 - learningRate)
     */
    public static double[] updatePreferenceVector(double[] currentPrefs, double[] foodFeatures, double learningRate) {
        if (currentPrefs == null || foodFeatures == null || currentPrefs.length != foodFeatures.length) {
            return currentPrefs;
        }

        double[] updatedVector = new double[currentPrefs.length];
        for (int i = 0; i < currentPrefs.length; i++) {
            updatedVector[i] = currentPrefs[i] * learningRate + foodFeatures[i] * (1.0 - learningRate);
        }
        return updatedVector;
    }
}
