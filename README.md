# Taskify: demo didattica di Spec Kit

Questo repository è un **esempio didattico** di sviluppo guidato dalle specifiche (*Spec-Driven
Development*) con [Spec Kit](https://github.com/github/spec-kit) e Claude Code. Costruisce passo
passo **Taskify**, una piccola piattaforma Kanban per un team (cinque utenti predefiniti, tre progetti
di esempio, nessun login in questa prima fase), partendo dalla costituzione del progetto e arrivando al
codice.

Lo scopo non è il prodotto, ma il **percorso**: ogni fase di Spec Kit lascia artefatti leggibili nel
repository, e quattro **tag Git** fissano i punti di arrivo, così si può rivedere o spiegare ogni passo
da solo.

> Gli artefatti di Spec Kit (costituzione, spec, piano, task, analisi) e il codice sono scritti in
> inglese; questo README è in italiano per l'aula.

## I quattro step e i quattro tag

| Tag | Step | Comandi Spec Kit | Cosa contiene |
|---|---|---|---|
| [`v0.1-spec-plan-tasks`](../../tree/v0.1-spec-plan-tasks) | 1. Dalla costituzione ai task | `/speckit-constitution`, `/speckit-specify`, `/speckit-clarify`, `/speckit-plan`, `/speckit-tasks` | Costituzione v1.0.0, specifica con le chiarificazioni, piano tecnico, contratti OpenAPI, modello dati, quickstart e 102 task |
| [`v0.2-analysis-security-fixes`](../../tree/v0.2-analysis-security-fixes) | 2. Analisi e correzioni di sicurezza | `/speckit-analyze`, `/speckit-constitution` | Report di analisi (`analysis.md`), costituzione v1.1.0 con l'eccezione di identificazione nelle fasi iniziali, sezione *Security Considerations* nella spec |
| [`v0.3-tasks-fixes`](../../tree/v0.3-tasks-fixes) | 3. Correzioni ai task | (modifiche guidate dall'analisi) | Ordine dei task corretto (`User` nella fase Foundational) e decisione di progetto: se `project-service` è giù, nessuna modifica è possibile (fail closed) |
| [`v0.4-foundation-us1`](../../tree/v0.4-foundation-us1) | 4. Implementazione parziale | `/speckit-implement` | Setup, fondamenta e User Story 1 (T001-T054): backend, gateway, frontend, test |

Per rivivere un passo:

```bash
git checkout v0.1-spec-plan-tasks            # lo stato alla fine dello step 1
git diff v0.1-spec-plan-tasks v0.2-analysis-security-fixes --stat   # cosa è cambiato tra due step
git checkout main                            # per tornare all'ultimo stato
```

### Step 1: dalla costituzione ai task (`v0.1`)

1. **Costituzione** (`/speckit-constitution`): input *"Taskify is a Security-First application. All
   user inputs must be validated. We use a microservices architecture. Code must be fully documented."*
   Risultato: quattro principi in `.specify/memory/constitution.md`.
2. **Specifica** (`/speckit-specify`): una descrizione in linguaggio naturale diventa `specs/001-taskify-kanban/spec.md`
   con cinque user story prioritarie, requisiti testabili e criteri di successo, senza scelte tecnologiche.
3. **Chiarificazione** (`/speckit-clarify`): quattro domande mirate sul comportamento delle card (chi può
   spostare in Done, chi può commentare, assegnatario unico, modifica di titolo e descrizione) e una sulla
   durabilità dei dati. Le risposte finiscono nella sezione *Clarifications* e nei requisiti.
4. **Piano** (`/speckit-plan`): stack Java 21 + Spring Boot, React + Material UI, H2 su file. Il piano è
   stato rieseguito due volte: per passare da SQLite a H2 e per togliere notifiche e aggiornamenti in
   tempo reale. Produce `plan.md`, `research.md`, `data-model.md`, `contracts/` e `quickstart.md`.
5. **Task** (`/speckit-tasks`): 102 task raggruppati per user story, con test inclusi perché la
   costituzione li richiede.

### Step 2: analisi e correzioni di sicurezza (`v0.2`)

`/speckit-analyze` controlla in sola lettura la coerenza tra spec, piano e task. Ha trovato due problemi
**critici** di conformità alla costituzione:

- la costituzione chiedeva autenticazione su ogni endpoint, ma la spec rimanda il login;
- la spec non descriveva confini di fiducia, dati sensibili e casi di abuso.

La risposta didatticamente interessante: non si aggira la regola reinterpretandola, la si **emenda
esplicitamente**. La costituzione passa alla v1.1.0 con un'eccezione circoscritta (identificazione senza
login solo nelle fasi iniziali, a sei condizioni, con data di fine). Il report completo, con lo stato di
ogni finding, è in [`specs/001-taskify-kanban/analysis.md`](specs/001-taskify-kanban/analysis.md).

### Step 3: correzioni ai task (`v0.3`)

I finding di gravità alta vengono chiusi nei documenti prima di scrivere codice: un ordine di dipendenze
sbagliato tra i task e un'ambiguità sul comportamento quando un servizio è fuori uso. Quest'ultima richiede
una **decisione umana** (qui: fallire con un errore chiaro, "la card non può essere spostata ora").

### Step 4: implementazione parziale (`v0.4`)

`/speckit-implement` limitato a setup, fondamenta e User Story 1 (T001-T054). Durante l'implementazione è
emerso un problema che i documenti non avevano visto: il selettore utente ha bisogno dell'elenco utenti
*prima* di sapere chi è l'utente. La correzione (un endpoint pubblico) è stata riportata anche in spec,
contratto e test. Le User Story 2-5 (T055-T102) **non sono ancora implementate**.

## Cosa si impara da questo esempio

- La costituzione è **vincolante**: l'analisi la usa per bloccare, non per suggerire.
- Chiarire prima di pianificare costa poco; correggere dopo costa di più.
- Spec, piano e task devono restare allineati: ogni decisione presa a valle (H2, niente real-time, fail
  closed) va riportata nei documenti a monte.
- L'implementazione scopre ancora cose: i documenti si aggiornano, non si buttano.
- I tag rendono il percorso ripercorribile in aula, passo per passo.

## Dove trovare cosa

```text
.specify/memory/constitution.md       costituzione del progetto (v1.1.0)
specs/001-taskify-kanban/
  spec.md                             specifica con chiarificazioni e Security Considerations
  plan.md, research.md                piano tecnico e decisioni
  data-model.md, contracts/           modello dati e contratti OpenAPI
  quickstart.md                       scenari di validazione end-to-end
  tasks.md                            102 task (T001-T054 completati)
  analysis.md                         report di /speckit-analyze
services/project-service/             utenti e progetti (porta 8081)
services/task-service/                task e commenti (porta 8082)
services/gateway/                     punto di ingresso del browser (porta 8080)
frontend/                             React + TypeScript + Material UI (porta 3000)
docs/seed-data.md                     dati di esempio caricati al primo avvio
```

## Architettura in breve

- **Microservizi**: `project-service` (utenti e progetti) e `task-service` (task e commenti), dietro un
  **gateway** Spring Cloud. Ogni servizio ha il proprio database **H2 su file**: i dati sopravvivono ai
  riavvii e i dati di esempio vengono caricati solo al primo avvio (Flyway).
- **Sicurezza**: ogni input è validato lato server; `X-User-Id` identifica l'utente ma **non è
  autenticazione** (eccezione prevista dalla costituzione, da sostituire prima di una release reale); le
  chiamate tra servizi usano un token condiviso; `/internal/**` non è raggiungibile dal gateway.
- **Frontend**: dati letti all'apertura, al ritorno sulla finestra e con il pulsante *Refresh*. Non ci
  sono aggiornamenti in tempo reale né notifiche in questa fase.
- Se `project-service` non risponde, `task-service` rifiuta con 503 e non cambia nulla.

## Come eseguirlo in locale

Prerequisiti: JDK 21, Maven 3.9+, Node.js 20 o superiore.

```bash
export TASKIFY_SERVICE_TOKEN=$(openssl rand -hex 32)   # segreto condiviso tra i servizi
export TASKIFY_DATA_DIR=./data TASKIFY_DB_USER=taskify TASKIFY_DB_PASSWORD=cambiami

mvn -DskipTests package
java -jar services/project-service/target/project-service-0.1.0-SNAPSHOT.jar &
java -jar services/task-service/target/task-service-0.1.0-SNAPSHOT.jar &
java -jar services/gateway/target/gateway-0.1.0-SNAPSHOT.jar &

cd frontend && npm install && npm run dev               # http://localhost:3000
```

Per riportare i dati allo stato iniziale: fermare i servizi e cancellare la cartella `data/`.

Verifiche automatiche:

```bash
mvn verify                      # test backend + controllo Javadoc (checkstyle)
cd frontend && npm test         # test dei componenti
cd frontend && npm run e2e      # Playwright, richiede lo stack in esecuzione
```

Le variabili d'ambiente sono elencate (senza valori) in `.env.example`. Non committare mai un file `.env`.
Docker Compose (`docker-compose.yml`) è incluso ma **non è stato provato** in questo ambiente.

## Limiti noti (è una demo)

- Non è pronto per la produzione: senza login chiunque raggiunga l'app può agire come uno dei cinque utenti.
- Le User Story 2-5 (spostare le card, creare e assegnare task, commentare, creare progetti) sono pianificate
  nei task ma non implementate.
- Restano aperti alcuni finding di gravità media e bassa dell'analisi (per esempio il task per la CI).
