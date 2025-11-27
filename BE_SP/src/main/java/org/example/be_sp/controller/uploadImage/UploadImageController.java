package org.example.be_sp.controller.uploadImage;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/upload-image")
public class UploadImageController {
    @Autowired
    private UploadImageToCloudinary uploadImageToCloudinary;

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestParam("file") MultipartFile[] file) throws IOException {
        var result = uploadImageToCloudinary.uploadImage(file);
        return new ResponseObject<>(true, result, "Upload successful");
    }
}
