package org.example.be_sp.controller.uploadImage;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/upload-image")
public class UploadImageController {
    @Autowired
    UploadImageToCloudinary uploadImageToCloudinary;

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestParam("file") MultipartFile[] file) throws IOException {
        return new ResponseObject<>(true, uploadImageToCloudinary.uploadImage(file), "Upload successful");
    }
}
