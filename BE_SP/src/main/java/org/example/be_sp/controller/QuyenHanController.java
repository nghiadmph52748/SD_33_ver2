package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.QuyenHanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/quyen-han-management")
@CrossOrigin(origins = "*")
public class QuyenHanController {
    @Autowired
    private QuyenHanService service;
    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(service.getAll(), "Hien thi thanh cong");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable("id") Integer id) {
        return new ResponseObject<>(service.getById(id), "Hien thi thanh cong" + id);
    }
}
