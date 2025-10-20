package org.example.be_sp.service.upload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.be_sp.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Service
public class UploadImageToCloudinary {

    @Value("${CLOUDINARY_CLOUD_NAME}")
    private String CLOUD_NAME;
    @Value("${CLOUDINARY_API_KEY}")
    private String API_KEY;
    @Value("${CLOUDINARY_API_SECRET}")
    private String API_SECRET;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET
        ));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Image {

        public Integer id;
        public String url;
        public String color; // Màu chữ chính (tiếng Việt)
    }

    public ArrayList<Image> uploadImage(MultipartFile[] files) {
        ArrayList<Image> urls = new ArrayList<>();
        try {
            for (int i = 0; i < files.length; i++) {
                String hash = DigestUtils.md5DigestAsHex(files[i].getBytes());
                Map uploadResult = cloudinary.uploader().upload(files[i].getBytes(),
                        ObjectUtils.asMap(
                                "folder", "SD_73/images",
                                "public_id", hash,
                                "resource_type", "image",
                                "colors", true
                        )
                );
                String primaryVietnameseColor = getPrimaryVietnameseColor(uploadResult.get("colors"));
                urls.add(new Image(
                        i + 1,
                        uploadResult.get("secure_url").toString(),
                        primaryVietnameseColor
                ));
            }
            return urls;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String uploadQrCode(byte[] qrCodeBytes) {
        try {
            String hash = DigestUtils.md5DigestAsHex(qrCodeBytes);
            Map uploadResult = cloudinary.uploader().upload(qrCodeBytes,
                    ObjectUtils.asMap(
                            "folder", "SD_73/qr_codes",
                            "public_id", hash,
                            "resource_type", "image"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            throw new ApiException("Fail  to upload Qrcode to Cloudinary: " + e.getMessage(), "CLOUDINARY_UPLOAD_ERROR");
        }
    }

    // ===== Helpers: màu chữ chính từ Cloudinary colors =====
    private static String getPrimaryVietnameseColor(Object colorsObj) {
        try {
            if (!(colorsObj instanceof List)) {
                return "Không rõ";
            }
            List<?> colors = (List<?>) colorsObj;
            if (colors.isEmpty()) {
                return "Không rõ";
            }

            String bestHex = null;
            double bestWeight = -1.0;

            for (Object entry : colors) {
                if (entry instanceof List<?> inner && !inner.isEmpty()) {
                    Object hexObj = inner.get(0);
                    String hex = hexObj == null ? null : hexObj.toString();
                    double weight = 0.0;
                    if (inner.size() > 1) {
                        Object w = inner.get(1);
                        if (w instanceof Number) {
                            weight = ((Number) w).doubleValue();
                        } else {
                            try {
                                weight = Double.parseDouble(w.toString());
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (hex != null && hex.startsWith("#")) {
                        if (weight > bestWeight) {
                            bestWeight = weight;
                            bestHex = hex;
                        }
                    }
                }
            }

            if (bestHex == null) {
                return "Không rõ";
            }
            return hexToVietnamese(bestHex);
        } catch (Exception e) {
            return "Không rõ";
        }
    }

    private static String hexToVietnamese(String hex) {
        int[] rgb = hexToRgb(hex);
        float[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
        float h = hsv[0];
        float s = hsv[1];
        float v = hsv[2];

        // Nhóm đơn sắc: Đen/Trắng/Bạc/Xám (ghi)
        if (s <= 0.07f) {
            if (v <= 0.10f) {
                return "Đen";
            }
            if (v >= 0.92f) {
                return "Trắng";
            }
            if (v >= 0.88f) {
                return "Bạc"; // xám rất sáng

            }
            if (v >= 0.65f) {
                return "Xám nhạt";
            }
            if (v >= 0.45f) {
                return "Ghi";  // xám trung tính

            }
            return "Xám đậm";
        }

        // Cực tối theo tông màu
        if (v < 0.18f) {
            if (h >= 210 && h < 250) {
                return "Xanh navy";   // xanh hải quân

            }
            if (h >= 255 && h < 290) {
                return "Chàm";
            }
            if (h >= 290 && h < 330) {
                return "Tím than";
            }
            if (h >= 0 && h < 30 || h >= 330) {
                return "Đỏ đô";
            }
            return "Đen";
        }

        // Màu ấm nhạt: kem/be/kaki (bão hòa thấp, sáng cao)
        if (v >= 0.90f && s <= 0.18f && h >= 40 && h < 75) {
            return "Kem";
        }
        if (v >= 0.78f && v < 0.95f && s <= 0.22f && h >= 35 && h < 70) {
            return "Be";
        }
        if (v >= 0.70f && s <= 0.30f && h >= 50 && h < 95) {
            return "Kaki";
        }

        // Đỏ & các biến thể
        if ((h >= 350 || h < 15) && v <= 0.35f && s > 0.5f) {
            return "Đỏ đô";
        }
        if ((h >= 350 || h < 15) && v <= 0.45f && s > 0.6f) {
            return "Đỏ rượu vang";
        }
        if (h >= 10 && h < 20 && v <= 0.55f && s > 0.5f) {
            return "Đỏ gạch";
        }

        // Cam cháy
        if (h >= 20 && h < 35 && v < 0.65f && s > 0.6f) {
            return "Cam cháy";
        }

        // Nâu và các biến thể
        if (h >= 10 && h < 45 && v < 0.45f && s > 0.45f) {
            return "Nâu chocolate";
        }
        if (h >= 20 && h < 45 && v >= 0.45f && v < 0.65f && s > 0.35f) {
            return "Nâu đất";
        }
        if (h >= 20 && h < 45 && v >= 0.65f && s <= 0.45f) {
            return "Nâu nhạt";
        }

        // Vàng chuyên biệt
        if (h >= 35 && h < 45 && s >= 0.5f) {
            return withShade("Vàng cam", s, v);
        }
        if (h >= 45 && h < 55 && s >= 0.5f) {
            return "Vàng nghệ";
        }
        if (h >= 55 && h < 60) {
            return "Vàng tươi";
        }
        if (h >= 60 && h < 70) {
            return "Vàng chanh";
        }
        if (h >= 35 && h < 60 && s < 0.5f && v > 0.8f) {
            return "Vàng nhạt";
        }

        // Olive/rêu
        if (h >= 65 && h < 95 && v < 0.6f && s >= 0.3f) {
            return "Xanh rêu";
        }
        if (h >= 60 && h < 95 && s >= 0.15f && s <= 0.45f && v >= 0.45f && v <= 0.85f) {
            return "Xanh olive";
        }

        // Xanh lá chi tiết
        if (h >= 75 && h < 95) {
            return withShade("Xanh lá mạ", s, v);
        }
        if (h >= 95 && h < 120) {
            return withShade("Xanh lá", s, v);
        }
        if (h >= 120 && h < 140) {
            return withShade("Xanh lục", s, v);
        }
        if (h >= 140 && h < 160 && s > 0.45f) {
            return withShade("Xanh lục bảo", s, v);
        }

        // Bạc hà
        if (h >= 150 && h < 170 && s <= 0.45f && v > 0.7f) {
            return "Xanh bạc hà";
        }

        // Vùng teal/cyan
        if (h >= 165 && h < 178 && s > 0.5f) {
            return withShade("Xanh lục lam", s, v);
        }
        if (h >= 178 && h < 190) {
            return withShade("Xanh ngọc lam", s, v);
        }

        // Xanh da trời
        if (h >= 190 && h < 205 && v > 0.6f) {
            return "Xanh da trời";
        }

        // Xanh dương và biến thể
        if (h >= 205 && h < 220) {
            return withShade("Xanh dương nhạt", s, v);
        }
        if (h >= 220 && h < 235) {
            return withShade("Xanh dương", s, v);
        }
        if (h >= 235 && h < 255 && s > 0.4f) {
            return withShade("Xanh coban", s, v);
        }
        if (h >= 210 && h < 255 && v < 0.35f) {
            return "Xanh navy";
        }

        // Chàm/Indigo
        if (h >= 255 && h < 275) {
            return "Chàm";
        }

        // Tím chi tiết
        if (h >= 260 && h < 290 && s < 0.35f && v > 0.7f) {
            return "Tím oải hương";
        }
        if (h >= 275 && h < 300) {
            return withShade("Tím", s, v);
        }
        if (h >= 290 && h < 320 && v < 0.45f) {
            return "Tím mận";
        }

        // Magenta/Fuchsia
        if (h >= 300 && h < 315 && s > 0.5f) {
            return "Hồng tím";
        }
        if (h >= 315 && h < 330 && s > 0.6f) {
            return "Hồng đậm";
        }

        // Hồng & biến thể
        if ((h >= 330 && h < 350) || ((h >= 350 || h < 10) && s < 0.6f && v > 0.7f)) {
            if (s < 0.5f && v > 0.85f) {
                return "Hồng phấn";
            }
            return withShade("Hồng", s, v);
        }

        // Phần còn lại: gán tên tổng quát + sắc độ
        if (h < 10 || h >= 350) {
            return withShade("Đỏ", s, v);
        }
        if (h < 25) {
            return withShade("Cam", s, v);
        }
        if (h < 35) {
            return withShade("Hổ phách", s, v);
        }
        if (h < 55) {
            return withShade("Vàng", s, v);
        }
        if (h < 165) {
            return withShade("Xanh lá", s, v);
        }
        if (h < 190) {
            return withShade("Xanh ngọc", s, v);
        }
        if (h < 255) {
            return withShade("Xanh dương", s, v);
        }
        if (h < 290) {
            return "Chàm";
        }
        if (h < 330) {
            return withShade("Tím", s, v);
        }

        return withShade("Đỏ", s, v);
    }

    private static String withShade(String base, float s, float v) {
        // Thêm mô tả sắc độ đơn giản
        if (v >= 0.85f && s <= 0.60f) {
            return base + " nhạt";
        }
        if (v <= 0.40f && s >= 0.50f) {
            return base + " đậm";
        }
        return base;
    }

    private static int[] hexToRgb(String hex) {
        String h = hex.replace("#", "");
        if (h.length() == 3) {
            // R,G,B dạng #abc -> #aabbcc
            char r = h.charAt(0), g = h.charAt(1), b = h.charAt(2);
            h = "" + r + r + g + g + b + b;
        }
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float h;
        if (delta == 0) {
            h = 0;
        } else if (max == rf) {
            h = 60 * (((gf - bf) / delta) % 6);
        } else if (max == gf) {
            h = 60 * (((bf - rf) / delta) + 2);
        } else {
            h = 60 * (((rf - gf) / delta) + 4);
        }
        if (h < 0) {
            h += 360;
        }
        float s = max == 0 ? 0 : delta / max;
        float v = max;
        return new float[]{h, s, v};
    }
}
