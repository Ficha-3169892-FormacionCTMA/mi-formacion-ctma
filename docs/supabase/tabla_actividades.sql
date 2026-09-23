-- Ejecutar en Supabase: SQL Editor > New query > Run
-- Tabla donde la app respalda las actividades. El id es el mismo que usa Room en el teléfono.

create table if not exists public.actividades (
  id              bigint primary key,
  titulo          text        not null,
  descripcion     text        not null default '',
  fecha           text        not null,             -- formato AAAA-MM-DD
  prioridad       text        not null default 'MEDIA',
  progreso        integer     not null default 0 check (progreso between 0 and 100),
  completada      boolean     not null default false,
  actualizado_en  timestamptz not null default now()
);

alter table public.actividades enable row level security;

-- La app usa la clave anon/publishable: se le permite leer, crear, editar y borrar
create policy "actividades_leer" on public.actividades
  for select to anon, authenticated using (true);

create policy "actividades_crear" on public.actividades
  for insert to anon, authenticated with check (true);

create policy "actividades_editar" on public.actividades
  for update to anon, authenticated using (true) with check (true);

create policy "actividades_borrar" on public.actividades
  for delete to anon, authenticated using (true);
