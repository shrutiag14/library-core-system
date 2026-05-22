export function apiMessage(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const inner = (error as { error?: { message?: string; fieldErrors?: Record<string, string> } }).error;
    if (inner?.fieldErrors && Object.keys(inner.fieldErrors).length) {
      return Object.entries(inner.fieldErrors).map(([field, message]) => `${field}: ${message}`).join(', ');
    }
    if (inner?.message) {
      return inner.message;
    }
  }
  return 'Operation failed. Please try again.';
}
