import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiError, parkingApi } from './api'
import { ActiveStays } from './components/ActiveStays'
import { Brand } from './components/Brand'
import { AccessNeeded, PendingDetections } from './components/PendingDetections'
import { OperationForm } from './components/OperationForm'
import { localDate, minutesLabel, money } from './lib/format'

const today = localDate()
const monthStart = `${today.slice(0, 8)}01`

export default function AppP3() {
  const [occupancy, setOccupancy] = useState(null)
  const [activeStays, setActiveStays] = useState([])
  const [finishedStays, setFinishedStays] = useState([])
  const [pendingDetections, setPendingDetections] = useState([])
  const [report, setReport] = useState(null)
  const [adminTokenInput, setAdminTokenInput] = useState('')
  const [adminToken, setAdminToken] = useState('')
  const [range, setRange] = useState({ from: monthStart, to: today })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState(null)
  const [adminMessage, setAdminMessage] = useState('Informe a chave para liberar dados administrativos.')

  const refresh = useCallback(async () => {
    setLoading(true)
    const publicResults = await Promise.allSettled([parkingApi.getOccupancy(), parkingApi.getActiveStays()])
    if (publicResults[0].status === 'fulfilled') setOccupancy(publicResults[0].value)
    if (publicResults[1].status === 'fulfilled') setActiveStays(publicResults[1].value)
    if (publicResults.some((result) => result.status === 'rejected')) setNotice({ type: 'error', text: 'A operação não respondeu. Confirme se a API está em execução.' })
    if (!adminToken) {
      setFinishedStays([]); setReport(null); setPendingDetections([])
      setAdminMessage('Informe a chave para liberar dados administrativos.')
      setLoading(false)
      return
    }
    const privateResults = await Promise.allSettled([
      parkingApi.getFinishedStays(adminToken),
      parkingApi.getOperationsReport(range.from, range.to, adminToken),
      parkingApi.getPendingDetections(adminToken),
    ])
    if (privateResults.every((result) => result.status === 'fulfilled')) {
      setFinishedStays(privateResults[0].value); setReport(privateResults[1].value); setPendingDetections(privateResults[2].value); setAdminMessage(null)
    } else {
      const error = privateResults.find((result) => result.status === 'rejected')?.reason
      setAdminMessage(error instanceof ApiError && error.status === 403 ? 'A chave administrativa não foi aceita.' : 'Os dados administrativos não estão disponíveis agora.')
    }
    setLoading(false)
  }, [adminToken, range.from, range.to])

  useEffect(() => { refresh() }, [refresh])

  const occupancyPercent = occupancy?.capacity ? Math.round((occupancy.occupied / occupancy.capacity) * 100) : 0
  const todayRevenue = useMemo(() => finishedStays.filter((stay) => localDate(stay.exitedAt) === today).reduce((sum, stay) => sum + Number(stay.amountCharged ?? 0), 0), [finishedStays])

  async function runOperation(kind, plate) {
    setBusy(true); setNotice(null)
    try {
      const stay = kind === 'entry' ? await parkingApi.registerEntry(plate) : await parkingApi.registerExit(plate)
      setNotice({ type: 'success', text: kind === 'entry' ? `${stay.plate} entrou no pátio.` : `${stay.plate} saiu. Cobrança: ${money.format(stay.amountCharged)}.` })
      await refresh()
    } catch (error) { setNotice({ type: 'error', text: error.message ?? 'Não foi possível concluir a operação.' }) } finally { setBusy(false) }
  }

  async function resolveDetection(action, id, plate) {
    setBusy(true); setNotice(null)
    try {
      if (action === 'confirm') await parkingApi.confirmDetection(id, plate, adminToken)
      else await parkingApi.dismissDetection(id, 'Descartada pelo atendente.', adminToken)
      setNotice({ type: 'success', text: action === 'confirm' ? 'Leitura confirmada e processada.' : 'Leitura descartada.' })
      await refresh()
    } catch (error) { setNotice({ type: 'error', text: error.message ?? 'Não foi possível tratar a leitura.' }) } finally { setBusy(false) }
  }

  return <main className="min-h-screen bg-stone-100 text-slate-950"><div className="tactical-grid min-h-screen"><div className="mx-auto w-[min(1180px,calc(100%-2rem))] py-5 sm:w-[min(1180px,calc(100%-4rem))] sm:py-8">
    <header className="flex flex-wrap items-center justify-between gap-5 border-b-2 border-slate-950 pb-5"><Brand /><nav aria-label="Navegação" className="flex gap-4 text-sm font-bold text-slate-700"><a href="#patio">Pátio</a><a href="#relatorios">Relatórios</a><a href="#leituras">Leituras</a></nav><span className={`inline-flex items-center gap-2 text-sm font-bold ${loading ? 'text-amber-700' : 'text-emerald-800'}`}><i className={`size-2 rounded-full ${loading ? 'bg-amber-500' : 'bg-emerald-600'}`} />{loading ? 'Atualizando' : 'Operação conectada'}</span></header>
    <section id="operacao" className="grid gap-8 py-12 lg:grid-cols-[1.15fr_.85fr] lg:py-16" aria-labelledby="page-title"><div><p className="mb-4 font-mono text-sm font-bold tracking-wide text-slate-500">RECEPÇÃO · CONTROLE DE FLUXO</p><h1 id="page-title" className="max-w-3xl font-display text-5xl font-black leading-[.87] tracking-[-.055em] sm:text-7xl">O pátio não pode parar.</h1></div><p className="max-w-xl self-end text-lg leading-8 text-slate-600">Registre cada passagem, acompanhe a ocupação e deixe decisões incertas na fila certa: a do atendente.</p></section>
    <section className="grid gap-3 md:grid-cols-2" aria-label="Movimentação manual"><OperationForm kind="entry" onSubmit={(plate) => runOperation('entry', plate)} busy={busy} /><OperationForm kind="exit" onSubmit={(plate) => runOperation('exit', plate)} busy={busy} /></section>
    {notice && <p role="status" className={`mt-4 border p-4 text-sm font-bold ${notice.type === 'success' ? 'border-emerald-800 bg-emerald-50 text-emerald-950' : 'border-red-800 bg-red-50 text-red-950'}`}>{notice.text}</p>}
    <section className="mt-12 grid gap-3 lg:grid-cols-[.8fr_1.2fr]" aria-label="Situação atual"><article className="border border-slate-950 bg-slate-950 p-6 text-white sm:p-8"><p className="text-sm font-bold text-amber-300">Vagas livres</p><strong className="mt-2 block font-display text-8xl font-black leading-none tracking-[-.08em] text-amber-300">{occupancy?.available ?? '—'}</strong><p className="mt-3 text-slate-300">de {occupancy?.capacity ?? '—'} posições</p><div className="mt-10 h-3 bg-slate-700" aria-label={`${occupancyPercent}% de ocupação`}><div className="h-full bg-amber-300 transition-[width] duration-300 motion-reduce:transition-none" style={{ width: `${occupancyPercent}%` }} /></div><p className="mt-3 text-sm font-bold text-slate-300">{occupancyPercent}% ocupado agora</p></article><ActiveStays stays={activeStays} loading={loading} /></section>
    <section id="relatorios" className="mt-12 border border-slate-300 bg-white" aria-labelledby="report-title"><header className="grid gap-5 border-b border-slate-300 p-5 sm:grid-cols-[1fr_auto] sm:items-end sm:p-6"><div><p className="mb-1 text-sm font-bold text-slate-500">Visão administrativa</p><h2 id="report-title" className="font-display text-3xl font-black tracking-tight">Ritmo da operação</h2></div><form onSubmit={(event) => { event.preventDefault(); setAdminToken(adminTokenInput.trim()) }} className="flex flex-wrap gap-2"><label className="sr-only" htmlFor="admin-token">Chave administrativa</label><input id="admin-token" type="password" value={adminTokenInput} onChange={(event) => setAdminTokenInput(event.target.value)} placeholder="Chave administrativa" className="h-10 border border-slate-400 px-3 text-sm outline-none focus:ring-2 focus:ring-amber-400" autoComplete="off" /><button className="h-10 bg-slate-950 px-4 text-sm font-bold text-white">Liberar</button></form></header>
    {adminMessage ? <AccessNeeded title="Dados protegidos" text={adminMessage} /> : <><div className="flex flex-wrap gap-3 border-b border-slate-200 p-5 sm:px-6"><label className="grid gap-1 text-sm font-bold">De<input type="date" value={range.from} onChange={(event) => setRange({ ...range, from: event.target.value })} className="h-10 border border-slate-400 px-2 font-normal" /></label><label className="grid gap-1 text-sm font-bold">Até<input type="date" value={range.to} onChange={(event) => setRange({ ...range, to: event.target.value })} className="h-10 border border-slate-400 px-2 font-normal" /></label><button type="button" onClick={refresh} className="mt-auto h-10 border border-slate-950 px-4 text-sm font-bold hover:bg-slate-950 hover:text-white">Atualizar período</button></div><div className="grid divide-y divide-slate-200 sm:grid-cols-4 sm:divide-x sm:divide-y-0">{[[money.format(report?.revenue ?? 0), 'receita no período'], [report?.entries ?? 0, 'entradas'], [report?.exits ?? 0, 'saídas'], [minutesLabel(report?.averageStayMinutes), 'permanência média']].map(([value, label]) => <div key={label} className="p-5 sm:p-6"><strong className="font-display text-3xl font-black tracking-tight">{value}</strong><p className="mt-1 text-sm text-slate-600">{label}</p></div>)}</div><p className="border-t border-slate-200 px-5 py-3 text-sm text-slate-600">Receita de hoje: <strong>{money.format(todayRevenue)}</strong></p></>}</section>
    <div className="mt-12 pb-12"><PendingDetections detections={pendingDetections} locked={Boolean(adminMessage)} busy={busy} onConfirm={(id, plate) => resolveDetection('confirm', id, plate)} onDismiss={(id) => resolveDetection('dismiss', id)} /></div>
  </div></div></main>
}
