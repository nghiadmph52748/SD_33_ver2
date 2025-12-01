package org.example.be_sp.model.response.invoice;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.model.response.HoaDonResponse;
import org.springframework.beans.BeanUtils;

/**
 * Invoice response payload dedicated to the invoice module. Extends the legacy
 * HoaDonResponse structure to preserve the existing response contract while
 * enabling invoice-specific helpers.
 */
public class InvoiceResponse extends HoaDonResponse {

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
}
