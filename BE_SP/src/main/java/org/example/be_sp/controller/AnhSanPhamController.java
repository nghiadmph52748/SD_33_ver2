package org.example.be_sp.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import org.example.be_sp.entity.AnhSanPham;
import org.example.be_sp.model.request.AnhSanPhamRequest;
import org.example.be_sp.model.request.AnhSanPhamUploadCloud;
import org.example.be_sp.model.response.AnhSanPhamResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.AnhSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/anh-san-pham-management")
@CrossOrigin(origins = "*")
public class AnhSanPhamController {
	@Autowired
	AnhSanPhamService anhSanPhamService;

	@GetMapping("/list")
	public ResponseObject<?> getAll() {
		return new ResponseObject<>(anhSanPhamService.getAll());
	}

	@GetMapping("/playlist")
	public ResponseObject<?> getAllWF() {
		return new ResponseObject<>(anhSanPhamService.getAllAnhSanPham());
	}

	@GetMapping("/filter")
	public ResponseObject<?> get(@RequestParam("mauAnh") String mauAnh) {
		try {
			List<AnhSanPhamResponse> list = anhSanPhamService.getAllByMauAnh(mauAnh);
			return new ResponseObject<>(true, list, "Lấy danh sách ảnh sản phẩm theo màu thành công");
		} catch (Exception e) {
			return new ResponseObject<>(false, null, "Lỗi khi lọc ảnh sản phẩm: " + e.getMessage());
		}
	}

	@GetMapping("/paging")
	public ResponseObject<?> paging(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return new ResponseObject<>(anhSanPhamService.paging(page, size));
	}

	@GetMapping("/detail/{id}")
	public ResponseObject<?> detail(@PathVariable int id) {
		return new ResponseObject<>(anhSanPhamService.getById(id));
	}

	@PostMapping("/add")
	public ResponseObject<?> add(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "deleted", defaultValue = "false") Boolean deleted,
			@RequestParam(value = "trangThai", defaultValue = "true") Boolean trangThai) {
		try {
			// Tạo request object từ các tham số
			AnhSanPhamRequest request = new AnhSanPhamRequest();
			request.setTrangThai(trangThai);
			request.setDeleted(deleted);

			// Xử lý file upload và lưu đường dẫn
			String filePath = anhSanPhamService.uploadFile(file);
			request.setDuongDanAnh(filePath);
			request.setCreateAt(LocalDate.now());
			AnhSanPham savedAnhSanPham = anhSanPhamService.addAnhSanPham(request);
			return new ResponseObject<>(savedAnhSanPham.getId(), "Thêm ảnh sản phẩm thành công");
		} catch (Exception e) {
			return new ResponseObject<>(false, null, "Lỗi khi thêm ảnh sản phẩm: " + e.getMessage());
		}
	}

	@PutMapping("/update/{id}")
	public ResponseObject<?> update(
			@PathVariable int id,
			@RequestParam("duongDanAnh") String duongDanAnh,
			@RequestParam(value = "trangThai") Boolean trangThai,
			@RequestParam(value = "deleted", defaultValue = "false") Boolean deleted,
			@RequestParam(value = "updateBy", defaultValue = "1") Integer updateBy) {
		try {
			AnhSanPhamRequest request = new AnhSanPhamRequest();
			request.setDuongDanAnh(duongDanAnh);
			request.setTrangThai(trangThai);
			request.setDeleted(deleted);
			request.setUpdateAt(LocalDate.now());
			request.setUpdateBy(updateBy);
			AnhSanPham updatedAnhSanPham = anhSanPhamService.updateAnhSanPham(id, request);
			return new ResponseObject<>(updatedAnhSanPham.getId(), "Cập nhật ảnh sản phẩm thành công");
		} catch (Exception e) {
			return new ResponseObject<>(true, null, "Lỗi khi cập nhật ảnh sản phẩm: " + e.getMessage());
		}
	}

	@PostMapping("/add-multi-image/cloud")
	public ResponseObject<?> post(@RequestParam("file") MultipartFile[] file,
											@RequestParam(value = "tenAnh") String tenAnh,
											@RequestParam(value = "createBy") Integer createBy) {
		try {
			// Tạo request object từ các tham số
			AnhSanPhamUploadCloud request = new AnhSanPhamUploadCloud();
			request.setDuongDanAnh(file);
			request.setTenAnh(tenAnh);
			request.setCreateBy(createBy);
			List<Integer> savedIds = anhSanPhamService.addAnhSanPhamFromCloud(request);
			return new ResponseObject<>(savedIds, "Thêm ảnh sản phẩm thành công");
		} catch (Exception e) {
			return new ResponseObject<>(false, null, "Lỗi khi thêm ảnh sản phẩm: " + e.getMessage());
		}
	}

	@PutMapping("/update-multi-image/cloud/{id}")
	public ResponseObject<?> updateMultiImageCloud(@PathVariable int id,
			@RequestParam("file") MultipartFile[] file,
			@RequestParam("tenAnh") String tenAnh,
			@RequestParam("updateBy") Integer updateBy) {
		AnhSanPhamUploadCloud request = new AnhSanPhamUploadCloud();
		request.setDuongDanAnh(file);
		request.setTenAnh(tenAnh);
		request.setUpdateBy(updateBy);
		List<Integer> saved = anhSanPhamService.updateMultiImageCloud(id, request);
		return new ResponseObject<>(true, saved, "Cập nhật ảnh sản phẩm thành công");
	}

	@PutMapping("/update/status/{id}")
	public ResponseObject<?> updateStatus(@PathVariable int id) {
		anhSanPhamService.updateStatus(id);
		return new ResponseObject<>(true, null, "Cập nhật trạng thái ảnh sản phẩm thành công");
	}

	// Endpoint test để kiểm tra file upload
	@GetMapping("/test-upload")
	public ResponseObject<?> testUpload() {
		try {
			// Kiểm tra thư mục uploads có tồn tại không
			File uploadDir = new File("uploads/images");
			if (uploadDir.exists()) {
				File[] files = uploadDir.listFiles();
				int fileCount = files != null ? files.length : 0;
				return new ResponseObject<>(null, "Thư mục uploads tồn tại, có " + fileCount + " files");
			} else {
				return new ResponseObject<>(null, "Thư mục uploads không tồn tại");
			}
		} catch (Exception e) {
			return new ResponseObject<>(null, "Lỗi khi kiểm tra: " + e.getMessage());
		}
	}
}
