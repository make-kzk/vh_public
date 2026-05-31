# Deploying VibeHunt on Railway

Three services in one Railway project: **Postgres**, **Backend** (Ktor API), **Frontend** (React + nginx). Users open only the frontend URL; nginx proxies `/api/*` to the backend over Railway private networking (enabled by default — no toggle in the UI).

## Architecture

| Service | Root Directory | Config file (from repo root) |
|---------|----------------|--------------------------------|
| Backend | **empty** (repo root) | `/railway.toml` or `/deploy/railway.backend.toml` |
| Frontend | **empty** (repo root) | `/deploy/railway.frontend.toml` |
| Postgres | — | — |

Both app images build from the **repository root** (backend needs `:core` + `:server`; frontend uses `deploy/Dockerfile.webReact` with `app/webReact/` paths).

Do **not** use Railpack/Nixpacks auto-detect for the backend — use `builder = "DOCKERFILE"` in the config file above.

**CLI service names are case-sensitive.** If your services are named `Backend` and `Frontend`, use those exact names in `railway up --service …` (not `backend` / `frontend`).

## Dashboard setup (one-time)

### Backend

1. Service name e.g. `Backend` (Empty Service or GitHub repo).
2. **Settings → Root Directory**: leave **empty**.
3. **Settings → Config file**: `/railway.toml` (or `/deploy/railway.backend.toml`).
4. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (use your Postgres service name) |
   | `WEB_ORIGIN` | `https://${{Frontend.RAILWAY_PUBLIC_DOMAIN}}` (after frontend has a domain) |
   | `FRONTEND_URL` | same as `WEB_ORIGIN` |
   | `AUTH_DEV_MODE` | `true` (staging) or `false` (production) |

5. Public domain optional (health checks can use generated domain or private URL).

### Frontend

1. Service name e.g. `Frontend`.
2. **Settings → Root Directory**: leave **empty** (not `/app/webReact` when using `/deploy/railway.frontend.toml`).
3. **Settings → Config file**: `/deploy/railway.frontend.toml` — **not** `/app/webReact/railway.toml`.
4. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `BACKEND_UPSTREAM` | `${{Backend.RAILWAY_PRIVATE_DOMAIN}}:8080` |

   Use exact service names in `${{...}}` (case-sensitive). Do **not** use `${{Backend.PORT}}` here — cross-service `PORT` references resolve empty and nginx fails with `invalid port in upstream`.

   The API listens on `8080` (`deploy/Dockerfile.server` and Ktor default when `PORT` is unset).

5. **Networking → Public Networking**: **Generate Domain** (required for users).

### After frontend has a URL

Set backend `WEB_ORIGIN` and `FRONTEND_URL`, then redeploy backend.

## Deploy with Railway CLI

```powershell
cd C:\path\to\vibehunt
railway login
railway link    # project + environment
```

Deploy (defaults match services named `Backend` / `Frontend`):

```powershell
.\scripts\railway-deploy.ps1 backend
.\scripts\railway-deploy.ps1 frontend
# or
.\scripts\railway-deploy.ps1 all
```

Different service names:

```powershell
$env:RAILWAY_BACKEND_SERVICE = "my-api"
$env:RAILWAY_FRONTEND_SERVICE = "my-web"
.\scripts\railway-deploy.ps1 all
```

Linux/macOS: `./scripts/railway-deploy.sh all` (same `RAILWAY_*_SERVICE` env vars).

Manual:

```powershell
railway up --service Backend
railway up --service Frontend
```

CLI uploads from repo root and respects `.gitignore`. Docker builds use `.dockerignore` (includes `app/webReact` for the frontend image).

## Deploy order

1. Postgres
2. Backend (`/health`, Flyway in logs)
3. Frontend (public domain + `BACKEND_UPSTREAM`)
4. Backend `WEB_ORIGIN` / `FRONTEND_URL` if not set via references

## Verification

| Check | How |
|-------|-----|
| API health | `GET https://<backend-domain>/health` → `{"status":"ok"}` |
| Frontend | `https://<frontend-domain>/` |
| API via proxy | Dev login when `AUTH_DEV_MODE=true` |
| Build | Logs show **Dockerfile**, not **Railpack** |

## Troubleshooting

**`Service not found` (CLI)** — `railway up --service` name must match the Railway service exactly (e.g. `Backend`, not `backend`). Use `railway service list` to check names. Override in scripts via `RAILWAY_BACKEND_SERVICE` / `RAILWAY_FRONTEND_SERVICE`.

**Backend uses Railpack / `secret … not found`** — Config file not set or wrong. Set **Config file** to `/railway.toml`, redeploy. Do not rely on Java auto-detect.

**Backend: `:app:androidApp` directory does not exist (Gradle in Docker)** — Fixed in repo: `settings.gradle.kts` only includes app modules when their directories are present in the build context. Redeploy after pulling latest.

**Frontend: `couldn't locate the dockerfile at path Dockerfile`** — Wrong config file. Set **Config file** to `/deploy/railway.frontend.toml` (not `/app/webReact/railway.toml`) and **empty** Root Directory.

**Frontend: `package.json` not found** — Wrong Dockerfile context. Use `/deploy/railway.frontend.toml`, **empty** Root Directory, and `deploy/Dockerfile.webReact`.

**`BACKEND_UPSTREAM is required` or nginx `invalid port in upstream`** — Set `BACKEND_UPSTREAM=${{Backend.RAILWAY_PRIVATE_DOMAIN}}:8080` on frontend (literal `8080`, not `${{Backend.PORT}}`). Wrong config file (`/app/webReact/railway.toml` with repo-root context) also breaks the Docker build — use `/deploy/railway.frontend.toml` and empty Root Directory.

**Database errors** — `DATABASE_URL=${{Postgres.DATABASE_URL}}`; SSL handled in app code.

**401 after login** — `WEB_ORIGIN` must match frontend URL exactly (`https://`, no trailing slash).

## Alternative: frontend root `/app/webReact`

If Root Directory is `/app/webReact` and config is `/app/webReact/railway.toml`, `dockerfilePath` must be `Dockerfile` (not `app/webReact/Dockerfile`). Prefer the `/deploy/railway.frontend.toml` layout above for CLI deploys.

## Local vs Railway

- No `.env` in containers; set variables in Railway.
- Railway sets `PORT` for both services.
- `DATABASE_URL` from Postgres is `postgresql://...`; server adds JDBC `sslmode=require`.
