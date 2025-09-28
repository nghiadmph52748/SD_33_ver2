package org.example.be_sp.controller;

import org.example.be_sp.annotation.RequireAuth;
import org.example.be_sp.model.request.PhieuGiamGiaRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/phieu-giam-gia-management")
@CrossOrigin(origins = "*")
@RequireAuth
public class PhieuGiamGiaController {

    @Autowired
    PhieuGiamGiaService service;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(service.getAll());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam Integer page, @RequestParam Integer size) {
        return new ResponseObject<>(service.paging(page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(service.getByIdResponse(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody PhieuGiamGiaRequest request) {
        service.add(request);
        return new ResponseObject<>(null, "Add success");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody PhieuGiamGiaRequest request, @PathVariable Integer id) {
        service.update(id, request);
        return new ResponseObject<>(null, "Update success");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        service.updateStatus(id);
        return new ResponseObject<>(null, "Update status success");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return new ResponseObject<>(null, "Delete success");
    }

    @GetMapping("/list/khach-hang/active/{id}")
    public ResponseObject<?> getActiveForCustomer(@PathVariable Integer id) {
        return new ResponseObject<>(service.getActiveCouponsForCustomer(id));
    }
}
