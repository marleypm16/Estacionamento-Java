import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiError, parkingApi } from './api'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const clock = new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit' })
const date = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' })

function isToday(timestamp) {
  if (!timestamp) return false
  return new Date(timestamp).toDateString() === new Date().toDateString()
}

function App() {
  const [occupancy, setOccupancy] = useState(null)
  const [activeStays, setActiveStays] = useState([])
  const [finishedStays, setFinishedStays] = useState([])
  const [adminTokenInput, setAdminTokenInput] = useState('')
  const [adminToken, setAdminToken] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [notice, setNotice] = useState(null)
  const [historyMessage, setHistoryMessage] = useState(null)

  const refresh = useCallback(async () => {
    setLoading(true)
    const [occupancyResult, activeResult, historyResult] = await Promise.allSettled([
      parkingApi.getOccupancy(),
      parkingApi.getActiveStays(),
      parkingApi.getFinishedStays(adminToken),
    ])

    if (occupancyResult.status === 'fulfilled') setOccupancy(occupancyResult.value)
    if (activeResult.status === 'fulfilled') setActiveStays(activeResult.value)

    if (historyResult.status === 'fulfilled') {
      setFinishedStays(historyResult.value)
      setHistoryMessage(null)
    } else if (historyResult.reason instanceof ApiError && historyResult.reason.status === 403) {
      setHistoryMessage('Informe a chave administrativa para ver a receita do dia.')
    } else {
      setHistoryMessage('A receita do dia não está disponível agora.')
    }

    if (occupancyResult.status === 'rejected' || activeResult.status === 'rejected') {
      setNotice({ type: 'error', text: 'Não foi possível atualizar a operação. Verifique se a API está em execução.' })
    }
    setLoading(false)
  }, [adminToken])

  useEffect(() => {
    refresh()
  }, [refresh])

  const todayRevenue = useMemo(
    () => finishedStays
      .filter((stay) => isToday(stay.exitedAt))
      .reduce((total, stay) => total + Number(stay.amountCharged ?? 0), 0),
    [finishedStays],
  )

  async function submitStay(event, operation) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const plate = form.get('plate').trim().toUpperCase()
    if (!plate) return

    setSubmitting(true)
    setNotice(null)
    try {
      const stay = operation === 'entry'
        ? await parkingApi.registerEntry(plate)
        : await parkingApi.registerExit(plate)
      event.currentTarget.reset()
      setNotice({
        type: 'success',
        text: operation === 'entry'
          ? `${stay.plate} entrou às ${clock.format(new Date(stay.enteredAt))}.`
          : `${stay.plate} saiu. Cobrança: ${money.format(stay.amountCharged)}.`,
      })
      await refresh()
    } catch (error) {
      setNotice({ type: 'error', text: error.message ?? 'Não foi possível concluir a operação.' })
    } finally {
      setSubmitting(false)
    }
  }

  const available = occupancy?.available ?? '—'
  const capacity = occupancy?.capacity ?? '—'
  const occupancyPercent = occupancy ? Math.round((occupancy.occupied / occupancy.capacity) * 100) : 0

  return (
    <main className="shell">
      <header className="topbar">
        <a className="brand" href="#operacao" aria-label="Pátio, início">
          <span className="brand-mark" aria-hidden="true"><i /><i /><i /></span>
          <span>Pátio</span>
        </a>
        <div className="live-status" aria-live="polite">
          <span className={loading ? 'status-dot loading' : 'status-dot'} />
          {loading ? 'Atualizando operação' : 'Operação conectada'}
        </div>
      </header>

      <section className="masthead" id="operacao" aria-labelledby="page-title">
        <p className="date-line">{date.format(new Date())} <span aria-hidden="true">/</span> recepção</p>
        <h1 id="page-title">O pátio,<br />em movimento.</h1>
        <p>Registre cada passagem e mantenha a próxima vaga sempre à vista.</p>
      </section>

      <section className="operations" aria-label="Movimentação de veículos">
        <form className="operation-form entry-form" onSubmit={(event) => submitStay(event, 'entry')}>
          <div>
            <h2>Chegada</h2>
            <p>Abra uma nova estadia para o veículo.</p>
          </div>
          <label>
            <span>Placa</span>
            <input name="plate" placeholder="ABC1D23" autoComplete="off" maxLength="8" required />
          </label>
          <button type="submit" disabled={submitting}>Registrar entrada</button>
        </form>

        <form className="operation-form exit-form" onSubmit={(event) => submitStay(event, 'exit')}>
          <div>
            <h2>Saída</h2>
            <p>Feche a estadia e veja o valor a cobrar.</p>
          </div>
          <label>
            <span>Placa</span>
            <input name="plate" placeholder="ABC1D23" autoComplete="off" maxLength="8" required />
          </label>
          <button type="submit" disabled={submitting}>Registrar saída</button>
        </form>
      </section>

      {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}

      <section className="dashboard" aria-label="Situação do estacionamento">
        <article className="bay-meter">
          <div className="bay-outline" aria-hidden="true"><span /><span /><span /><span /></div>
          <p>vagas livres</p>
          <strong>{available}</strong>
          <span>de {capacity} posições</span>
          <div className="occupancy-track" aria-label={`${occupancyPercent}% de ocupação`}>
            <span style={{ width: `${occupancyPercent}%` }} />
          </div>
          <small>{occupancyPercent}% ocupado agora</small>
        </article>

        <article className="revenue">
          <div className="section-heading">
            <div>
              <h2>Receita de hoje</h2>
              <p>Saídas finalizadas desde meia-noite.</p>
            </div>
            <label className="token-field">
              <span>Chave administrativa</span>
              <input
                type="password"
                value={adminTokenInput}
                onChange={(event) => setAdminTokenInput(event.target.value)}
                onBlur={() => setAdminToken(adminTokenInput)}
                placeholder="Opcional no desenvolvimento"
                autoComplete="off"
              />
            </label>
          </div>
          {historyMessage ? <p className="muted-message">{historyMessage}</p> : <p className="revenue-amount">{money.format(todayRevenue)}</p>}
        </article>

        <article className="active-list">
          <div className="section-heading">
            <div>
              <h2>No pátio</h2>
              <p>{activeStays.length} veículo{activeStays.length === 1 ? '' : 's'} em permanência.</p>
            </div>
            <button className="text-button" type="button" onClick={refresh} disabled={loading}>Atualizar</button>
          </div>
          {activeStays.length === 0 && !loading ? (
            <p className="empty-state">Nenhum veículo estacionado. Registre uma chegada para começar.</p>
          ) : (
            <ul>
              {activeStays.map((stay) => (
                <li key={stay.id}>
                  <strong>{stay.plate}</strong>
                  <span>entrada às {clock.format(new Date(stay.enteredAt))}</span>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>
    </main>
  )
}

export default App
