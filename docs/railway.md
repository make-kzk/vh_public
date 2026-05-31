# Deploying VibeHunt on Railway

This project runs as **three services** in one Railway project: Postgres (already provisioned), a **backend** (Ktor API), and a **frontend** (React + nginx). The browser only uses the frontend public URL; nginx proxies `/api/*` to the backend over Railway private networking.

## Architecture

| Service | Repository root directory | Config file (absolute from repo root) |
|---------|---------------------------|----------------------------------------|
| Backend | **Empty / repo root** | `/deploy/railway.backend.toml` |
| Frontend | `/app/webReact` | `/app/webReact/railway.toml` |
| Postgres | (managed) | — |

The backend **must** build from the repository root because `:server` depends on `:core`. Do not set the backend root directory to `server/`.

## First-time setup

### 1. Backend service

1. In your Railway project: **New → GitHub Repo** (this repository).
2. Name the service e.g. `backend`.
3. **Settings → Root Directory**: leave empty (repository root).
4. **Settings → Config file**: `/deploy/railway.backend.toml`
5. **Settings → Watch paths** (optional, reduces noise):
   - `/server/**`
   - `/core/**`
   - `/deploy/**`
   - `/gradle/**`
   - `/settings.gradle.kts`
   - `/build.gradle.kts`
   - `/gradle.properties`
6. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` |
   | `WEB_ORIGIN` | `https://${{Frontend.RAILWAY_PUBLIC_DOMAIN}}` |
   | `FRONTEND_URL` | `https://${{Frontend.RAILWAY_PUBLIC_DOMAIN}}` |
   | `AUTH_DEV_MODE` | `true` for staging, `false` when dev login should be off |

7. Enable **Private Networking** for this service.
8. Deploy and wait for a successful start (Flyway runs on boot).

### 2. Frontend service

1. **New → GitHub Repo** (same repository).
2. Name the service e.g. `frontend`.
3. **Settings → Root Directory**: `/app/webReact`
4. **Settings → Config file**: `/app/webReact/railway.toml`
5. **Settings → Watch paths**: `/app/webReact/**`
6. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `BACKEND_UPSTREAM` | `${{Backend.RAILWAY_PRIVATE_DOMAIN}}:${{Backend.PORT}}` |

   Replace `Backend` with your backend service name if different.

7. Enable **Private Networking**.
8. **Settings → Networking**: generate a public domain.

### 3. Wire origins

After the frontend has a public domain, ensure the backend has:

- `WEB_ORIGIN` = `https://<your-frontend-domain>`
- `FRONTEND_URL` = same URL

Redeploy the backend if you added the frontend domain after the first backend deploy.

## Deploy order

1. Postgres (already running)
2. Backend (migrations + `/health`)
3. Frontend (static assets + `/api` proxy)
4. Update backend `WEB_ORIGIN` / `FRONTEND_URL` if needed

## Verification

| Check | How |
|-------|-----|
| API health | `GET https://<backend-domain>/health` → `{"status":"ok"}` |
| Frontend | Open `https://<frontend-domain>/` |
| API via proxy | Dev login on frontend when `AUTH_DEV_MODE=true`; session cookie is set on the **frontend** host |
| Logs | Backend should show Flyway migration success on startup |

## Local vs Railway

- Containers do **not** read `.env` from the repo; set variables in Railway only (except local dev).
- Railway sets `PORT` for both services; the API and nginx entrypoint use it.
- Railway Postgres provides `DATABASE_URL` as `postgresql://...`; the server converts it to JDBC with `sslmode=require`.
- Cookies use `Secure` when `FRONTEND_URL` or `WEB_ORIGIN` uses `https://`, or when `COOKIE_SECURE=true`.

## Troubleshooting

**Backend build fails** — Confirm root directory is repo root, not `server/`. Check build logs for Gradle errors.

**Frontend: `BACKEND_UPSTREAM is required`** — Set `BACKEND_UPSTREAM` on the frontend service and enable private networking on both services.

**Database connection errors** — Confirm `DATABASE_URL` references the Postgres service. Check SSL: JDBC URL should include `sslmode=require` (handled automatically).

**401 / no session after login** — Ensure `WEB_ORIGIN` matches the frontend HTTPS URL exactly (no trailing slash). Cookies need HTTPS in production (`Secure` flag).

**CORS errors** — With the nginx proxy, the browser calls same-origin `/api`; CORS should not apply. If you call the backend public URL directly from the browser, add that origin to `WEB_ORIGIN`.
