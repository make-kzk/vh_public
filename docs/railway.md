# Deploying VibeHunt on Railway

Three services in one Railway project: **Postgres**, **backend** (Ktor API), **frontend** (React + nginx). Users open only the frontend URL; nginx proxies `/api/*` to the backend over Railway private networking (enabled by default — no toggle in the UI).

## Architecture

| Service | Root Directory | Config file (from repo root) |
|---------|----------------|--------------------------------|
| Backend | **empty** (repo root) | `/railway.toml` or `/deploy/railway.backend.toml` |
| Frontend | **empty** (repo root) | `/deploy/railway.frontend.toml` |
| Postgres | — | — |

Both app images build from the **repository root** (backend needs `:core` + `:server`; frontend uses `deploy/Dockerfile.webReact` with `app/webReact/` paths).

Do **not** use Railpack/Nixpacks auto-detect for the backend — use `builder = "DOCKERFILE"` in the config file above.

## Dashboard setup (one-time)

### Backend

1. Service name e.g. `backend` (Empty Service or GitHub repo).
2. **Settings → Root Directory**: leave **empty**.
3. **Settings → Config file**: `/railway.toml` (or `/deploy/railway.backend.toml`).
4. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (use your Postgres service name) |
   | `WEB_ORIGIN` | `https://${{frontend.RAILWAY_PUBLIC_DOMAIN}}` (after frontend has a domain) |
   | `FRONTEND_URL` | same as `WEB_ORIGIN` |
   | `AUTH_DEV_MODE` | `true` (staging) or `false` (production) |

5. Public domain optional (health checks can use generated domain or private URL).

### Frontend

1. Service name e.g. `frontend`.
2. **Settings → Root Directory**: leave **empty** (not `/app/webReact` when using `/deploy/railway.frontend.toml`).
3. **Settings → Config file**: `/deploy/railway.frontend.toml`.
4. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `BACKEND_UPSTREAM` | `${{backend.RAILWAY_PRIVATE_DOMAIN}}:${{backend.PORT}}` |

   Use exact backend service name in `${{...}}`.

5. **Networking → Public Networking**: **Generate Domain** (required for users).

### After frontend has a URL

Set backend `WEB_ORIGIN` and `FRONTEND_URL`, then redeploy backend.

## Deploy with Railway CLI

```powershell
cd C:\path\to\vibehunt
railway login
railway link    # project + environment; repeat with railway service link backend / frontend as needed
```

Deploy (service names must match your Railway services):

```powershell
.\scripts\railway-deploy.ps1 backend
.\scripts\railway-deploy.ps1 frontend
# or
.\scripts\railway-deploy.ps1 all
```

Linux/macOS: `./scripts/railway-deploy.sh all`

Manual:

```powershell
railway up --service backend
railway up --service frontend
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

**Backend uses Railpack / `secret … not found`** — Config file not set or wrong. Set **Config file** to `/railway.toml`, redeploy. Do not rely on Java auto-detect.

**Frontend: `package.json` not found** — Wrong Dockerfile context. Use `/deploy/railway.frontend.toml`, **empty** Root Directory, and `deploy/Dockerfile.webReact`. Old setup used `app/webReact/Dockerfile` with repo root context.

**`BACKEND_UPSTREAM is required`** — Set variable on frontend; fix `${{backend...}}` service name.

**Database errors** — `DATABASE_URL=${{Postgres.DATABASE_URL}}`; SSL handled in app code.

**401 after login** — `WEB_ORIGIN` must match frontend URL exactly (`https://`, no trailing slash).

## Alternative: frontend root `/app/webReact`

If Root Directory is `/app/webReact` and config is `/app/webReact/railway.toml`, `dockerfilePath` must be `Dockerfile` (not `app/webReact/Dockerfile`). Prefer the `/deploy/railway.frontend.toml` layout above for CLI deploys.

## Local vs Railway

- No `.env` in containers; set variables in Railway.
- Railway sets `PORT` for both services.
- `DATABASE_URL` from Postgres is `postgresql://...`; server adds JDBC `sslmode=require`.
