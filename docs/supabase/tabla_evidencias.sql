-- Ejecutar en Supabase: SQL Editor > New query > Run
-- Tabla con la metadata de las fotos subidas a evidencias_bucket.
-- (En el proyecto actual ya existe; este script sirve para recrearla en un proyecto nuevo.)

create table if not exists public.evidencias (
  id              uuid        primary key default gen_random_uuid(),
  actividad_id    text        not null,
  url_remota      text        not null,
  tipo_mime       text        not null default 'image/jpeg',
  tamano_bytes    bigint      not null default 0,
  fecha_creacion  timestamptz not null default now()
);

alter table public.evidencias enable row level security;

create policy "evidencias_tabla_leer" on public.evidencias
  for select to anon, authenticated using (true);

create policy "evidencias_tabla_crear" on public.evidencias
  for insert to anon, authenticated with check (true);

create policy "evidencias_tabla_borrar" on public.evidencias
  for delete to anon, authenticated using (true);
