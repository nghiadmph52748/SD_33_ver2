package org.example.be_sp.controller;

import org.example.be_sp.model.request.NhaSanXuatRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.NhaSanXuatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/api/nha-san-xuat-management")
@CrossOrigin(origins = "*")
public class NhaSanXuatController {
    @Autowired
    NhaSanXuatService nhaSanXuatService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(nhaSanXuatService.getAllNhaSanXuat(), "Hien thi thanh cong");
    }

    @GetMapping("/paging")
    public ResponseObject<?> getAllPaging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(nhaSanXuatService.paging(page, size), "Hien thi thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(nhaSanXuatService.getById(id), "Hien thi thanh cong");
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody NhaSanXuatRequest request) {
        nhaSanXuatService.add(request);
        return new ResponseObject<>(true,null, "Them moi thanh cong");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody NhaSanXuatRequest request) {
        nhaSanXuatService.update(id, request);
        return new ResponseObject<>(true,null, "Cap nhat thanh cong");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        nhaSanXuatService.updateStatus(id);
        return new ResponseObject<>(true,null, "Xoa nha san xuat thanh cong");
    }
}
