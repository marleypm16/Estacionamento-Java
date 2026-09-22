import { time } from '../lib/format'

export function ActiveStays({ stays, loading }) {
  return (
    <section id="patio" className="border border-slate-300 bg-white" aria-labelledby="active-title">
      <header className="flex items-end justify-between gap-4 border-b border-slate-300 p-5 sm:p-6">
        <div>
          <p className="mb-1 text-sm font-bold text-slate-500">Movimentação atual</p>
          <h2 id="active-title" className="font-display text-3xl font-black tracking-tight">No pátio</h2>
        </div>
        <span className="font-display text-4xl font-black text-slate-950">{stays.length}</span>
      </header>
      {loading ? <p className="p-6 text-slate-600">Carregando veículos…</p> : stays.length === 0 ? <p className="p-6 text-slate-600">Nenhum veículo estacionado no momento.</p> : (
        <ul className="divide-y divide-slate-200">
          {stays.map((stay) => <li key={stay.id} className="flex items-center justify-between gap-4 p-5 sm:px-6">
            <strong className="font-mono text-base tracking-[0.12em] text-slate-950">{stay.plate}</strong>
            <span className="text-sm text-slate-600">Entrada às {time.format(new Date(stay.enteredAt))}</span>
          </li>)}
        </ul>
      )}
    </section>
  )
}
