package org.example.be_sp.model.request.invoice;

import org.example.be_sp.model.request.AddressChangeNotificationRequest;

/**
 * Request payload dedicated for invoice address change workflows. Inherits the
 * existing address change structure to ensure backward compatibility with the
 * current frontend payload while allowing service-level customisation.
 */
public class InvoiceAddressChangeRequest extends AddressChangeNotificationRequest {
}
