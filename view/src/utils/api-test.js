// Test API connection
const testAPI = async () => {
  try {
    console.log('Testing API connection...')
    
    // Test health check
    const healthResponse = await fetch('http://localhost:8080/api/public/health')
    const healthData = await healthResponse.json()
    console.log('Health check:', healthData)
    
    // Test invoice API
    const invoiceResponse = await fetch('http://localhost:8080/api/hoa-don-management/playlist')
    const invoiceData = await invoiceResponse.json()
    console.log('Invoice API:', invoiceData)
    
    // Test product API
    const productResponse = await fetch('http://localhost:8080/api/san-pham-management/playlist')
    const productData = await productResponse.json()
    console.log('Product API:', productData)
    
    // Test customer API
    const customerResponse = await fetch('http://localhost:8080/api/khach-hang-management/playlist')
    const customerData = await customerResponse.json()
    console.log('Customer API:', customerData)
    
  } catch (error) {
    console.error('API Test Error:', error)
  }
}

// Run test when page loads
if (typeof window !== 'undefined') {
  window.testAPI = testAPI
  console.log('API test function available. Run testAPI() in console.')
}
