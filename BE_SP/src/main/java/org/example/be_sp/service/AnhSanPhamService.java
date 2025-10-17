package org.example.be_sp.service;

import org.example.be_sp.entity.AnhSanPham;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.AnhSanPhamRequest;
import org.example.be_sp.model.request.AnhSanPhamUploadCloud;
import org.example.be_sp.model.response.AnhSanPhamResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.AnhSanPhamRepository;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.example.be_sp.util.GenericCrudService;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnhSanPhamService extends GenericCrudService<AnhSanPham, Integer, AnhSanPhamResponse, AnhSanPhamRequest> {
	@Autowired
	private AnhSanPhamRepository anhSanPhamRepository;
	@Autowired
	private UploadImageToCloudinary uploadImageToCloudinary;

	// Thư mục lưu file upload
	private static final String UPLOAD_DIR = "uploads/images/";

	public AnhSanPhamService(Class<AnhSanPham> entity, Class<AnhSanPhamResponse> anhSanPhamResponseClass,
			Class<AnhSanPhamRequest> anhSanPhamRequestClass, JpaRepository<AnhSanPham, Integer> anhSanPhamRepository) {
		super(entity, anhSanPhamResponseClass, anhSanPhamRequestClass, anhSanPhamRepository);
	}

	public List<AnhSanPhamResponse> getAllAnhSanPham() {
		return anhSanPhamRepository.findAllByDeletedFalse(false).stream().map(AnhSanPhamResponse::new).toList();
	}

	public List<AnhSanPhamResponse> getAllByMauAnh(String mauAnh) {
		return anhSanPhamRepository.findAllByMauAnh(mauAnh).stream().map(AnhSanPhamResponse::new).toList();
	}

	public PagingResponse<AnhSanPhamResponse> pagingAnhSanPham(int page, int size) {
		return new PagingResponse<>(anhSanPhamRepository.findAllPageByDeleted(false, PageRequest.of(page, size)).map(AnhSanPhamResponse::new ), page);
	}

	public AnhSanPhamResponse getAnhSanPhamById(int id) {
		return anhSanPhamRepository.findById(id).map(AnhSanPhamResponse::new)
				.orElseThrow(() -> new ApiException("Không tìm thấy ảnh sản phẩm với id: " + id, "404"));
	}

	public void updateStatus(int id) {
		AnhSanPham existing = anhSanPhamRepository.findById(id)
				.orElseThrow(() -> new ApiException("Không tìm thấy ảnh sản phẩm với id: " + id, "404"));
		existing.setDeleted(true);
		anhSanPhamRepository.save(existing);
	}

	/**
	 * Thêm ảnh sản phẩm mới và trả về entity đã lưu
	 */
	public AnhSanPham addAnhSanPham(AnhSanPhamRequest request) {
		try {
			AnhSanPham entity = MapperUtils.map(request, AnhSanPham.class);
            return anhSanPhamRepository.save(entity);
		} catch (Exception e) {
			System.err.println("Lỗi tạo ảnh sản phẩm: " + e.getMessage());
			throw e;
		}
	}

	public List<Integer> addAnhSanPhamFromCloud(AnhSanPhamUploadCloud request) {
		try {
			ArrayList<UploadImageToCloudinary.Image> lst = uploadImageToCloudinary.uploadImage(request.getDuongDanAnh());
			List<Integer> savedIds = new ArrayList<>();

			for (UploadImageToCloudinary.Image s : lst) {
				AnhSanPham entity = new AnhSanPham();
				if (anhSanPhamRepository.existsByDuongDanAnh(s.getUrl())) {
					AnhSanPham existingAnhSanPham = anhSanPhamRepository.findByDuongDanAnh(s.getUrl());
					existingAnhSanPham.setDeleted(false); // Reset deleted flag if re-adding
					anhSanPhamRepository.save(existingAnhSanPham);
					savedIds.add(existingAnhSanPham.getId());
					continue;
				}
				entity.setTenAnh(request.getTenAnh());
				entity.setDuongDanAnh(s.getUrl());
				if (request.getMauAnh() != null && !request.getMauAnh().isEmpty()) {
					entity.setMauAnh(request.getMauAnh());
				} else {
					entity.setMauAnh(s.getColor());
				}
				entity.setCreateAt(LocalDate.now());
				entity.setCreateBy(request.getCreateBy());
				entity.setDeleted(false);
				entity.setTrangThai(true);
				AnhSanPham savedEntity = anhSanPhamRepository.save(entity);
				savedIds.add(savedEntity.getId());
			}
			return savedIds;
		} catch (Exception e) {
			System.out.println("Lỗi thêm ảnh sản phẩm từ cloud: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Cập nhật ảnh sản phẩm và trả về entity đã cập nhật
	 */
	public AnhSanPham updateAnhSanPham(int id, AnhSanPhamRequest request) {
		AnhSanPham entity = anhSanPhamRepository.findById(id)
				.orElseThrow(() -> new ApiException("Không tìm thấy ảnh sản phẩm với id: " + id, "404"));
		entity.setTenAnh(request.getTenAnh());
		entity.setTrangThai(request.getTrangThai());
		entity.setUpdateAt(request.getUpdateAt());
		entity.setUpdateBy(request.getUpdateBy());
		return anhSanPhamRepository.save(entity);
	}

	public List<Integer> updateMultiImageCloud(Integer id, AnhSanPhamUploadCloud request) {
		ArrayList<Integer> returnlst = new ArrayList<>();
		ArrayList<UploadImageToCloudinary.Image> lst = uploadImageToCloudinary.uploadImage(request.getDuongDanAnh());
		AnhSanPham entity = anhSanPhamRepository.findById(id)
				.orElseThrow(() -> new ApiException("Không tìm thấy ảnh sản phẩm với id: " + id, "404"));
		for (UploadImageToCloudinary.Image s : lst) {
			entity.setDuongDanAnh(s.getUrl());
			entity.setTenAnh(request.getTenAnh() + " - " + s.getColor());
			entity.setMauAnh(s.getColor());
			entity.setUpdateAt(LocalDate.now());
			entity.setUpdateBy(request.getUpdateBy());
			entity.setId(id);
			returnlst.add(anhSanPhamRepository.save(entity).getId());
		}
		return returnlst;
	}

	/**
	 * Upload file và trả về đường dẫn
	 */
	public String uploadFile(MultipartFile file) throws IOException {
		File uploadDir = new File(UPLOAD_DIR);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}

		// Tạo tên file duy nhất
		String originalFilename = file.getOriginalFilename();
		String fileExtension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String filename = UUID.randomUUID().toString() + fileExtension;

		// Lưu file
		Path filePath = Paths.get(UPLOAD_DIR + filename);
		Files.write(filePath, file.getBytes());

		// Trả về đường dẫn tương đối để lưu vào database
		// Sử dụng đường dẫn tương đối để dễ dàng serve
		return "uploads/images/" + filename;
	}
}
