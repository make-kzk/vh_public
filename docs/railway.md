# Deploying VibeHunt on Railway (GitHub)

Three services in one Railway project: **Postgres**, **Backend** (Ktor API), **Frontend** (React + nginx). Users open only the frontend URL; nginx proxies `/api/*` to the backend over Railway private networking.

Pushes to the connected GitHub repository trigger deploys. Railway does **not** auto-create Backend and Frontend from this monorepo — add both services once in the dashboard (see below).

## Architecture

| Service | Root Directory | Config file (from repo root) |
|---------|----------------|--------------------------------|
| Backend | **empty** (repo root) | `/railway.toml` |
| Frontend | **empty** (repo root) | `/deploy/railway.frontend.toml` |
| Postgres | — | — |

Both app images build from the **repository root** (backend needs `:core` + `:server`; frontend uses `deploy/Dockerfile.webReact` with `app/webReact/` paths).

Do **not** use Railpack/Nixpacks auto-detect for the backend — use `builder = "DOCKERFILE"` in the config file above.

Service names in `${{...}}` references are **case-sensitive** (e.g. `Backend`, `Frontend`, `Postgres`).

## GitHub repository

1. Create an empty repository on GitHub (account linked to Railway).
2. Push this project:

```powershell
cd C:\path\to\vibehunt
git remote add origin https://github.com/<user>/<repo>.git
git push -u origin master
```

Use `main` instead of `master` if your GitHub default branch is `main`.

## Railway dashboard setup (one-time)

Your project already has **Postgres**. Add two application services and connect the same GitHub repo to each.

### Backend

1. **+ New** → **Empty Service** → name `Backend`.
2. **Settings → Source**: connect your GitHub repository and branch.
3. **Settings → Root Directory**: leave **empty**.
4. **Settings → Config file**: `/railway.toml`.
5. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` |
   | `WEB_ORIGIN` | `https://${{Frontend.RAILWAY_PUBLIC_DOMAIN}}` (after frontend has a domain) |
   | `FRONTEND_URL` | same as `WEB_ORIGIN` |
   | `AUTH_DEV_MODE` | `false` (production) or `true` (staging) |

6. Deploy (automatic on push, or **Deploy** in the dashboard).
7. Public domain optional (health: `GET /health`).

### Frontend

1. **+ New** → **Empty Service** → name `Frontend`.
2. **Settings → Source**: connect the **same** GitHub repository and branch.
3. **Settings → Root Directory**: leave **empty**.
4. **Settings → Config file**: `/deploy/railway.frontend.toml`.
5. **Variables**:

   | Variable | Value |
   |----------|--------|
   | `BACKEND_UPSTREAM` | `${{Backend.RAILWAY_PRIVATE_DOMAIN}}:8080` |

   Use exact service names in `${{...}}`. Do **not** use `${{Backend.PORT}}` — cross-service `PORT` references resolve empty and nginx fails with `invalid port in upstream`.

   The API listens on `8080` (`deploy/Dockerfile.server` and Ktor default when `PORT` is unset).

6. **Networking → Public Networking**: **Generate Domain** (required for users).
7. Deploy.

### After frontend has a URL

If `WEB_ORIGIN` / `FRONTEND_URL` were not set via `${{Frontend.RAILWAY_PUBLIC_DOMAIN}}` before the domain existed, set them and redeploy **Backend**.

## Deploy order

1. Postgres (already provisioned)
2. Backend (`/health`, Flyway in logs)
3. Frontend (public domain + `BACKEND_UPSTREAM`)
4. Backend redeploy if `WEB_ORIGIN` / `FRONTEND_URL` needed updating

## Verification

| Check | How |
|-------|-----|
| API health | `GET https://<backend-domain>/health` → `{"status":"ok"}` |
| Frontend | `https://<frontend-domain>/` |
| API via proxy | Sign-in when `AUTH_DEV_MODE=true` on backend |
| Build | Deployment logs show **Dockerfile**, not **Railpack** |

## Troubleshooting

**Backend uses Railpack / `secret … not found`** — Config file not set or wrong. Set **Config file** to `/railway.toml`, redeploy. Do not rely on Java auto-detect.

**Backend: `:app:androidApp` directory does not exist (Gradle in Docker)** — `settings.gradle.kts` only includes app modules when their directories are present in the build context. Redeploy after pulling latest.

**Frontend: `couldn't locate the dockerfile at path Dockerfile`** — Wrong config file. Set **Config file** to `/deploy/railway.frontend.toml` and **empty** Root Directory.

**Frontend: `package.json` not found** — Wrong Dockerfile context. Use `/deploy/railway.frontend.toml`, **empty** Root Directory, and `deploy/Dockerfile.webReact`.

**`BACKEND_UPSTREAM is required` or nginx `invalid port in upstream`** — Set `BACKEND_UPSTREAM=${{Backend.RAILWAY_PRIVATE_DOMAIN}}:8080` on frontend (literal `8080`, not `${{Backend.PORT}}`).

**Database errors** — `DATABASE_URL=${{Postgres.DATABASE_URL}}`; SSL handled in app code.

**401 after login** — `WEB_ORIGIN` must match frontend URL exactly (`https://`, no trailing slash).

**403 on login (CORS)** — Backend logs show `CORS check fails` and `allowed hosts: [https://]`. Set `WEB_ORIGIN` and `FRONTEND_URL` to `https://<frontend-domain>` (not `https://${{Frontend.RAILWAY_PUBLIC_DOMAIN}}` before the frontend has a public domain), then redeploy Backend.

**NetworkError / upstream timed out on `/api/*`** — Frontend nginx cannot reach Backend over private networking. Common causes:

1. **Stale backend IP after redeploy** — redeploy **Frontend** (temporary fix), or deploy the nginx `resolver [fd12::10]` + `$backend_upstream` pattern in `app/webReact/nginx.conf.template`.
2. **IPv6 upstream timeout** — nginx may round-robin to the backend’s IPv6 private address while Ktor listens on IPv4 only (`0.0.0.0`). One request works (login), the next fails (registration). Fix: bind the API to `::` in `Application.kt` and/or set `resolver [fd12::10] ipv6=off` in nginx so proxy uses IPv4 only.

Confirm `BACKEND_UPSTREAM=${{Backend.RAILWAY_PRIVATE_DOMAIN}}:8080` on Frontend.

**Unnecessary rebuilds** — `watchPatterns` in `/railway.toml` and `/deploy/railway.frontend.toml` limit which file changes trigger each service.

## Local vs Railway

- No `.env` in containers; set variables in Railway.
- Railway sets `PORT` for both services.
- `DATABASE_URL` from Postgres is `postgresql://...`; server adds JDBC `sslmode=require`.
