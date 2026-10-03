-- Product defaults requested on 4 October 2026. Existing stored choices are preserved.
alter table public.user_settings
 alter column personalization_enabled set default true,
 alter column listening_shared set default true,
 alter column taste_shared set default true,
 alter column private_session set default false;
