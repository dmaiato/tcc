-- V3__seed_admin.sql
-- Seeds the single admin user for Mission Control

INSERT INTO users (id, username, email, password_hash, xp, role, created_at)
VALUES (gen_random_uuid(), 'admin', 'admin@sqlab.com', '$2a$10$0YTw968Ija.MHjNGP8Zkbevt4xVPGONIpf4LQIzvnu/c8enHuYyDi', 0, 'ADMIN', NOW())
ON CONFLICT (email) DO NOTHING;
