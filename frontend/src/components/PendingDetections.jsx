import { useState } from 'react'
import { dateTime } from '../lib/format'

export function PendingDetections({ detections, locked, onConfirm, onDismiss, busy }) {
  const [plates, setPlates] = useState({})
  if (locked) return <AccessNeeded title="Pendências de leitura" text="Informe a chave administrativa para revisar leituras de câmera." />
  return (
    <section id="leituras" className="border border-slate-900 bg-white" aria-labelledby="detections-title">
      <header className="border-b border-slate-900 bg-slate-950 p-5 text-white sm:p-6">
        <p className="mb-1 text-sm font-bold text-amber-300">Revisão humana</p>
        <h2 id="detections-title" className="font-display text-3xl font-black tracking-tight">Leituras pendentes</h2>
      </header>
      {detections.length === 0 ? <p className="p-6 text-slate-600">Nenhuma leitura precisa de confirmação.</p> : (
        <ul className="divide-y divide-slate-200">
          {detections.map((detection) => <li key={detection.id} className="p-5 sm:p-6">
            <div className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1">
              <strong className="font-mono tracking-[0.12em]">{detection.rawPlate}</strong>
              <span className="text-sm text-slate-500">{detection.direction === 'ENTRY' ? 'Entrada' : 'Saída'} · {Math.round(detection.confidence * 100)}% · {dateTime.format(new Date(detection.detectedAt))}</span>
            </div>
            <p className="mt-2 text-sm leading-6 text-slate-600">{detection.reason}</p>
            <div className="mt-4 flex flex-col gap-2 sm:flex-row">
              <label className="sr-only" htmlFor={`plate-${detection.id}`}>Placa confirmada</label>
              <input id={`plate-${detection.id}`} value={plates[detection.id] ?? detection.rawPlate} onChange={(event) => setPlates({ ...plates, [detection.id]: event.target.value.toUpperCase() })} className="h-10 min-w-0 flex-1 border border-slate-400 px-3 font-mono text-sm font-bold tracking-wider outline-none focus:ring-2 focus:ring-amber-400" aria-label="Placa confirmada" />
              <button type="button" disabled={busy} onClick={() => onConfirm(detection.id, plates[detection.id] ?? detection.rawPlate)} className="h-10 bg-slate-950 px-4 text-sm font-bold text-white hover:bg-slate-700 disabled:opacity-60">Confirmar</button>
              <button type="button" disabled={busy} onClick={() => onDismiss(detection.id)} className="h-10 border border-slate-400 px-4 text-sm font-bold text-slate-700 hover:border-slate-950 disabled:opacity-60">Descartar</button>
            </div>
          </li>)}
        </ul>
      )}
    </section>
  )
}

export function AccessNeeded({ title, text }) {
  return <section className="border border-dashed border-slate-400 bg-slate-100 p-6"><h2 className="font-display text-2xl font-black">{title}</h2><p className="mt-2 max-w-md text-sm leading-6 text-slate-600">{text}</p></section>
}
