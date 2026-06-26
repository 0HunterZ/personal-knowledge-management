# Kế hoạch Slide Bảo vệ Đồ án: Focus Node (7-10 Slide)

## Goal
Xây dựng một bài thuyết trình 10 slide sắc bén, đi thẳng vào kiến trúc và các điểm đột phá kỹ thuật của Focus Node, không dùng text dài dòng, tối ưu cho thời gian bảo vệ ngắn.

---

## Cấu trúc Slide Chi tiết

### Slide 1: Tiêu đề & Giới thiệu
- **Tiêu đề:** Focus Node - Hệ sinh thái Quản lý Tri thức & Năng suất Cục bộ.
- **Nội dung:** Tên sinh viên, Tên GVHD.
- **Visual:** Logo/Icon ứng dụng, hoặc một ảnh chụp màn hình cực đẹp của giao diện trang chủ (Dark Mode).

### Slide 2: Đặt vấn đề (The Problem)
- **Bullet points (ngắn gọn):**
  - Môi trường số hiện đại chứa quá nhiều xao nhãng (Mạng xã hội, thông báo).
  - Kiến thức bị phân mảnh ở nhiều công cụ khác nhau (Ghi chú, Task, Pomodoro).
  - Phụ thuộc hoàn toàn vào Internet & Cloud, rủi ro bảo mật dữ liệu cá nhân.
- **Visual:** 3 icon minh họa cho 3 vấn đề trên.

### Slide 3: Kiến trúc Hệ thống (System Architecture)
- **Bullet points:**
  - **Mô hình:** MVC (Model - View - Controller) kết hợp Service Locator.
  - **Kiến trúc Dữ liệu (Multi-DB):** 
    - `SqliteAppDataService` (Offline-first)
    - `SqlAppDataService` (LAN/Server)
  - **Giao diện:** JavaFX 21 + AtlantaFX Theme.
- **Visual:** Sơ đồ luồng (Flowchart) cực kỳ cơ bản minh họa việc Service Locator gọi tới các DB khác nhau.

### Slide 4: Đột phá 1 - Lõi Đồng bộ LAN P2P
- **Tiêu đề:** Cộng tác thời gian thực không cần Internet
- **Bullet points:**
  - Kiến trúc Phi tập trung (Decentralized).
  - Tự động khám phá thiết bị qua `DiscoveryPacket` (UDP Broadcast).
  - Đồng bộ trạng thái (CRDTs) cho 5-10 máy trạm.
  - Bảo mật tuyệt đối: Mã hóa E2EE AES-256.
- **Visual:** Ảnh chụp màn hình LAN Hub View hoặc icon các máy tính kết nối chéo nhau (Mesh network).

### Slide 5: Đột phá 2 - OS-Level Zen Mode
- **Tiêu đề:** Kiểm soát xao nhãng cấp độ Hệ điều hành
- **Bullet points:**
  - Không chỉ là đếm ngược thời gian (Pomodoro).
  - Can thiệp HĐH: Kích hoạt chế độ Do Not Disturb.
  - Chặn mạng lưới: Tự động khóa các website giải trí/mạng xã hội.
  - Kích thích thính giác: Tích hợp âm thanh Binaural Beats (Alpha/Theta).
- **Visual:** Ảnh giao diện Zen Mode toàn màn hình.

### Slide 6: Đột phá 3 - Bản đồ Tri thức & Spaced Repetition
- **Tiêu đề:** Tối ưu hóa Năng lực Ghi nhớ
- **Bullet points:**
  - **Spaced Repetition:** Dựa trên Đường cong quên lãng (Forgetting Curve) của Ebbinghaus để nhắc lại kiến thức đúng thời điểm.
  - **Infinite Canvas:** Render hệ thống ghi chú thành Vũ trụ đồ thị (Knowledge Graph) 3 chiều, hỗ trợ zoom vô cực.
- **Visual:** Ảnh chụp phần Knowledge Graph (các hạt node nối với nhau).

### Slide 7: Đột phá 4 - Phân tích Hành vi (Behavioral Analytics)
- **Tiêu đề:** Gamification & Chống Kiệt sức
- **Bullet points:**
  - **Golden Hour Detection:** Phân tích dữ liệu lịch sử để tìm ra "Khung giờ vàng" hiệu suất cao nhất của người dùng.
  - **Burnout Prevention:** Tự động giảm nhiệt độ màu màn hình và cấm nhận task mới khi phát hiện người dùng làm việc quá sức.
- **Visual:** Biểu đồ thống kê hiệu suất từ màn hình Analytics.

### Slide 8: Kết quả Đạt được
- **Bullet points:**
  - Hoàn thiện một "Siêu ứng dụng" Desktop với hiệu năng cao.
  - Giải quyết thành công bài toán đồng bộ thời gian thực (Concurrency/CRDTs) trong JavaFX.
  - Tạo ra trải nghiệm UX/UI hiện đại vượt qua khuôn khổ JavaFX truyền thống.

### Slide 9: Hạn chế & Hướng phát triển
- **Bullet points:**
  - **Hạn chế 1 (Công nghệ UI):** JavaFX cứng nhắc, khó tạo ra các hiệu ứng Animation và UX mượt mà như các Web Framework hiện đại.
  - **Hạn chế 2 (Cơ chế OS-level):** Việc can thiệp sâu vào Window dễ bị các phần mềm Diệt Virus (Windows Defender) hiểu nhầm là mã độc (False Positive).
  - **Hạn chế 3 (LAN P2P):** Xử lý xung đột CRDTs vẫn có độ trễ nếu mạng LAN không ổn định, chưa thể mượt 100% như Cloud.
  - **Hướng phát triển:** Dịch chuyển UI sang kiến trúc Hybrid (Electron/Tauri) để tận dụng sức mạnh Web; Xây dựng hệ thống Plugin mở rộng.

### Slide 10: Q&A
- Lời cảm ơn Hội đồng.
- Demo thực tế (nếu có thời gian).

---

## Checklist Hành động (Trước giờ bảo vệ)
- [ ] Chụp 6-7 ảnh màn hình (Screenshots) nét nhất của các tính năng tương ứng.
- [ ] Copy các Bullet points trên vào PowerPoint/Canva.
- [ ] Giữ font chữ to (tối thiểu 24pt), không đọc slide khi thuyết trình.
- [ ] Mở sẵn app Focus Node để có thể Demo ngay lập tức nếu Thầy/Cô hỏi.
