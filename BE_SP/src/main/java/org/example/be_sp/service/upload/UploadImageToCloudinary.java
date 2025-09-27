package org.example.be_sp.service.upload;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class UploadImageToCloudinary {
    private static final String CLOUD_NAME = "dlgbdwd96";
    private static final String API_KEY = "192955682558772";
    private static final String API_SECRET = "76BO3I-JZ7pGueCII0HAW8fSZTQ";

    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET
    ));

    public ArrayList<String> uploadImage(MultipartFile[] files) {
        ArrayList<String> urls = new java.util.ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String hash = DigestUtils.md5DigestAsHex(file.getBytes());
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "SD_73",
                                "public_id", hash,
                                "resource_type", "image"
                        )
                );
                urls.add((String) uploadResult.get("secure_url"));
            }
            return urls;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
