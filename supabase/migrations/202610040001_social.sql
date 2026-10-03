-- Onda social V1. The database owns identity, privacy and taste eligibility.
create schema if not exists private;
revoke all on schema private from public, anon, authenticated;

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  username text not null unique check (username ~ '^[a-z0-9_]{3,24}$'),
  display_name text not null check (char_length(display_name) between 1 and 80),
  created_at timestamptz not null default now()
);
create table public.user_settings (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  listening_shared boolean not null default false,
  taste_shared boolean not null default false,
  personalization_enabled boolean not null default false,
  private_session boolean not null default false,
  policy_version bigint not null default 1
);
create table public.follows (
  follower_id uuid not null references public.profiles(id) on delete cascade,
  following_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (follower_id, following_id), check (follower_id <> following_id)
);
create index on public.follows(following_id, follower_id);
create table public.blocked_users (
  blocker_id uuid not null references public.profiles(id) on delete cascade,
  blocked_id uuid not null references public.profiles(id) on delete cascade,
  primary key (blocker_id, blocked_id), check (blocker_id <> blocked_id)
);
create index on public.blocked_users(blocked_id, blocker_id);
create table public.recommendation_influences (
  receiver_id uuid not null references public.profiles(id) on delete cascade,
  source_user_id uuid not null references public.profiles(id) on delete cascade,
  enabled boolean not null default false,
  hidden_activity boolean not null default false,
  primary key (receiver_id, source_user_id), check (receiver_id <> source_user_id)
);
create table public.user_presence (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  video_id text not null check (video_id ~ '^[A-Za-z0-9_-]{11}$'),
  title text not null check (char_length(title) between 1 and 300),
  artist text not null check (char_length(artist) between 1 and 300),
  is_playing boolean not null,
  updated_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '90 seconds')
);
create table public.listening_events (
  id uuid primary key,
  user_id uuid not null references public.profiles(id) on delete cascade,
  video_id text not null check (video_id ~ '^[A-Za-z0-9_-]{11}$'),
  title text not null check (char_length(title) between 1 and 300),
  artist text not null check (char_length(artist) between 1 and 300),
  kind text not null check (kind in ('listen','like','save','skip','dislike')),
  created_at timestamptz not null default now()
);
create index on public.listening_events(user_id, created_at desc, id desc);

create function private.blocked(a uuid, b uuid) returns boolean
language sql stable security definer set search_path = '' as $$
 select exists(select 1 from public.blocked_users where (blocker_id=a and blocked_id=b) or (blocker_id=b and blocked_id=a));
$$;
create function private.follows_user(viewer uuid, subject uuid) returns boolean
language sql stable security definer set search_path = '' as $$
 select exists(select 1 from public.follows where follower_id=viewer and following_id=subject);
$$;
create function private.can_use_taste(viewer uuid, subject uuid) returns boolean
language sql stable security definer set search_path = '' as $$
 select viewer is not null and viewer<>subject and not private.blocked(viewer,subject)
   and exists(select 1 from public.user_settings where user_id=viewer and personalization_enabled)
   and private.follows_user(viewer,subject)
   and exists(select 1 from public.user_settings where user_id=subject and taste_shared);
$$;
create function private.can_see_listening(viewer uuid, subject uuid) returns boolean
language sql stable security definer set search_path = '' as $$
 select viewer=subject or (viewer is not null and not private.blocked(viewer,subject)
   and private.follows_user(viewer,subject)
   and exists(select 1 from public.user_settings where user_id=subject and listening_shared and not private_session));
$$;

alter table public.profiles enable row level security;
alter table public.user_settings enable row level security;
alter table public.follows enable row level security;
alter table public.blocked_users enable row level security;
alter table public.recommendation_influences enable row level security;
alter table public.user_presence enable row level security;
alter table public.listening_events enable row level security;

create policy profile_read on public.profiles for select to authenticated using (not private.blocked(auth.uid(),id));
create policy settings_owner on public.user_settings for select to authenticated using (user_id=auth.uid());
create policy follow_read on public.follows for select to authenticated using (not private.blocked(auth.uid(),follower_id) and not private.blocked(auth.uid(),following_id));
create policy block_owner on public.blocked_users for select to authenticated using (blocker_id=auth.uid());
create policy influence_owner on public.recommendation_influences for select to authenticated using (receiver_id=auth.uid());
create policy presence_read on public.user_presence for select to authenticated using (private.can_see_listening(auth.uid(),user_id));
create policy events_owner on public.listening_events for select to authenticated using (user_id=auth.uid());

-- Writes use narrow RPCs, never arbitrary owner/membership fields.
revoke all on public.profiles, public.user_settings, public.follows, public.blocked_users,
 public.recommendation_influences, public.user_presence, public.listening_events from anon, authenticated;
grant select on public.profiles, public.user_settings, public.follows, public.blocked_users,
 public.recommendation_influences, public.user_presence, public.listening_events to authenticated;
grant usage on schema private to authenticated;
revoke all on all functions in schema private from public, anon, authenticated;
grant execute on function private.blocked(uuid,uuid), private.follows_user(uuid,uuid),
 private.can_use_taste(uuid,uuid), private.can_see_listening(uuid,uuid) to authenticated;

create function public.setup_profile(p_username text, p_display_name text) returns void
language plpgsql security definer set search_path = '' as $$
begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 insert into public.profiles(id,username,display_name) values(auth.uid(),lower(trim(p_username)),trim(p_display_name))
 on conflict(id) do update set username=excluded.username, display_name=excluded.display_name;
 insert into public.user_settings(user_id) values(auth.uid()) on conflict do nothing;
end; $$;

create function public.set_follow(p_user_id uuid, p_following boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
 -- Serialize consent/relationship writes, including eligibility checks.
 perform pg_catalog.pg_advisory_xact_lock(1869505633, 1);
 if auth.uid() is null or auth.uid()=p_user_id then raise exception 'Invalid follow'; end if;
 if private.blocked(auth.uid(),p_user_id) then raise exception 'This connection is unavailable'; end if;
 if p_following then
  insert into public.follows(follower_id,following_id) values(auth.uid(),p_user_id) on conflict do nothing;
 else
  delete from public.follows where follower_id=auth.uid() and following_id=p_user_id;
  update public.recommendation_influences set enabled=false where receiver_id=auth.uid() and source_user_id=p_user_id;
 end if;
 update public.user_settings set policy_version=policy_version+1 where user_id=auth.uid();
end; $$;

create function public.set_influence(p_user_id uuid, p_enabled boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
 -- Serialize consent/relationship writes, including eligibility checks.
 perform pg_catalog.pg_advisory_xact_lock(1869505633, 1);
 if auth.uid() is null or auth.uid()=p_user_id then raise exception 'Invalid taste source'; end if;
 if p_enabled and not private.can_use_taste(auth.uid(),p_user_id) then raise exception 'Shared taste is unavailable'; end if;
 insert into public.recommendation_influences(receiver_id,source_user_id,enabled) values(auth.uid(),p_user_id,p_enabled)
 on conflict(receiver_id,source_user_id) do update set enabled=excluded.enabled;
 update public.user_settings set policy_version=policy_version+1 where user_id=auth.uid();
end; $$;

create function public.set_social_preferences(p_listening_shared boolean, p_taste_shared boolean, p_private_session boolean, p_personalization_enabled boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
 -- Serialize consent/relationship writes, including eligibility checks.
 perform pg_catalog.pg_advisory_xact_lock(1869505633, 1);
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 update public.user_settings set listening_shared=p_listening_shared,taste_shared=p_taste_shared,
  private_session=p_private_session,policy_version=policy_version+1 where user_id=auth.uid();
 update public.user_settings set personalization_enabled=p_personalization_enabled where user_id=auth.uid();
 if not found then raise exception 'Create a profile first'; end if;
 if not p_listening_shared or p_private_session then delete from public.user_presence where user_id=auth.uid(); end if;
 if not p_taste_shared then
  update public.recommendation_influences set enabled=false where source_user_id=auth.uid();
  update public.user_settings set policy_version=policy_version+1 where user_id in
   (select receiver_id from public.recommendation_influences where source_user_id=auth.uid());
 end if;
end; $$;

create function public.set_block(p_user_id uuid, p_blocked boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
 -- Serialize consent/relationship writes, including eligibility checks.
 perform pg_catalog.pg_advisory_xact_lock(1869505633, 1);
 if auth.uid() is null or auth.uid()=p_user_id then raise exception 'Invalid block'; end if;
 if p_blocked then
  insert into public.blocked_users(blocker_id,blocked_id) values(auth.uid(),p_user_id) on conflict do nothing;
  delete from public.follows where (follower_id=auth.uid() and following_id=p_user_id) or (following_id=auth.uid() and follower_id=p_user_id);
  update public.recommendation_influences set enabled=false where
   (receiver_id=auth.uid() and source_user_id=p_user_id) or (source_user_id=auth.uid() and receiver_id=p_user_id);
 else delete from public.blocked_users where blocker_id=auth.uid() and blocked_id=p_user_id;
 end if;
 update public.user_settings set policy_version=policy_version+1 where user_id in (auth.uid(),p_user_id);
end; $$;

create function public.hide_listening(p_user_id uuid, p_hidden boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
 if auth.uid() is null or auth.uid()=p_user_id then raise exception 'Invalid person'; end if;
 insert into public.recommendation_influences(receiver_id,source_user_id,hidden_activity) values(auth.uid(),p_user_id,p_hidden)
 on conflict(receiver_id,source_user_id) do update set hidden_activity=excluded.hidden_activity;
end; $$;

create function public.publish_listening(p_video_id text, p_title text, p_artist text, p_is_playing boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
 -- Finish or reject this event before a privacy preference update commits.
 perform 1 from public.user_settings where user_id=auth.uid() for share;
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 if not exists(select 1 from public.user_settings where user_id=auth.uid() and listening_shared and not private_session) then
  delete from public.user_presence where user_id=auth.uid(); return;
 end if;
 insert into public.user_presence(user_id,video_id,title,artist,is_playing,updated_at,expires_at)
 values(auth.uid(),p_video_id,p_title,p_artist,p_is_playing,now(),now()+interval '90 seconds')
 on conflict(user_id) do update set video_id=excluded.video_id,title=excluded.title,artist=excluded.artist,
  is_playing=excluded.is_playing,updated_at=excluded.updated_at,expires_at=excluded.expires_at;
end; $$;

create function public.social_home() returns jsonb
language plpgsql stable security definer set search_path = '' as $$
begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 return jsonb_build_object(
  'profile',(select to_jsonb(p) from public.profiles p where p.id=auth.uid()),
  'settings',(select to_jsonb(s) from public.user_settings s where s.user_id=auth.uid()),
  'following',coalesce((select jsonb_agg(jsonb_build_object('id',p.id,'username',p.username,'display_name',p.display_name,
    'following',true,'influence_enabled',coalesce(i.enabled,false) and private.can_use_taste(auth.uid(),p.id),
    'can_influence',private.can_use_taste(auth.uid(),p.id),'hidden_activity',coalesce(i.hidden_activity,false),
    'listening',case when private.can_see_listening(auth.uid(),p.id) and not coalesce(i.hidden_activity,false)
      then (select to_jsonb(a) || jsonb_build_object('live',a.is_playing and a.expires_at>now()) from public.user_presence a where a.user_id=p.id)
      else null end) order by p.display_name,p.id)
    from public.follows f join public.profiles p on p.id=f.following_id
    left join public.recommendation_influences i on i.receiver_id=auth.uid() and i.source_user_id=p.id
    where f.follower_id=auth.uid() and not private.blocked(auth.uid(),p.id)), '[]'::jsonb));
end; $$;

create function public.search_people(p_query text) returns jsonb
language plpgsql stable security definer set search_path = '' as $$
begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 if char_length(trim(p_query))<2 then return '[]'::jsonb; end if;
 return coalesce((select jsonb_agg(to_jsonb(results)) from (
  select p.id,p.username,p.display_name,private.follows_user(auth.uid(),p.id) as following,
   private.can_use_taste(auth.uid(),p.id) as can_influence,
   coalesce((select enabled from public.recommendation_influences where receiver_id=auth.uid() and source_user_id=p.id),false)
     and private.can_use_taste(auth.uid(),p.id) as influence_enabled
  from public.profiles p where p.id<>auth.uid() and not private.blocked(auth.uid(),p.id)
   and (position(lower(trim(p_query)) in p.username)>0 or position(lower(trim(p_query)) in lower(p.display_name))>0)
  order by p.username,p.id limit 30) results),'[]'::jsonb);
end; $$;

create function public.record_listening_event(p_id uuid,p_video_id text,p_title text,p_artist text,p_kind text) returns void
language plpgsql security definer set search_path = '' as $$
begin
 -- Finish or reject this event before a privacy preference update commits.
 perform 1 from public.user_settings where user_id=auth.uid() for share;
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 if not exists(select 1 from public.user_settings where user_id=auth.uid() and (personalization_enabled or taste_shared) and not private_session) then return; end if;
 insert into public.listening_events(id,user_id,video_id,title,artist,kind)
 values(p_id,auth.uid(),p_video_id,p_title,p_artist,p_kind) on conflict(id) do nothing;
end; $$;

-- Only permitted aggregate scores and metadata, never raw friend event history.
create function public.recommendation_context() returns jsonb
language plpgsql stable security definer set search_path = '' as $$
declare result jsonb;
begin
 if auth.uid() is null then raise exception 'Sign in required'; end if;
 with eligible as (
  select i.source_user_id,p.display_name from public.recommendation_influences i join public.profiles p on p.id=i.source_user_id
  where i.receiver_id=auth.uid() and i.enabled and private.can_use_taste(auth.uid(),i.source_user_id)
 ), pool as (
  select distinct on(e.video_id) e.video_id,e.title,e.artist from public.listening_events e
  where ((e.user_id=auth.uid() and exists(select 1 from public.user_settings where user_id=auth.uid() and personalization_enabled)) or e.user_id in(select source_user_id from eligible)) and e.kind in('listen','like','save')
  order by e.video_id,e.created_at desc,e.id desc limit 200
 ), affinities as (
  select e.user_id,p.video_id,least(1.0,sum(case e.kind when 'like' then 0.5 when 'save' then 0.5 when 'listen' then 0.1 else 0 end)) as score
  from pool p join public.listening_events e on e.video_id=p.video_id or lower(e.artist)=lower(p.artist)
  where (e.user_id=auth.uid() or e.user_id in(select source_user_id from eligible)) and e.kind in('listen','like','save')
  group by e.user_id,p.video_id
 ) select jsonb_build_object(
  'policy_version',(select policy_version from public.user_settings where user_id=auth.uid()),
  'candidates',coalesce((select jsonb_agg(jsonb_build_object('id','youtube:'||p.video_id,'videoId',p.video_id,'title',p.title,'artist',p.artist,
    'personal',coalesce((select score from affinities a where a.user_id=auth.uid() and a.video_id=p.video_id),0))) from pool p),'[]'::jsonb),
  'sources',coalesce((select jsonb_agg(jsonb_build_object('id',s.source_user_id,'name',s.display_name,
    'enabled',true,'consent',true,'following',true,'blocked',false,
    'affinities',coalesce((select jsonb_object_agg('youtube:'||a.video_id,a.score) from affinities a where a.user_id=s.source_user_id),'{}'::jsonb))) from eligible s),'[]'::jsonb),
  'excluded',coalesce((select jsonb_agg(distinct 'youtube:'||video_id) from public.listening_events where user_id=auth.uid() and kind='dislike'),'[]'::jsonb)
 ) into result;
 return result;
end; $$;

revoke all on function public.setup_profile(text,text), public.set_follow(uuid,boolean), public.set_influence(uuid,boolean),
 public.set_social_preferences(boolean,boolean,boolean,boolean), public.set_block(uuid,boolean), public.hide_listening(uuid,boolean),
 public.publish_listening(text,text,text,boolean), public.social_home(), public.search_people(text),
 public.record_listening_event(uuid,text,text,text,text), public.recommendation_context() from public, anon;
grant execute on function public.setup_profile(text,text), public.set_follow(uuid,boolean), public.set_influence(uuid,boolean),
 public.set_social_preferences(boolean,boolean,boolean,boolean), public.set_block(uuid,boolean), public.hide_listening(uuid,boolean),
 public.publish_listening(text,text,text,boolean), public.social_home(), public.search_people(text),
 public.record_listening_event(uuid,text,text,text,text), public.recommendation_context() to authenticated;
