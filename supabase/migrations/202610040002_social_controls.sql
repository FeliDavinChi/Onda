create index on public.recommendation_influences(source_user_id,receiver_id);
alter policy profile_read on public.profiles using (not private.blocked((select auth.uid()),id));
alter policy settings_owner on public.user_settings using (user_id=(select auth.uid()));
alter policy follow_read on public.follows using (not private.blocked((select auth.uid()),follower_id) and not private.blocked((select auth.uid()),following_id));
alter policy block_owner on public.blocked_users using (blocker_id=(select auth.uid()));
alter policy influence_owner on public.recommendation_influences using (receiver_id=(select auth.uid()));
alter policy presence_read on public.user_presence using (private.can_see_listening((select auth.uid()),user_id));
alter policy events_owner on public.listening_events using (user_id=(select auth.uid()));

-- Only names of people blocked by the caller; never their activity or taste.
create function public.blocked_people() returns jsonb
language plpgsql stable security definer set search_path = '' as $$
begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 return coalesce((select jsonb_agg(jsonb_build_object('id',p.id,'username',p.username,'display_name',p.display_name) order by p.display_name,p.id)
   from public.blocked_users b join public.profiles p on p.id=b.blocked_id where b.blocker_id=auth.uid()),'[]'::jsonb);
end; $$;
revoke all on function public.blocked_people() from public, anon;
grant execute on function public.blocked_people() to authenticated;
