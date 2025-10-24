package org.example.be_sp.service;

import java.io.IOException;
import java.util.List;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.ChiTietSanPhamRequest;
import org.example.be_sp.model.response.ChiTietSanPhamFullResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChatLieuRepository;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.DeGiayRepository;
import org.example.be_sp.repository.KichThuocRepository;
import org.example.be_sp.repository.MauSacRepository;
import org.example.be_sp.repository.SanPhamRepository;
import org.example.be_sp.repository.TrongLuongRepository;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.example.be_sp.util.MapperUtils;
import org.example.be_sp.util.QRGeneration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;

@Service
public class ChiTietSanPhamService {

	@Autowired
	ChiTietSanPhamRepository repository;
	@Autowired
	SanPhamRepository sanPham;
	@Autowired
	MauSacRepository mauSac;
	@Autowired
	KichThuocRepository kichThuoc;
	@Autowired
	DeGiayRepository deGiay;
	@Autowired
	ChatLieuRepository chatLieu;
	@Autowired
	TrongLuongRepository trongLuong;

	@Autowired
	UploadImageToCloudinary uploadImageToCloudinary;

	public List<ChiTietSanPhamFullResponse> getAll() {
		return repository.findAllByDeletedWithDetails(false).stream().map(ChiTietSanPhamFullResponse::new).toList();
	}

	public List<ChiTietSanPhamFullResponse> getAllByIdSanPham(Integer idSanPham) {
		return repository.findAllByDeletedAndIdSanPhamIdWithDetails(false, idSanPham).stream()
				.map(ChiTietSanPhamFullResponse::new).toList();
	}

	public PagingResponse<ChiTietSanPhamFullResponse> getAllWithPage(int page, int size) {
		return new PagingResponse<>(repository.findAllByDeleted(false, PageRequest.of(page, size))
				.map(ChiTietSanPhamFullResponse::new), page);
	}

	public PagingResponse<ChiTietSanPhamFullResponse> getAllByIdSanPhamWithPage(Integer idSanPham, int page, int size) {
		return new PagingResponse<>(repository.findAllByDeletedAndIdSanPham_Id(false, idSanPham, PageRequest.of(page,
				size)).map(ChiTietSanPhamFullResponse::new),
				page);
	}

	public ChiTietSanPhamFullResponse getById(Integer id) {
		return repository.findByIdWithDetails(id).map(ChiTietSanPhamFullResponse::new)
				.orElseThrow(() -> new ApiException("Chi tiết sản phẩm không tồn tại", "404"));
	}

	public Integer updateStatus(Integer id) {
		ChiTietSanPham chiTietSanPham = repository.findById(id)
				.orElseThrow(() -> new ApiException("Chi tiết sản phẩm không tồn tại", "404"));
		chiTietSanPham.setDeleted(true);
		chiTietSanPham.setTrangThai(false);
		repository.save(chiTietSanPham);
		return chiTietSanPham.getId();
	}

	public Integer add(ChiTietSanPhamRequest request) throws IOException, WriterException {
		ChiTietSanPham c = MapperUtils.map(request, ChiTietSanPham.class);
		c.setIdSanPham(sanPham.findSanPhamById(request.getIdSanPham()));
		c.setTenChiTietSanPham(c.getIdSanPham().getTenSanPham() + " - " +
				mauSac.findMauSacById(request.getIdMauSac()).getTenMauSac() + " - " +
				kichThuoc.findKichThuocById(request.getIdKichThuoc()).getTenKichThuoc());
		c.setTenSanPhamChiTiet(c.getTenChiTietSanPham());
		c.setIdMauSac(mauSac.findMauSacById(request.getIdMauSac()));
		c.setIdKichThuoc(kichThuoc.findKichThuocById(request.getIdKichThuoc()));
		c.setIdDeGiay(deGiay.findDeGiayById(request.getIdDeGiay()));
		c.setIdChatLieu(chatLieu.findChatLieuById(request.getIdChatLieu()));
		c.setIdTrongLuong(trongLuong.findTrongLuongById(request.getIdTrongLuong()));
		ChiTietSanPham saved = repository.save(c);
		byte[] qrcode = QRGeneration.generateQRCode(saved.getId().toString());
		String qrCodeUrl = uploadImageToCloudinary.uploadQrCode(qrcode);
		saved.setQrcode(qrCodeUrl);
		repository.save(saved);
		return saved.getId();
	}

	public Integer update(ChiTietSanPhamRequest request, Integer id) {
		ChiTietSanPham e = repository.findById(id)
				.orElseThrow(() -> new ApiException("Chi tiết sản phẩm không tồn tại", "404"));
		MapperUtils.mapToExisting(request, e);
		e.setId(id);
		e.setIdSanPham(sanPham.findSanPhamById(request.getIdSanPham()));
		e.setIdMauSac(mauSac.findMauSacById(request.getIdMauSac()));
		e.setIdKichThuoc(kichThuoc.findKichThuocById(request.getIdKichThuoc()));
		e.setIdDeGiay(deGiay.findDeGiayById(request.getIdDeGiay()));
		e.setIdChatLieu(chatLieu.findChatLieuById(request.getIdChatLieu()));
		e.setIdTrongLuong(trongLuong.findTrongLuongById(request.getIdTrongLuong()));
		repository.save(e);
		return e.getId();
	}
}
