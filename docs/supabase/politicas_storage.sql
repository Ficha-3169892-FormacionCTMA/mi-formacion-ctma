-- Ejecutar en Supabase: SQL Editor > New query > Run
-- Permite que la app (clave anon/publishable) suba, lea, reemplace y borre fotos en el bucket de evidencias.

-- 1. Bucket público (lo crea si no existe)
insert into storage.buckets (id, name, public)
values ('evidencias_bucket', 'evidencias_bucket', true)
on conflict (id) do update set public = true;

-- 2. Políticas sobre los archivos del bucket
create policy "evidencias_subir" on storage.objects
  for insert to anon, authenticated
  with check (bucket_id = 'evidencias_bucket');

create policy "evidencias_leer" on storage.objects
  for select to anon, authenticated
  using (bucket_id = 'evidencias_bucket');

create policy "evidencias_reemplazar" on storage.objects
  for update to anon, authenticated
  using (bucket_id = 'evidencias_bucket');

create policy "evidencias_borrar" on storage.objects
  for delete to anon, authenticated
  using (bucket_id = 'evidencias_bucket');
