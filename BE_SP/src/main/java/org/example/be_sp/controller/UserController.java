package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    @PostMapping("/info")
    public ResponseObject<?> getUserInfo() {
        try {
            // Trong thực tế, cần lấy user từ JWT token
            // Hiện tại tạm thời return mock data
            return new ResponseObject<>(true, new Object() {
                public final String name = "Admin User";
                public final String avatar = "";
                public final String job = "Administrator";
                public final String organization = "GearUp";
                public final String location = "Hanoi";
                public final String email = "admin@gearup.vn";
                public final String introduction = "System Administrator";
                public final String personalWebsite = "";
                public final String jobName = "Admin";
                public final String organizationName = "GearUp";
                public final String locationName = "Hanoi, Vietnam";
                public final String phone = "0123456789";
                public final String registrationDate = "2024-01-01";
                public final String accountId = "1";
                public final String certification = "";
                public final String role = "admin";
            }, "Lấy thông tin người dùng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
        }
    }

    @PostMapping("/menu")
    public ResponseObject<?> getMenuList() {
        try {
            // Mock menu data - trong thực tế cần lấy từ database
            return new ResponseObject<>(true, new Object[] {
                new Object() {
                    public final String path = "/dashboard";
                    public final String name = "Dashboard";
                    public final Object[] children = new Object[] {
                        new Object() {
                            public final String path = "/dashboard/workplace";
                            public final String name = "Workplace";
                        },
                        new Object() {
                            public final String path = "/dashboard/monitor";
                            public final String name = "Monitor";
                        }
                    };
                },
                new Object() {
                    public final String path = "/quan-ly-san-pham";
                    public final String name = "Quản lý sản phẩm";
                },
                new Object() {
                    public final String path = "/quan-ly-hoa-don";
                    public final String name = "Quản lý hóa đơn";
                }
            }, "Lấy danh sách menu thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách menu: " + e.getMessage());
        }
    }
}
