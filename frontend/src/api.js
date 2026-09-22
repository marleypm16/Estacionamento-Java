const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

async function request(path, { method = 'GET', body, adminToken } = {}) {
  const headers = { Accept: 'application/json' }
  if (body) headers['Content-Type'] = 'application/json'
  if (adminToken) headers['X-Admin-Token'] = adminToken

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(payload?.message ?? 'Não foi possível concluir a operação.', response.status)
  }
  return payload
}

export const parkingApi = {
  getOccupancy: () => request('/api/v1/stays/occupancy'),
  getActiveStays: () => request('/api/v1/stays/active'),
  getFinishedStays: (adminToken) => request('/api/v1/stays?status=FINISHED', { adminToken }),
  registerEntry: (plate) => request('/api/v1/stays/entries', { method: 'POST', body: { plate } }),
  registerExit: (plate) => request('/api/v1/stays/exits', { method: 'POST', body: { plate } }),
}
