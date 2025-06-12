package com.hotel.controller;

import com.hotel.dto.BillResponse;
import com.hotel.entity.Bill;
import com.hotel.entity.BillDetail;
import com.hotel.enums.BillStatus;
import com.hotel.mapper.BillDetailMapper;
import com.hotel.mapper.BillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {
    
    private final BillMapper billMapper;
    private final BillDetailMapper billDetailMapper;
    
    /**
     * 查询所有账单
     */
    @GetMapping
    public List<BillResponse> getAllBills() {
        List<Bill> bills = billMapper.selectList(null);
        return bills.stream()
                .map(this::buildBillResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID查询账单详情
     */
    @GetMapping("/{billId}")
    public BillResponse getBillById(@PathVariable Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new RuntimeException("账单不存在");
        }
        return buildBillResponse(bill);
    }
    
    /**
     * 根据客户ID查询账单
     */
    @GetMapping("/customer/{customerId}")
    public List<BillResponse> getBillsByCustomerId(@PathVariable Long customerId) {
        List<Bill> bills = billMapper.findByCustomerId(customerId);
        return bills.stream()
                .map(this::buildBillResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据房间号查询账单
     */
    @GetMapping("/room/{roomNumber}")
    public List<BillResponse> getBillsByRoomNumber(@PathVariable Integer roomNumber) {
        List<Bill> bills = billMapper.findByRoomNumber(roomNumber);
        return bills.stream()
                .map(this::buildBillResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据房间ID查询账单
     */
    @GetMapping("/room-id/{roomId}")
    public List<BillResponse> getBillsByRoomId(@PathVariable Long roomId) {
        List<Bill> bills = billMapper.findByRoomId(roomId);
        return bills.stream()
                .map(this::buildBillResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询未支付账单
     */
    @GetMapping("/unpaid")
    public List<BillResponse> getUnpaidBills() {
        List<Bill> bills = billMapper.findUnpaidBills();
        return bills.stream()
                .map(this::buildBillResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 支付账单
     */
    @PostMapping("/{billId}/pay")
    public String payBill(@PathVariable Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new RuntimeException("账单不存在");
        }
        
        if (BillStatus.PAID.getCode().equals(bill.getStatus())) {
            return "账单已支付";
        }
        
        bill.setStatus(BillStatus.PAID.getCode());
        billMapper.updateById(bill);
        
        log.info("账单{}已支付，金额：{}", bill.getBillNumber(), bill.getTotalAmount());
        return "支付成功";
    }
    
    /**
     * 取消账单
     */
    @PostMapping("/{billId}/cancel")
    public String cancelBill(@PathVariable Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new RuntimeException("账单不存在");
        }
        
        if (BillStatus.PAID.getCode().equals(bill.getStatus())) {
            throw new RuntimeException("已支付账单不能取消");
        }
        
        bill.setStatus(BillStatus.CANCELLED.getCode());
        billMapper.updateById(bill);
        
        log.info("账单{}已取消", bill.getBillNumber());
        return "账单已取消";
    }
    
    /**
     * 查询账单详单
     */
    @GetMapping("/{billId}/details")
    public List<BillDetail> getBillDetails(@PathVariable Long billId) {
        return billDetailMapper.findByBillId(billId);
    }
    
    /**
     * 构建账单响应对象
     */
    private BillResponse buildBillResponse(Bill bill) {
        // 查询详单
        List<BillDetail> details = billDetailMapper.findByBillId(bill.getId());
        bill.setBillDetails(details);
        return BillResponse.fromBill(bill);
    }
} 