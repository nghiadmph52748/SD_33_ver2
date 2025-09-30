package org.example.be_sp.controller;

import org.example.be_sp.model.request.TrongLuongRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.TrongLuongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trong-luong-management")
@CrossOrigin(origins = "*")
public class TrongLuongController {

    @Autowired
    TrongLuongService trongLuongService;

    @GetMapping("/list")
    public ResponseObject<?> get() {
        return new ResponseObject<>(trongLuongService.getAll());
    }

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(trongLuongService.getAllTrongLuong());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(trongLuongService.pagingwithdeletedfalse(page, size), "Hien thi thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(trongLuongService.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody TrongLuongRequest request) {
        trongLuongService.add(request);
        return new ResponseObject<>(true, null, "Thêm thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody TrongLuongRequest request, @PathVariable Integer id) {
        trongLuongService.update(id, request);
        return new ResponseObject<>(true, null, "Cập nhật thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        trongLuongService.updateStatus(id);
        return new ResponseObject<>(true, null, "Cập nhật trạng thái thành công");
    }
}
