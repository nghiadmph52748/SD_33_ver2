export const formatCurrency = (value: number | string): string => {
  const numericValue = typeof value === "string" ? Number.parseFloat(value) : value;

  if (Number.isNaN(numericValue) || numericValue === 0) {
    return "₫0";
  }

  // Format VND currency (Vietnamese Dong)
  // Remove decimal places and add thousand separators
  const formatted = Math.round(numericValue).toLocaleString('vi-VN');
  return `₫${formatted}`;
};
