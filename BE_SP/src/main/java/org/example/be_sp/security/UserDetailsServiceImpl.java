package org.example.be_sp.security;

import java.util.ArrayList;
import java.util.List;

import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private NhanVienService nhanVienService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(username);

        if (nhanVien == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (nhanVien.getIdQuyenHan() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + nhanVien.getIdQuyenHan().getTenQuyenHan()));
        }

        return User.builder()
                .username(nhanVien.getTenTaiKhoan())
                .password(nhanVien.getMatKhau()) // In production, this should be encoded
                .disabled(!Boolean.TRUE.equals(nhanVien.getTrangThai()))
                .authorities(authorities)
                .build();
    }
}

