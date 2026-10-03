-- New profiles match the product defaults; saving a profile preserves user choices.
begin;
insert into auth.users(id) values ('00000000-0000-0000-0000-000000000010');
set local role authenticated;
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000010',true);
select public.setup_profile('onda_defaults_test','Defaults test');
do $$ begin
 if not exists (
  select 1 from public.user_settings
  where user_id=auth.uid() and listening_shared and taste_shared
   and personalization_enabled and not private_session
 ) then raise exception 'New profile must enable personalization, listening sharing and taste sharing, with private session off'; end if;
 if jsonb_array_length(public.recommendation_context()->'sources')<>0 then
  raise exception 'New profile must not automatically select people to influence recommendations';
 end if;
end $$;
select public.set_social_preferences(false,false,true,false);
select public.setup_profile('onda_defaults_test','Updated name');
do $$ begin
 if not exists (
  select 1 from public.user_settings
  where user_id=auth.uid() and not listening_shared and not taste_shared
   and not personalization_enabled and private_session
 ) then raise exception 'Saving profile reset existing settings'; end if;
end $$;
select 'social default checks passed' as result;
rollback;
