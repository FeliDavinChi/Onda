-- Integration checks execute inside one transaction and leave no test accounts.
begin;
insert into auth.users(id) values
 ('00000000-0000-0000-0000-000000000001'),
 ('00000000-0000-0000-0000-000000000002'),
 ('00000000-0000-0000-0000-000000000003');
set local role authenticated;
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000001',true);
select public.setup_profile('onda_test_a','Receiver');
select public.set_social_preferences(false,false,false,true);
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000002',true);
select public.setup_profile('onda_test_b','Source');
select public.set_social_preferences(false,false,false,false);
select public.record_listening_event('10000000-0000-0000-0000-000000000001','abcdefghijk','Test song','Test artist','like');
do $$ begin
 if (select count(*) from public.listening_events)<>0 then raise exception 'Disabled personalization collected taste'; end if;
end $$;
select public.set_social_preferences(true,true,false,false);
select public.record_listening_event('10000000-0000-0000-0000-000000000002','abcdefghijk','Test song','Test artist','like');
select public.publish_listening('abcdefghijk','Test song','Test artist',true);
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000003',true);
select public.setup_profile('onda_test_c','Outsider');
do $$ begin
 if (select count(*) from public.user_presence)<>0 then raise exception 'Unfollowed viewer saw presence'; end if;
 if (select count(*) from public.listening_events)<>0 then raise exception 'Outsider read private events'; end if;
 begin
  insert into public.follows values('00000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000002',now());
  raise exception 'Client write was allowed';
 exception when insufficient_privilege then null;
 end;
end $$;
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000001',true);
select public.set_follow('00000000-0000-0000-0000-000000000002',true);
do $$ begin
 if jsonb_array_length(public.recommendation_context()->'sources')<>0 then raise exception 'Following enabled influence'; end if;
 if (select count(*) from public.user_presence)<>1 then raise exception 'Follower cannot see shared listening'; end if;
 if (select count(*) from public.listening_events)<>0 then raise exception 'Follower read source raw events'; end if;
end $$;
select public.set_influence('00000000-0000-0000-0000-000000000002',true);
do $$ begin
 if jsonb_array_length(public.recommendation_context()->'sources')<>1 then raise exception 'Explicit taste choice unavailable'; end if;
 if jsonb_array_length(public.recommendation_context()->'candidates')<>1 then raise exception 'Shared taste did not supply candidate metadata'; end if;
 if (public.recommendation_context()->'sources'->0->'affinities'->>'youtube:abcdefghijk')::numeric<>0.5 then raise exception 'Shared taste affinity was not permission scoped'; end if;
end $$;
select public.hide_listening('00000000-0000-0000-0000-000000000002',true);
do $$ begin
 if jsonb_array_length(public.recommendation_context()->'sources')<>1 then raise exception 'Hiding activity removed taste choice'; end if;
 if public.social_home()->'following'->0->'listening'<>'null'::jsonb then raise exception 'Hidden activity still visible'; end if;
end $$;
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000002',true);
select public.set_social_preferences(true,false,true,false);
select public.record_listening_event('10000000-0000-0000-0000-000000000003','ABCDEFGHIJK','Private song','Artist','listen');
do $$ begin
 if (select count(*) from public.listening_events)<>1 then raise exception 'Private session collected new taste'; end if;
 if (select count(*) from public.user_presence)<>0 then raise exception 'Private session left presence'; end if;
end $$;
select public.set_social_preferences(true,true,false,false);
select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000001',true);
do $$ begin
 if jsonb_array_length(public.recommendation_context()->'sources')<>0 then raise exception 'Restoring source consent resurrected choice'; end if;
end $$;
select public.set_influence('00000000-0000-0000-0000-000000000002',true);
select public.set_follow('00000000-0000-0000-0000-000000000002',false);
select public.set_follow('00000000-0000-0000-0000-000000000002',true);
do $$ begin
 if jsonb_array_length(public.recommendation_context()->'sources')<>0 then raise exception 'Refollow resurrected influence'; end if;
end $$;
select public.set_influence('00000000-0000-0000-0000-000000000002',true);
select public.set_block('00000000-0000-0000-0000-000000000002',true);
do $$ begin
 if jsonb_array_length(public.recommendation_context()->'sources')<>0 then raise exception 'Blocked source still contributed'; end if;
 if jsonb_array_length(public.blocked_people())<>1 then raise exception 'Own block controls unavailable'; end if;
 if exists(select 1 from public.profiles where id='00000000-0000-0000-0000-000000000002') then raise exception 'Blocked profile still visible'; end if;
end $$;
select public.set_block('00000000-0000-0000-0000-000000000002',false);
do $$ begin
 if exists(select 1 from public.follows where following_id='00000000-0000-0000-0000-000000000002') then raise exception 'Unblock restored follow'; end if;
end $$;
select 'social policy checks passed' as result;
rollback;
