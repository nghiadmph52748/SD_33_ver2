package org.example.be_sp.controller.uploadImage;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/upload-image")
public class UploadImageController {
    UploadImageToCloudinary uploadImageToCloudinary = new UploadImageToCloudinary();

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestParam("file") MultipartFile[] file) throws IOException {
        return new ResponseObject<>(true, uploadImageToCloudinary.uploadImage(file), "Upload successful");
    }
}
