-- V3__seed_admin.sql
-- Seeds the single admin user for Mission Control with deterministic UUID

INSERT INTO users (id, username, email, password_hash, xp, role, created_at)
VALUES ('00000000-0000-0000-0000-000000000000', 'admin', 'admin@sqlab.com', '$2a$10$0YTw968Ija.MHjNGP8Zkbevt4xVPGONIpf4LQIzvnu/c8enHuYyDi', 0, 'ADMIN', NOW())
ON CONFLICT (email) DO NOTHING;
