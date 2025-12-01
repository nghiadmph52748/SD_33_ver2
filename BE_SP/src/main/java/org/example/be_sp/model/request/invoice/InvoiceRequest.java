package org.example.be_sp.model.request.invoice;

import org.example.be_sp.model.request.BanHangTaiQuayRequest;

/**
 * Request payload for invoice create/update operations. Extends the legacy
 * BanHangTaiQuayRequest to retain existing serialization behaviour while
 * providing a dedicated type for the invoice module.
 */
public class InvoiceRequest extends BanHangTaiQuayRequest {
}
