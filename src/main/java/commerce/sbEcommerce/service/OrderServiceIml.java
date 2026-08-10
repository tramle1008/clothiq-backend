package commerce.sbEcommerce.service;

import commerce.sbEcommerce.model.*;
import commerce.sbEcommerce.payload.AddressDTO;
import commerce.sbEcommerce.payload.OrderDTO;
import commerce.sbEcommerce.payload.OrderItemDTO;
import commerce.sbEcommerce.payload.OrderSearchCriteria;
import commerce.sbEcommerce.payload.QRPaymentResponseDTO;
import commerce.sbEcommerce.repository.*;
import commerce.sbEcommerce.util.AuthUtil;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

@Service
public class OrderServiceIml implements OrderService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired
    CartRepository cartRepository;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CartService cartService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    AuthUtil authUtil;

    @PostConstruct
    public void setupModelMapper() {
        modelMapper.addMappings(new PropertyMap<Address, AddressDTO>() {
            @Override
            protected void configure() {
                map().setWardId(source.getWard().getWardId());
                map().setWardName(source.getWard().getName());
                map().setProvinceId(source.getWard().getProvince().getProvinceId());
                map().setProvinceName(source.getWard().getProvince().getName());
            }
        });
    }

    @Override
    @Transactional
    public OrderDTO placeOrderWithCOD(String emailId, Long addressId) {
        return createAndSaveOrder(emailId, addressId, "COD", null, null, null, null);
    }

    @Override
    @Transactional
    public OrderDTO placeOrderWithOnlinePayment(String emailId, Long addressId, String paymentMethod,
                                                String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        return createAndSaveOrder(emailId, addressId, paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage);
    }

    private OrderDTO createAndSaveOrder(String emailId, Long addressId, String paymentMethod,
                                        String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        Cart cart = cartRepository.findByUser_Email(emailId);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart rá»—ng");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y Ä‘á»‹a chá»‰"));

        Order order = new Order();
        order.setEmail(emailId);
        order.setDateOrder(LocalDate.now(VIETNAM_ZONE));
        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems());
        order.setTotalAmount(calculateOrderTotal(cartItems));
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setDeliveryStatus(DeliveryStatus.PENDING);
        } else {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setDeliveryStatus(DeliveryStatus.PENDING);
        }

        order.setAddress(address);
        order.setCode(generateOrderCode());
        order.setPaymentMethod(paymentMethod);

        if (!paymentMethod.equalsIgnoreCase("COD")) {
            Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
            payment.setOrder(order);
            paymentRepository.save(payment);
            order.setPayment(payment);
        }

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            double salePrice = product.getSalePrice() != null ? product.getSalePrice() : 0.0;
            double discount = getCurrentDiscountPercent(product);
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setDiscount(discount);
            item.setOrderProductPrice(salePrice);
            return item;
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);

            cart.getCartItems().remove(item);
            item.setCart(null);
        }

        cartRepository.save(cart);

        OrderDTO orderDTO = mapOrderToDto(savedOrder);
        orderDTO.setOrderItemList(orderItems.stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));
        return orderDTO;
    }

    private String generateOrderCode() {
        return "DH" + System.currentTimeMillis();
    }

    @Override
    public QRPaymentResponseDTO createOrderWithQR(String email, Long addressId) {
        Order order = new Order();
        order.setEmail(email);
        order.setDateOrder(LocalDate.now(VIETNAM_ZONE));
        order.setPaymentMethod("QR");
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order.setAddress(addressRepository.findById(addressId).orElseThrow());

        Cart cart = cartRepository.findByUser_Email(email);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart rá»—ng");
        }

        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems());
        double total = calculateOrderTotal(cartItems);
        order.setTotalAmount(total);
        String orderCode = generateOrderCode();
        order.setCode(orderCode);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            double salePrice = product.getSalePrice() != null ? product.getSalePrice() : 0.0;
            double discount = getCurrentDiscountPercent(product);
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setDiscount(discount);
            item.setOrderProductPrice(salePrice);
            return item;
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        String qrUrl = generateQrUrl(savedOrder);
        return new QRPaymentResponseDTO(savedOrder.getOrderId(), orderCode, total, qrUrl);
    }

    private double getCurrentDiscountPercent(Product product) {
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        return product.getDiscounts().stream()
                .filter(discount -> Boolean.TRUE.equals(discount.getActive()))
                .filter(discount -> discount.getStartDate() == null || !discount.getStartDate().isAfter(now))
                .filter(discount -> discount.getEndDate() == null || !discount.getEndDate().isBefore(now))
                .map(Discount::getPercent)
                .findFirst()
                .orElse(0.0);
    }

    private double calculateOrderTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(cartItem -> {
                    Product product = cartItem.getProduct();
                    double salePrice = product.getSalePrice() != null ? product.getSalePrice() : 0.0;
                    double discount = getCurrentDiscountPercent(product);
                    double discountedPrice = salePrice * (100 - discount) * 0.01;
                    return discountedPrice * cartItem.getQuantity();
                })
                .sum();
    }

    private String generateQrUrl(Order order) {
        String account = System.getenv("BANK_ACCOUNT");
        String bank = "VietinBank";
        long amount = Math.round(order.getTotalAmount());
        String des = "SEVQR." + order.getCode();
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s",
                account, bank, amount, des);
    }

    @Override
    @Transactional
    public void markOrderPaidByCode(String code, Long amount, String referenceCode) {
        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n hÃ ng"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        if (!order.getTotalAmount().equals(amount.doubleValue())) {
            throw new RuntimeException("Sá»‘ tiá»n khÃ´ng khá»›p vá»›i Ä‘Æ¡n hÃ ng");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        Cart cart = cartRepository.findByUser_Email(order.getEmail());
        if (cart != null) {
            List<CartItem> cartItems = new ArrayList<>(cart.getCartItems());

            for (CartItem item : cartItems) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);

                cart.getCartItems().remove(item);
                item.setCart(null);
            }

            cartRepository.save(cart);
        }

        Payment payment = order.getPayment();
        if (payment == null) {
            payment = new Payment("QR", referenceCode, "success", "Webhook SePay", "SePay");
            payment.setOrder(order);
        } else {
            payment.setPgPaymentId(referenceCode);
            payment.setPgStatus("success");
            payment.setPgResponseMessage("Webhook SePay");
            payment.setPgName("SePay");
        }

        paymentRepository.save(payment);
        order.setPayment(payment);
        orderRepository.save(order);

        Transaction tx = new Transaction();
        tx.setCode(code);
        tx.setTransactionContent(code);
        tx.setAmountIn(BigDecimal.valueOf(amount));
        tx.setTransactionDate(new Timestamp(System.currentTimeMillis()));
        tx.setReferenceNumber(referenceCode);
        tx.setGateway("SePay");
        transactionRepository.save(tx);
    }

    @Override
    public Page<OrderDTO> getPENDING(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, OrderSearchCriteria criteria) {
        return searchOrders(pageNumber, pageSize, sortBy, sortOrder, criteria, null, DeliveryStatus.PENDING, true);
    }

    @Override
    public Page<OrderDTO> getOrderSHIPPED(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, OrderSearchCriteria criteria) {
        return searchOrders(pageNumber, pageSize, sortBy, sortOrder, criteria, null, DeliveryStatus.SHIPPED, false);
    }

    @Override
    public OrderDTO serviceMarkRejected(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không thấy đơn hàng"));

        if (order.getDeliveryStatus() != DeliveryStatus.PENDING) {
            throw new RuntimeException("Đơn hàng đang ở trạng thái PENDING");
        }

        order.setDeliveryStatus(DeliveryStatus.REJECTED);
        order.setTotalAmount(order.getTotalAmount());
        orderRepository.save(order);
        return mapOrderToDto(order);
    }

    @Override
    public Page<OrderDTO> getPagedOrderDetails(int pageNumber, int pageSize, String sortBy, String sortOrder, OrderSearchCriteria criteria) {
        return searchOrders(pageNumber, pageSize, sortBy, sortOrder, criteria, null, null, false);
    }

    @Override
    public Page<OrderDTO> getUserOrderPaginated(int page, int size, String sortBy, String sortDir, OrderSearchCriteria criteria) {
        return searchOrders(page, size, sortBy, sortDir, criteria, authUtil.getCurrentUserEmail(), null, false);
    }

    @Override
    public Page<OrderDTO> getAllOrdersPaginated(int page, int size, String sortBy, String sortDir, OrderSearchCriteria criteria) {
        return searchOrders(page, size, sortBy, sortDir, criteria, null, null, false);
    }

    @Override
    @Transactional
    public OrderDTO adminMarkShipped(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không thấy đơn hàng"));

        if (order.getDeliveryStatus() != DeliveryStatus.PENDING) {
            throw new RuntimeException("Trạng thái đơn hàng không đúng");
        }

        order.setDeliveryStatus(DeliveryStatus.SHIPPED);
        orderRepository.save(order);
        return mapOrderToDto(order);
    }

    @Override
    @Transactional
    public OrderDTO delivererMarkDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không thấy đơn hàng"));

        if (order.getDeliveryStatus() != DeliveryStatus.SHIPPED) {
            throw new RuntimeException("Trạng thái đơn hàng không là đang giao");
        }

        if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderRepository.save(order);
        return mapOrderToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateShippingSlipPdf(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException("Không có đơn hàng để in");
        }

        List<Order> orders = new ArrayList<>();
        orderRepository.findAllById(orderIds).forEach(orders::add);
        if (orders.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                if (order.getDeliveryStatus() != DeliveryStatus.SHIPPED) {
                    throw new RuntimeException("Chỉ được in phiếu cho đơn ở trạng thái SHIPPED");
                }

                appendShippingSlip(document, order);
                if (i < orders.size() - 1) {
                    document.newPage();
                }
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Không thể tạo PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private Page<OrderDTO> searchOrders(int pageNumber, int pageSize, String sortBy, String sortOrder,
                                        OrderSearchCriteria criteria, String email, DeliveryStatus fixedStatus,
                                        boolean requireReadyForProcessing) {
        OrderSearchCriteria effectiveCriteria = criteria != null ? criteria : new OrderSearchCriteria();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));

        if (fixedStatus != null && effectiveCriteria.getDeliveryStatus() != null
                && effectiveCriteria.getDeliveryStatus() != fixedStatus) {
            return Page.empty(pageable);
        }

        DeliveryStatus effectiveStatus = fixedStatus != null ? fixedStatus : effectiveCriteria.getDeliveryStatus();

        Specification<Order> spec = Specification.where(hasEmail(email))
                .and(hasDeliveryStatus(effectiveStatus))
                .and(hasPaymentStatus(effectiveCriteria.getPaymentStatus()))
                .and(hasPaymentMethod(effectiveCriteria.getPaymentMethod()))
                .and(hasKeyword(effectiveCriteria.getKeyword()))
                .and(hasFromDate(effectiveCriteria.getFromDate()))
                .and(hasToDate(effectiveCriteria.getToDate()));

        if (requireReadyForProcessing) {
            spec = spec.and(isReadyForProcessing());
        }

        return orderRepository.findAll(spec, pageable).map(this::mapOrderToDto);
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String safeSortBy = switch (sortBy) {
            case "orderId", "dateOrder", "totalAmount", "deliveryStatus", "paymentStatus", "paymentMethod", "code", "email" -> sortBy;
            default -> "dateOrder";
        };
        return "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();
    }

    private Specification<Order> hasEmail(String email) {
        return (root, query, cb) -> email == null ? null : cb.equal(root.get("email"), email);
    }

    private Specification<Order> hasDeliveryStatus(DeliveryStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("deliveryStatus"), status);
    }

    private Specification<Order> hasPaymentStatus(PaymentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("paymentStatus"), status);
    }

    private Specification<Order> hasPaymentMethod(String paymentMethod) {
        return (root, query, cb) -> {
            if (paymentMethod == null || paymentMethod.isBlank()) {
                return null;
            }
            return cb.equal(cb.lower(root.get("paymentMethod")), paymentMethod.trim().toLowerCase());
        };
    }

    private Specification<Order> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(root.get("orderId").as(String.class), pattern)
            );
        };
    }

    private Specification<Order> hasFromDate(LocalDate fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("dateOrder"), fromDate);
    }

    private Specification<Order> hasToDate(LocalDate toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("dateOrder"), toDate);
    }

    private Specification<Order> isReadyForProcessing() {
        return (root, query, cb) -> cb.or(
                cb.and(
                        cb.equal(cb.lower(root.get("paymentMethod")), "cod"),
                        cb.equal(root.get("paymentStatus"), PaymentStatus.UNPAID)
                ),
                cb.and(
                        cb.notEqual(cb.lower(root.get("paymentMethod")), "cod"),
                        cb.equal(root.get("paymentStatus"), PaymentStatus.PAID)
                )
        );
    }

    private OrderDTO mapOrderToDto(Order order) {
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);

        userRepository.findByEmail(order.getEmail())
                .ifPresent(user -> dto.setUserName(user.getUserName()));

        List<OrderItemDTO> itemDTOs = order.getOrderItemList().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList());
        dto.setOrderItemList(itemDTOs);

        if (order.getAddress() != null) {
            dto.setAddress(modelMapper.map(order.getAddress(), AddressDTO.class));
        }

        return dto;
    }

    private void appendShippingSlip(Document document, Order order) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

        Paragraph title = new Paragraph("PHIẾU GIAO HÀNG", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10f);
        document.add(title);

        Paragraph meta = new Paragraph();
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.add(new Chunk("Mã đơn: #" + order.getOrderId() + "\n", headerFont));
        meta.add(new Chunk("Mã phiếu: " + (order.getCode() != null ? order.getCode() : "--") + "\n", normalFont));
        meta.add(new Chunk("Ngày in: " + LocalDate.now(VIETNAM_ZONE) + "\n", normalFont));
        meta.setSpacingAfter(12f);
        document.add(meta);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 2f});
        addInfoCell(infoTable, "Khách hàng", getUserName(order), headerFont, normalFont);
        addInfoCell(infoTable, "Email", order.getEmail(), headerFont, normalFont);
        addInfoCell(infoTable, "Số điện thoại", order.getAddress() != null ? order.getAddress().getPhoneNumber() : "--", headerFont, normalFont);
        addInfoCell(infoTable, "Địa chỉ", formatAddress(order), headerFont, normalFont);
        addInfoCell(infoTable, "Thanh toán", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "--", headerFont, normalFont);
        addInfoCell(infoTable, "Trạng thái giao", order.getDeliveryStatus() != null ? order.getDeliveryStatus().name() : "--", headerFont, normalFont);
        document.add(infoTable);

        Paragraph itemsTitle = new Paragraph("Danh sách sản phẩm", headerFont);
        itemsTitle.setSpacingBefore(12f);
        itemsTitle.setSpacingAfter(6f);
        document.add(itemsTitle);

        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[]{4f, 1f, 2f, 2f, 2f});
        addHeaderCell(itemTable, "Sản phẩm", headerFont);
        addHeaderCell(itemTable, "SL", headerFont);
        addHeaderCell(itemTable, "Đơn giá", headerFont);
        addHeaderCell(itemTable, "Giảm giá", headerFont);
        addHeaderCell(itemTable, "Thành tiền", headerFont);

        double grandTotal = 0;
        for (OrderItem item : order.getOrderItemList()) {
            double lineTotal = item.getOrderProductPrice() * (100 - item.getDiscount()) * 0.01 * item.getQuantity();
            grandTotal += lineTotal;
            addBodyCell(itemTable, item.getProduct() != null ? item.getProduct().getProductName() : "--", normalFont);
            addBodyCell(itemTable, String.valueOf(item.getQuantity()), normalFont);
            addBodyCell(itemTable, formatMoney(item.getOrderProductPrice()), normalFont);
            addBodyCell(itemTable, item.getDiscount() + "%", normalFont);
            addBodyCell(itemTable, formatMoney(lineTotal), normalFont);
        }
        document.add(itemTable);

        Paragraph total = new Paragraph("Tổng tiền: " + formatMoney(grandTotal), headerFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10f);
        document.add(total);

    }

    private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell keyCell = new PdfPCell(new Phrase(label + ":", labelFont));
        keyCell.setBorder(Rectangle.BOX);
        keyCell.setPadding(6f);
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "--", valueFont));
        valueCell.setBorder(Rectangle.BOX);
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 236, 240));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "--", font));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private String getUserName(Order order) {
        return userRepository.findByEmail(order.getEmail())
                .map(User::getUserName)
                .orElse("--");
    }

    private String formatAddress(Order order) {
        if (order.getAddress() == null) {
            return "--";
        }
        Address address = order.getAddress();
        List<String> parts = new ArrayList<>();
        if (address.getDetail() != null) parts.add(address.getDetail());
        if (address.getWard() != null) parts.add(address.getWard().getName());
        if (address.getWard() != null && address.getWard().getProvince() != null) {
            parts.add(address.getWard().getProvince().getName());
        }
        return parts.isEmpty() ? "--" : String.join(", ", parts);
    }

    private String formatMoney(double value) {
        return String.format("%,.0f VND", value);
    }
}
