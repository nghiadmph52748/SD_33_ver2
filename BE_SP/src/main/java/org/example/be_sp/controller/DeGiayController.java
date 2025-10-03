package org.example.be_sp.controller;

import org.example.be_sp.model.request.DeGiayRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.DeGiayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/de-giay-management")
@CrossOrigin(origins = "*")
public class DeGiayController {

    @Autowired
    DeGiayService deGiayService;

    @GetMapping("/list")
    public ResponseObject<?> get() {
        return new ResponseObject<>(deGiayService.getAll());
    }

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(deGiayService.getAllDeGiay());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(deGiayService.pagingwithdeletedfalse(page, size), "Hien thi thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(deGiayService.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody DeGiayRequest deGiayRequest) {
        return new ResponseObject<>(true, deGiayService.add(deGiayRequest).getId(), "Thêm đế giày thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody DeGiayRequest deGiayRequest, @PathVariable Integer id) {
        deGiayService.update(id, deGiayRequest);
        return new ResponseObject<>(true, null, "Cập nhật đế giày thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        deGiayService.updateStatus(id);
        return new ResponseObject<>(true, null, "Cập nhật trạng thái đế giày thành");
    }
}
