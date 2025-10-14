package org.example.be_sp.controller;

import org.example.be_sp.model.request.PhieuGiamGiaCaNhanRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.PhieuGiamGiaCaNhanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phieu-giam-gia-ca-nhan-management")
@CrossOrigin(origins = "*")
public class PhieuGiamGiaCaNhanController {
    @Autowired
    PhieuGiamGiaCaNhanService service;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(service.getAllResponse());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam Integer page, @RequestParam Integer size) {
        return new ResponseObject<>(service.getAllResponsePaging(page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(service.getByIdResponse(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody PhieuGiamGiaCaNhanRequest request) {
        service.add(request);
        return new ResponseObject<>(true, null,"Add phieu giam gia ca nhan successfully");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody PhieuGiamGiaCaNhanRequest request, @PathVariable Integer id) {
        service.update(id, request);
        return new ResponseObject<>(true, null,"Update phieu giam gia ca nhan successfully");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        service.updateStatus(id);
        return new ResponseObject<>(true, null,"Update status phieu giam gia ca nhan successfully");
    }
}
