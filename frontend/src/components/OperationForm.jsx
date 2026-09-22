import { useState } from 'react'

export function OperationForm({ kind, onSubmit, busy }) {
  const [plate, setPlate] = useState('')
  const isEntry = kind === 'entry'
  const label = isEntry ? 'Registrar entrada' : 'Registrar saída'

  async function submit(event) {
    event.preventDefault()
    if (!plate.trim()) return
    await onSubmit(plate)
    setPlate('')
  }

  return (
    <form onSubmit={submit} className={`border border-slate-900 p-5 sm:p-6 ${isEntry ? 'bg-slate-950 text-white' : 'bg-amber-300 text-slate-950'}`}>
      <div className="mb-7 flex items-start justify-between gap-4">
        <div>
          <p className="mb-1 text-sm font-bold">{isEntry ? 'Chegada' : 'Partida'}</p>
          <h2 className="font-display text-3xl font-black tracking-tight">{isEntry ? 'Abrir estadia' : 'Fechar e cobrar'}</h2>
        </div>
        <span className={`grid size-9 place-items-center border border-current text-lg ${isEntry ? 'text-amber-300' : ''}`} aria-hidden="true">{isEntry ? '↓' : '↑'}</span>
      </div>
      <label className="grid gap-2 text-sm font-bold" htmlFor={`${kind}-plate`}>
        Placa do veículo
        <input id={`${kind}-plate`} name="plate" value={plate} onChange={(event) => setPlate(event.target.value.toUpperCase())}
          placeholder="ABC1D23" autoComplete="off" maxLength="8" required
          className={`h-12 border px-3 font-mono text-base font-bold tracking-[0.14em] outline-none transition focus:ring-2 focus:ring-offset-2 ${isEntry ? 'border-slate-600 bg-slate-900 text-white focus:ring-amber-300 focus:ring-offset-slate-950' : 'border-slate-950 bg-amber-100 text-slate-950 focus:ring-slate-950 focus:ring-offset-amber-300'}`} />
      </label>
      <button type="submit" disabled={busy} className={`mt-5 h-12 w-full border border-current px-4 text-sm font-black transition enabled:hover:-translate-y-0.5 enabled:hover:shadow-[4px_4px_0_currentColor] disabled:cursor-wait disabled:opacity-60 ${isEntry ? 'bg-amber-300 text-slate-950' : 'bg-slate-950 text-white'}`}>
        {busy ? 'Registrando…' : label}
      </button>
    </form>
  )
}
