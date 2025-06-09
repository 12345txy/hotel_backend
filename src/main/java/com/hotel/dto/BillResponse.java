package com.hotel.dto;

import com.hotel.entity.Bill;
import com.hotel.entity.BillDetail;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单响应DTO
 */
@Data
@Accessors(chain = true)
public class BillResponse {
    
    /**
     * 账单ID
     */
    private Long billId;
    
    /**
     * 账单号
     */
    private String billNumber;
    
    /**
     * 房间号
     */
    private Integer roomNumber;
    
    /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 入住时间
     */
    private LocalDateTime checkInTime;
    
    /**
     * 退房时间
     */
    private LocalDateTime checkOutTime;
    
    /**
     * 住宿天数
     */
    private Integer stayDays;
    
    /**
     * 房费
     */
    private Double roomFee;
    
    /**
     * 空调总费用
     */
    private Double acTotalFee;
    
    /**
     * 总费用
     */
    private Double totalAmount;
    
    /**
     * 账单状态
     */
    private String status;
    
    /**
     * 账单状态描述
     */
    private String statusDesc;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 详单列表
     */
    private List<BillDetail> details;
    
    /**
     * 从Bill实体转换
     */
    public static BillResponse fromBill(Bill bill) {
        BillResponse response = new BillResponse();
        response.setBillId(bill.getId())
                .setBillNumber(bill.getBillNumber())
                .setRoomNumber(bill.getRoomId().intValue())
                .setCustomerName(bill.getCustomerName())
                .setCheckInTime(bill.getCheckInTime())
                .setCheckOutTime(bill.getCheckOutTime())
                .setStayDays(bill.getStayDays())
                .setRoomFee(bill.getRoomFee())
                .setAcTotalFee(bill.getAcTotalFee())
                .setTotalAmount(bill.getTotalAmount())
                .setStatus(bill.getStatus())
                .setStatusDesc(getStatusDesc(bill.getStatus()))
                .setCreateTime(bill.getCreateTime())
                .setRemark(bill.getRemark())
                .setDetails(bill.getBillDetails());
        return response;
    }
    
    private static String getStatusDesc(String status) {
        switch (status) {
            case "UNPAID": return "未支付";
            case "PAID": return "已支付";
            case "CANCELLED": return "已取消";
            default: return "未知状态";
        }
    }
} 