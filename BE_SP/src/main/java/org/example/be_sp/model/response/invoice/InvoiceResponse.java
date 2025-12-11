package org.example.be_sp.model.response.invoice;

import java.util.Collections;
import java.util.List;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.model.response.HoaDonResponse;
import org.springframework.beans.BeanUtils;

/**
 * Invoice response payload dedicated to the invoice module. Extends the legacy
 * HoaDonResponse structure to preserve the existing response contract while
 * enabling invoice-specific helpers.
 */
public class InvoiceResponse extends HoaDonResponse {

    private List<OrderStageResponse> orderStages = Collections.emptyList();

    public InvoiceResponse() {
        super();
    }

    public InvoiceResponse(HoaDon entity) {
        super(entity);
    }

    public static InvoiceResponse from(HoaDonResponse source) {
        InvoiceResponse target = new InvoiceResponse();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public List<OrderStageResponse> getOrderStages() {
        return orderStages;
    }

    public void setOrderStages(List<OrderStageResponse> orderStages) {
        this.orderStages = orderStages != null ? orderStages : Collections.emptyList();
    }
}
