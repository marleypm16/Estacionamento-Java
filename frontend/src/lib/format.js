export const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
export const time = new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit' })
export const dateTime = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })

export function localDate(value = new Date()) {
  const date = new Date(value)
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date - offset).toISOString().slice(0, 10)
}

export function minutesLabel(minutes = 0) {
  const hours = Math.floor(minutes / 60)
  const remaining = Math.round(minutes % 60)
  return hours ? `${hours}h ${remaining}min` : `${remaining} min`
}
